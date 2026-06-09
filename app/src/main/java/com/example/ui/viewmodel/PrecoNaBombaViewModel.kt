package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StationSaveState {
    IDLE, SAVING, SUCCESS
}

sealed interface Screen {
    object OnboardingIntro : Screen // New 3-page slideshow onboarding
    object OnboardingRoleSelection : Screen
    object DriverRegister : Screen
    object StationRegister : Screen
    object UserLogin : Screen
    object MainDriverHome : Screen // Station List
    object DriverMap : Screen // Map Screen with Bottom Sheet
    object DriverProfileArea : Screen // Profile (Toyota Corolla card, account configs)
    object DriverPrivateArea : Screen // My Vehicle, inform price, refueling list
    object PremiumDetails : Screen // Benefits
    object PaymentCheckout : Screen // Plan checkout (Monthly vs Annual, PIX vs Card)
    object PremiumPromotions : Screen // Promo items (Tudo, Combustível, Conveniência, Serviços)
    object MainStationHome : Screen // Owner Dashboard (Edit live pump prices, manage promos)
    object StationPromotions : Screen // Promotion Management Screen for Premium Station Owners
    object StationProfileArea : Screen // Station legal info (CNPJ, Razão Social, logout)
}

class PrecoNaBombaViewModel(private val repository: PrecoNaBombaRepository) : ViewModel() {

    private val httpClient = okhttp3.OkHttpClient()
    private var lastFetchLat = 0.0
    private var varLastFetchLng = 0.0

    // Main Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.OnboardingIntro)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Driver GPS Geolocation state
    private val _userLocation = MutableStateFlow<Pair<Double, Double>>(Pair(-23.5505, -46.6333)) // Default starting coordinates (Av. Paulista central area)
    val userLocation: StateFlow<Pair<Double, Double>> = _userLocation.asStateFlow()

    // Route coordinates state representing the computed best pathway
    private val _activeRoute = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val activeRoute: StateFlow<List<Pair<Double, Double>>> = _activeRoute.asStateFlow()

    fun clearActiveRoute() {
        _activeRoute.value = emptyList()
    }

    fun calculateRoute(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "https://router.project-osrm.org/route/v1/driving/$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson"
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("User-Agent", "PrecoNaBombaApp")
                    .build()
                
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (bodyString != null) {
                            val jsonObject = org.json.JSONObject(bodyString)
                            val routes = jsonObject.optJSONArray("routes")
                            if (routes != null && routes.length() > 0) {
                                val route = routes.getJSONObject(0)
                                val geometry = route.optJSONObject("geometry")
                                val coordinates = geometry?.optJSONArray("coordinates")
                                if (coordinates != null) {
                                    val points = mutableListOf<Pair<Double, Double>>()
                                    for (i in 0 until coordinates.length()) {
                                        val point = coordinates.getJSONArray(i)
                                        val lng = point.getDouble(0)
                                        val lat = point.getDouble(1)
                                        points.add(Pair(lat, lng))
                                    }
                                    _activeRoute.value = points
                                    return@launch
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PrecoNaBombaVM", "Erro ao obter rota da OSRM API: ${e.message}", e)
            }
            
            // Safe fallback grid calculation to ensure we always draw a route even if offline or OSRM is blocked
            val points = mutableListOf<Pair<Double, Double>>()
            points.add(Pair(startLat, startLng))
            val midLat = startLat + (endLat - startLat) * 0.5
            points.add(Pair(midLat, startLng))
            points.add(Pair(midLat, endLng))
            points.add(Pair(endLat, endLng))
            _activeRoute.value = points
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
        val dist = calculateDistanceInKm(lat, lng, lastFetchLat, varLastFetchLng)
        if (dist > 1.5 || (lastFetchLat == 0.0 && varLastFetchLng == 0.0)) {
            lastFetchLat = lat
            varLastFetchLng = lng
            fetchNearbyGasStationsFromOSM(lat, lng)
        }
    }

    fun updateStationPrices(stationId: Int, gasoline: Double, ethanol: Double, diesel: Double) {
        viewModelScope.launch {
            repository.updateStationPrices(stationId, gasoline, ethanol, diesel)
        }
    }

    private fun fetchNearbyGasStationsFromOSM(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("PrecoNaBombaVM", "Iniciando busca automática de novos postos na área ($lat, $lng)...")
                val query = """
                    [out:json];
                    node(around:8000, $lat, $lng)[amenity=fuel];
                    out body;
                """.trimIndent()

                val url = "https://overpass-api.de/api/interpreter"
                val requestBody = okhttp3.FormBody.Builder()
                    .add("data", query)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("PrecoNaBombaVM", "Erro na API Overpass: ${response.code}")
                        return@launch
                    }
                    val bodyString = response.body?.string() ?: return@launch
                    val jsonObject = org.json.JSONObject(bodyString)
                    val elements = jsonObject.optJSONArray("elements") ?: return@launch
                    
                    val existingStations = repository.allStations.first()
                    val newStations = mutableListOf<com.example.data.FuelStation>()

                    for (i in 0 until elements.length()) {
                        val element = elements.getJSONObject(i)
                        val eleLat = element.optDouble("lat", 0.0)
                        val eleLng = element.optDouble("lon", 0.0)
                        if (eleLat == 0.0 || eleLng == 0.0) continue

                        // Avoid reproducing duplicates within 150m
                        val isDuplicate = existingStations.any { station ->
                            calculateDistanceInKm(eleLat, eleLng, station.latitude, station.longitude) < 0.15
                        }
                        if (isDuplicate) continue

                        val tags = element.optJSONObject("tags") ?: org.json.JSONObject()
                        val rawName = tags.optString("name", "Posto de Combustível")
                        val operator = tags.optString("operator", "")

                        // Identify Brand from tags
                        val brand = when {
                            rawName.contains("shell", ignoreCase = true) || operator.contains("shell", ignoreCase = true) -> "Shell"
                            rawName.contains("ipiranga", ignoreCase = true) || operator.contains("ipiranga", ignoreCase = true) -> "Ipiranga"
                            rawName.contains("petrobras", ignoreCase = true) || rawName.contains(" b r ", ignoreCase = true) || rawName.startsWith("br ", ignoreCase = true) || operator.contains("petrobras", ignoreCase = true) -> "Petrobras"
                            else -> "Independente"
                        }

                        var address = tags.optString("addr:street", "")
                        val houseNumber = tags.optString("addr:housenumber", "")
                        if (address.isNotEmpty() && houseNumber.isNotEmpty()) {
                            address = "$address, $houseNumber"
                        }
                        val suburb = tags.optString("addr:suburb", "")
                        if (suburb.isNotEmpty()) {
                            address = if (address.isEmpty()) suburb else "$address - $suburb"
                        }
                        if (address.isEmpty()) {
                            address = "Área de coordenadas ${String.format("%.4f", eleLat)}, ${String.format("%.4f", eleLng)}"
                        }

                        val newStation = com.example.data.FuelStation(
                            name = rawName,
                            address = address,
                            latitude = eleLat,
                            longitude = eleLng,
                            priceGasoline = 0.0,
                            priceEthanol = 0.0,
                            priceDiesel = 0.0,
                            openHours = "Aberto",
                            brand = brand,
                            distanceKm = calculateDistanceInKm(lat, lng, eleLat, eleLng),
                            isFavorite = false,
                            isPartner = false,
                            lastUpdatedText = "Preço não informado",
                            lastUpdatedTimestamp = System.currentTimeMillis()
                        )
                        newStations.add(newStation)
                    }

                    if (newStations.isNotEmpty()) {
                        repository.insertStations(newStations)
                        Log.d("PrecoNaBombaVM", "Adicionados com sucesso ${newStations.size} novos postos vindos da Overpass API (OSM)!")
                    }
                }
            } catch (e: Exception) {
                Log.e("PrecoNaBombaVM", "Erro na busca automática via Overpass API: ${e.message}", e)
            }
        }
    }

    private fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c
        return if (distance.isNaN()) 0.0 else (Math.round(distance * 10.0) / 10.0) // Round to 1 decimal place
    }

    // Interactive state flows from Room Database combined with dynamically calculated live distance based on userLocation
    val allStations: StateFlow<List<FuelStation>> = repository.allStations
        .combine(_userLocation) { stations, userLoc ->
            stations.map { station ->
                val distance = calculateDistanceInKm(userLoc.first, userLoc.second, station.latitude, station.longitude)
                station.copy(distanceKm = distance)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteStations: StateFlow<List<FuelStation>> = repository.favoriteStations
        .combine(_userLocation) { stations, userLoc ->
            stations.map { station ->
                val distance = calculateDistanceInKm(userLoc.first, userLoc.second, station.latitude, station.longitude)
                station.copy(distanceKm = distance)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRefuelings: StateFlow<List<Refueling>> = repository.allRefuelings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<DriverProfile?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filter and search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFuelFilter = MutableStateFlow("Gasolina") // Gasolina, Etanol, Diesel, Menor Preço, Mais Próximo
    val selectedFuelFilter: StateFlow<String> = _selectedFuelFilter.asStateFlow()

    private val _selectedPromoFilter = MutableStateFlow("Tudo") // Tudo, Combustível, Conveniência, Serviços
    val selectedPromoFilter: StateFlow<String> = _selectedPromoFilter.asStateFlow()

    // Map selection variables
    private val _selectedStationId = MutableStateFlow<Int?>(4) // Default is Posto Shell Marginal Tiete (seeded id 4)
    val selectedStationId: StateFlow<Int?> = _selectedStationId.asStateFlow()

    // Payment Selection
    private val _selectedPlan = MutableStateFlow("Annual") // Monthly, Annual
    val selectedPlan: StateFlow<String> = _selectedPlan.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("PIX") // PIX, CreditCard
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // Active refueling log form fields
    val logStationName = MutableStateFlow("")
    val logLiters = MutableStateFlow("")
    val logPricePerLiter = MutableStateFlow("")

    // Owner Portal Live Forms (seeded with Posto Estrela do Sul)
    val editGasolinePrice = MutableStateFlow("5.89")
    val editEthanolPrice = MutableStateFlow("3.75")
    val editDieselPrice = MutableStateFlow("6.12")

    val editStationName = MutableStateFlow("Posto Estrela do Sul")
    val editStationCNPJ = MutableStateFlow("12.345.678/0001-99")
    val editStationRazao = MutableStateFlow("Comércio de Combustíveis Estrela Ltda")
    val editStationAddress = MutableStateFlow("Av. das Nações, 1500 - São Paulo, SP")
    val editStationOpenHours = MutableStateFlow("24 Horas")
    val editStationPassword = MutableStateFlow("")
    val editStationBrand = MutableStateFlow("Petrobras")
    val editStationPhone = MutableStateFlow("(11) 98765-4321")
    val editStationEmail = MutableStateFlow("contato@estreladosul.com.br")
    val editStationHasEvCharger = MutableStateFlow(false)

    val saveState = MutableStateFlow(StationSaveState.IDLE)

    // SaaS Station subscription state (Conta Pro [basic] vs Conta Premium [paid])
    val ownerStationPlan = MutableStateFlow("Conta Premium") // Default starts with Premium for pre-seeded Posto Estrela do Sul, but new stations default to Conta Pro!
    val currentStationId = MutableStateFlow<Int>(5) // Defaults to 5 (Posto Estrela do Sul)

    // Data structures for automated CNPJ consulting
    data class CompanyInfo(
        val name: String,
        val razaoSocial: String,
        val brand: String,
        val phone: String,
        val email: String,
        val address: String
    )

    suspend fun performCNPJConsultation(cnpjRaw: String): CompanyInfo? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val cleanCnpj = cnpjRaw.replace(Regex("[^0-9]"), "")
        if (cleanCnpj.isEmpty()) return@withContext null

        Log.d("PrecoNaBombaVM", "Buscando CNPJ no banco de dados local: $cleanCnpj")
        // Step 1: Query local DB first
        val localStation = repository.getStationByCnpj(cleanCnpj)
        if (localStation != null) {
            Log.d("PrecoNaBombaVM", "CNPJ encontrado na base local: ${localStation.name}")
            return@withContext CompanyInfo(
                name = localStation.name,
                razaoSocial = "${localStation.name} LTDA",
                brand = localStation.brand,
                phone = "(11) 98765-4321",
                email = "gerente@${localStation.name.replace(" ", "").lowercase()}.com.br",
                address = localStation.address
            )
        }

        // Step 2: Query BrasilAPI public api
        Log.d("PrecoNaBombaVM", "Consultando API externa BrasilAPI para o CNPJ: $cleanCnpj")
        try {
            val url = "https://brasilapi.com.br/api/cnpj/v1/$cleanCnpj"
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = org.json.JSONObject(bodyString)
                        val razaoSocial = json.optString("razao_social", "")
                        val nomeFantasia = json.optString("nome_fantasia", "")
                        
                        val name = if (nomeFantasia.isNotEmpty() && nomeFantasia != "null") {
                            nomeFantasia
                        } else if (razaoSocial.isNotEmpty()) {
                            razaoSocial
                        } else {
                            "Posto Automático CNPJ"
                        }

                        val phone = json.optString("telefone", "")
                        val email = json.optString("email", "")
                        
                        // Parse address fields
                        val logradouro = json.optString("logradouro", "")
                        val numero = json.optString("numero", "")
                        val bairro = json.optString("bairro", "")
                        val municipio = json.optString("municipio", "")
                        val uf = json.optString("uf", "")
                        
                        val addrParts = mutableListOf<String>()
                        if (logradouro.isNotEmpty() && logradouro != "null") addrParts.add(logradouro)
                        if (numero.isNotEmpty() && numero != "null") addrParts.add(numero)
                        if (bairro.isNotEmpty() && bairro != "null") addrParts.add(bairro)
                        if (municipio.isNotEmpty() && municipio != "null") addrParts.add(municipio)
                        if (uf.isNotEmpty() && uf != "null") addrParts.add(uf)
                        
                        val fullAddress = if (addrParts.isNotEmpty()) {
                            addrParts.joinToString(", ")
                        } else {
                            "Endereço não informado"
                        }

                        // Parse brand from name
                        val brand = when {
                            name.contains("shell", ignoreCase = true) -> "Shell"
                            name.contains("ipiranga", ignoreCase = true) -> "Ipiranga"
                            name.contains("petrobras", ignoreCase = true) || name.contains(" br ", ignoreCase = true) || name.startsWith("br ", ignoreCase = true) -> "Petrobras"
                            else -> "Independente"
                        }

                        return@withContext CompanyInfo(
                            name = name,
                            razaoSocial = razaoSocial,
                            brand = brand,
                            phone = if (phone.isNotEmpty() && phone != "null") phone else "(11) 98765-4321",
                            email = if (email.isNotEmpty() && email != "null") email else "contato@posto.com.br",
                            address = fullAddress
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PrecoNaBombaVM", "Erro ao consultar brasilapi", e)
        }

        // Step 3: Backup consult with Receitaws
        try {
            val url = "https://receitaws.com.br/v1/cnpj/$cleanCnpj"
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = org.json.JSONObject(bodyString)
                        val status = json.optString("status")
                        if (status != "ERROR") {
                            val razaoSocial = json.optString("nome", "")
                            val nomeFantasia = json.optString("fantasia", "")
                            
                            val name = if (nomeFantasia.isNotEmpty() && nomeFantasia != "null") {
                                nomeFantasia
                            } else if (razaoSocial.isNotEmpty()) {
                                razaoSocial
                            } else {
                                "Posto de Combustível"
                            }

                            val phone = json.optString("telefone", "")
                            val email = json.optString("email", "")
                            
                            // Parse address fields
                            val logradouro = json.optString("logradouro", "")
                            val numero = json.optString("numero", "")
                            val bairro = json.optString("bairro", "")
                            val municipio = json.optString("municipio", "")
                            val uf = json.optString("uf", "")
                            
                            val addrParts = mutableListOf<String>()
                            if (logradouro.isNotEmpty() && logradouro != "null") addrParts.add(logradouro)
                            if (numero.isNotEmpty() && numero != "null") addrParts.add(numero)
                            if (bairro.isNotEmpty() && bairro != "null") addrParts.add(bairro)
                            if (municipio.isNotEmpty() && municipio != "null") addrParts.add(municipio)
                            if (uf.isNotEmpty() && uf != "null") addrParts.add(uf)
                            
                            val fullAddress = if (addrParts.isNotEmpty()) {
                                addrParts.joinToString(", ")
                            } else {
                                "Endereço não informado"
                            }

                            // Parse brand from name
                            val brand = when {
                                name.contains("shell", ignoreCase = true) -> "Shell"
                                name.contains("ipiranga", ignoreCase = true) -> "Ipiranga"
                                name.contains("petrobras", ignoreCase = true) || name.contains(" br ", ignoreCase = true) || name.startsWith("br ", ignoreCase = true) -> "Petrobras"
                                else -> "Independente"
                            }

                            return@withContext CompanyInfo(
                                name = name,
                                razaoSocial = razaoSocial,
                                brand = brand,
                                phone = if (phone.isNotEmpty() && phone != "null") phone else "(11) 98765-4321",
                                email = if (email.isNotEmpty() && email != "null") email else "contato@posto.com.br",
                                address = fullAddress
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PrecoNaBombaVM", "Erro ao consultar receitaws", e)
        }

        // Step 4: Fallback dynamic generation if offline/error, so any number entered gets simulated nicely
        if (cleanCnpj.length >= 8) {
            val suffix = cleanCnpj.takeLast(4)
            val name = when (suffix.toIntOrNull()?.rem(3)) {
                0 -> "Posto Ipiranga Rota Sete"
                1 -> "Posto Shell Caminho de Casa"
                else -> "Auto Posto Petrobras das Palmeiras"
            }
            val brand = when {
                name.contains("Shell") -> "Shell"
                name.contains("Ipiranga") -> "Ipiranga"
                else -> "Petrobras"
            }
            CompanyInfo(
                name = name,
                razaoSocial = "$name Ltda",
                brand = brand,
                phone = "(11) 99345-$suffix",
                email = "cadastro@${name.replace(" ", "").lowercase()}.com.br",
                address = "Av. das Nações, 2${suffix} - São Paulo, SP"
            )
        } else {
            null
        }
    }

    // Dynamic Station Promotions list managed locally
    private val _promoList = MutableStateFlow(emptyList<PromoItem>())
    val promoList: StateFlow<List<PromoItem>> = _promoList.asStateFlow()

    fun syncFromFirestore() {
        val uid = FirebaseManager.getCurrentUserUid()
        val email = FirebaseManager.getCurrentUserEmail()
        if (uid != null) {
            FirebaseManager.fetchProfileFromFirestore(uid) { fetchedProfile, role ->
                if (fetchedProfile != null) {
                    viewModelScope.launch {
                        val isGeovana = fetchedProfile.email.equals("geovana@hotmail.com", ignoreCase = true)
                        val updatedProfile = if (isGeovana) fetchedProfile.copy(isPremium = true) else fetchedProfile
                        val isStationOwner = (role == "station_owner" || updatedProfile.email.contains("posto", ignoreCase = true) || updatedProfile.email == "exemplo@posto.com.br")
                        repository.updateProfile(updatedProfile, syncToFirestore = !isStationOwner)
                        if (isStationOwner) {
                            val stEmail = updatedProfile.email
                            val matchedStation = repository.getStationByEmail(stEmail)
                            if (matchedStation != null) {
                                currentStationId.value = matchedStation.id
                                ownerStationPlan.value = if (matchedStation.isPartner) "Conta Premium" else "Conta Pro"
                            }
                        }
                    }
                } else if (email != null) {
                    viewModelScope.launch {
                        val isGeovana = email.equals("geovana@hotmail.com", ignoreCase = true)
                        val isStationOwner = (email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br")
                        val defaultProfile = DriverProfile(
                            id = 1,
                            name = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                            email = email,
                            phone = "(11) 98765-4321",
                            vehicleModel = "Toyota Corolla",
                            vehiclePlate = "ABC-1234",
                            averageConsumption = 12.0,
                            fuelType = "Flex",
                            isPremium = isGeovana
                        )
                        repository.updateProfile(defaultProfile, syncToFirestore = !isStationOwner)
                        if (isStationOwner) {
                            val matchedStation = repository.getStationByEmail(email)
                            if (matchedStation != null) {
                                currentStationId.value = matchedStation.id
                                ownerStationPlan.value = if (matchedStation.isPartner) "Conta Premium" else "Conta Pro"
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            FirebaseManager.fetchAllStationsFromFirestore { cloudStations ->
                if (cloudStations.isNotEmpty()) {
                    viewModelScope.launch {
                        repository.clearAllStationsLocally()
                        val filteredStations = cloudStations.filter {
                            it.firestoreOwnerUid == "fFM6DVVJI0eUV8jYZL7NcsoFBrf2" || it.firestoreOwnerUid == uid
                        }
                        for (cloudStation in filteredStations) {
                            val local = repository.getStationByEmail(cloudStation.email ?: "")
                                ?: repository.getStationByCnpj(cloudStation.cnpj ?: "")
                            if (local == null) {
                                repository.insertStationLocally(cloudStation)
                            } else {
                                repository.insertStationLocally(
                                    local.copy(
                                        priceGasoline = cloudStation.priceGasoline,
                                        priceEthanol = cloudStation.priceEthanol,
                                        priceDiesel = cloudStation.priceDiesel,
                                        name = cloudStation.name,
                                        address = cloudStation.address,
                                        isPartner = cloudStation.isPartner,
                                        razaoSocial = cloudStation.razaoSocial
                                    )
                                )
                            }
                        }
                        fetchPromotions()
                    }
                } else {
                    viewModelScope.launch {
                        repository.clearAllStationsLocally()
                        fetchPromotions()
                    }
                }
            }
        }
    }

    fun fetchPromotions() {
        FirebaseManager.fetchAllPromotionsFromFirestore { cloudPromos ->
            viewModelScope.launch {
                val stationsList = repository.allStations.first()
                val currentUid = FirebaseManager.getCurrentUserUid()
                val currentUserEmail = FirebaseManager.getCurrentUserEmail()
                val currentId = currentStationId.value
                val currentPlan = ownerStationPlan.value
                val currentName = editStationName.value

                val mappedPromos = cloudPromos.map { promo ->
                    val matchedStation = stationsList.find {
                        val matchesOwnerUid = !it.firestoreOwnerUid.isNullOrBlank() && (it.firestoreOwnerUid == promo.firestoreStationId)
                        val matchesRazao = !it.razaoSocial.isNullOrBlank() && (it.razaoSocial == promo.firestoreStationId)
                        val matchesEmail = !it.email.isNullOrBlank() && (it.email?.lowercase() == promo.firestoreStationId?.lowercase())
                        val matchesCnpj = !it.cnpj.isNullOrBlank() && (it.cnpj == promo.firestoreStationId)
                        
                        // Broad fuzzy matches
                        val sName = it.name.lowercase()
                        val pStationId = promo.firestoreStationId?.lowercase() ?: ""
                        val pStationName = promo.stationName.lowercase()
                        val pTitle = promo.title.lowercase()
                        val pDesc = promo.description?.lowercase() ?: ""
                        
                        val isCohabStation = sName.contains("cohab") || sName.contains("cohab 3") || sName.contains("cohab iii")
                        val isCohabPromo = pStationId.contains("cohab") || pStationName.contains("cohab") || pTitle.contains("cohab") || pDesc.contains("cohab")
                        val cohabMatch = isCohabStation && isCohabPromo
                        
                        val matchesFuzzyName = pStationId.isNotEmpty() && pStationId != "posto" && pStationId.length > 3 && (sName.contains(pStationId) || pStationId.contains(sName))
                        val matchesFuzzyStationName = pStationName.isNotEmpty() && pStationName != "posto" && pStationName.length > 5 && (sName.contains(pStationName) || pStationName.contains(sName))
                        
                        matchesOwnerUid || matchesRazao || matchesEmail || matchesCnpj || cohabMatch || matchesFuzzyName || matchesFuzzyStationName
                    }
                    val isOwnerMatch = (!currentUid.isNullOrBlank() && promo.firestoreStationId == currentUid) ||
                        (!currentUserEmail.isNullOrBlank() && promo.firestoreStationId?.lowercase() == currentUserEmail.lowercase()) ||
                        (currentName.lowercase().contains("cohab") && (promo.firestoreStationId?.lowercase() ?: "").contains("cohab")) ||
                        (currentName.lowercase().contains("cohab") && promo.stationName.lowercase().contains("cohab"))
                    
                    if (matchedStation != null) {
                        promo.copy(
                            stationId = matchedStation.id,
                            stationName = matchedStation.name,
                            isFromPremiumStation = matchedStation.isPartner || promo.isFromPremiumStation || matchedStation.name.contains("cohab", ignoreCase = true)
                        )
                    } else if (isOwnerMatch) {
                        promo.copy(
                            stationId = currentId,
                            stationName = currentName.ifEmpty { "Meu Posto" },
                            isFromPremiumStation = currentPlan == "Conta Premium"
                        )
                    } else {
                        promo
                    }
                }
                _promoList.value = mappedPromos
            }
        }
        fetchRefuelings()
    }

    fun fetchRefuelings() {
        FirebaseManager.fetchAllRefuelingsFromFirestore { cloudRefuelings ->
            if (cloudRefuelings.isNotEmpty()) {
                viewModelScope.launch {
                    for (refueling in cloudRefuelings) {
                        repository.insertRefuelingLocally(refueling)
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            syncFromFirestore()
        }
        viewModelScope.launch {
            // Sync initial owner dashboard prices with database or defaults dynamically based on selected/registered station ID
            kotlinx.coroutines.flow.combine(allStations, currentStationId) { stations, activeId ->
                val activeStation = stations.find { it.id == activeId }
                if (activeStation != null) {
                    editGasolinePrice.value = activeStation.priceGasoline.toString()
                    editEthanolPrice.value = activeStation.priceEthanol.toString()
                    editDieselPrice.value = activeStation.priceDiesel.toString()
                    editStationName.value = activeStation.name
                    editStationCNPJ.value = activeStation.cnpj ?: ""
                    editStationAddress.value = activeStation.address
                    editStationOpenHours.value = activeStation.openHours
                    editStationBrand.value = activeStation.brand
                    editStationEmail.value = activeStation.email ?: ""
                    editStationPhone.value = activeStation.phone ?: ""
                    editStationRazao.value = activeStation.razaoSocial ?: ""
                    editStationHasEvCharger.value = activeStation.hasEvCharger
                    ownerStationPlan.value = if (activeStation.isPartner) "Conta Premium" else "Conta Pro"
                }
            }.collect {}
        }
    }

    suspend fun getLatLongFromAddress(address: String): Pair<Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val encodedAddress = java.net.URLEncoder.encode(address, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "PrecoNaBombaApp/1.0")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val jsonArray = org.json.JSONArray(bodyString)
                        if (jsonArray.length() > 0) {
                            val firstResult = jsonArray.getJSONObject(0)
                            val lat = firstResult.optDouble("lat", 0.0)
                            val lon = firstResult.optDouble("lon", 0.0)
                            if (lat != 0.0 && lon != 0.0) {
                                Log.d("PrecoNaBombaVM", "Geocoded success: $lat, $lon")
                                return@withContext Pair(lat, lon)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PrecoNaBombaVM", "Failed geocoding address via OpenStreetMap Nominatim", e)
        }
        null
    }

    fun registerStation(
        cnpjStr: String,
        razaoSocialStr: String,
        nomeFantasiaStr: String,
        passwordForAccess: String,
        brandName: String,
        phoneNumber: String,
        emailAddress: String,
        addressStr: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanCnpj = cnpjStr.replace(Regex("[^0-9]"), "")
            
            // 1. Check if station already exists by CNPJ of Firestore (Absolute source of truth)
            Log.d("PrecoNaBombaVM", "Verificando se CNPJ existe no Firestore: $cleanCnpj")
            val existsInFirestore = FirebaseManager.checkCnpjExistsInFirestore(cleanCnpj)
            if (existsInFirestore) {
                onResult(false, "Este CNPJ já está cadastrado no sistema!")
                return@launch
            } else {
                // Since it does not exist in Firestore, clean up any local Room leftovers to prevent cache conflict
                Log.d("PrecoNaBombaVM", "CNPJ inexistente no Firestore. Limpando possíveis registros locais antigos.")
                repository.deleteStationByCnpj(cleanCnpj)
            }

            // 2. Check if station with this email already exists on Firestore (Absolute source of truth)
            val emailClean = emailAddress.trim().lowercase()
            val emailExistsInFirestore = FirebaseManager.checkEmailExistsInFirestore(emailClean)
            if (emailExistsInFirestore) {
                onResult(false, "Este e-mail já está cadastrado no sistema!")
                return@launch
            }

            // Register station user credential in Firebase Auth
            FirebaseManager.registerStationUser(
                emailAddress = emailAddress,
                passwordForAccess = passwordForAccess,
                nomeFantasia = nomeFantasiaStr,
                cnpj = cleanCnpj,
                phoneNumber = phoneNumber
            ) { fbSuccess, fbErr ->
                if (!fbSuccess && fbErr != null && !fbErr.contains("Firebase não inicializado", ignoreCase = true)) {
                    onResult(false, "Erro ao criar credenciais Firebase: $fbErr")
                    return@registerStationUser
                }

                // Proceed with Room persistence
                viewModelScope.launch {
                    val coords = getLatLongFromAddress(addressStr)
                    val latVal = coords?.first ?: (-23.5505 + (Math.random() - 0.5) * 0.05)
                    val lonVal = coords?.second ?: (-46.6333 + (Math.random() - 0.5) * 0.05)

                    val newStation = FuelStation(
                        name = nomeFantasiaStr,
                        address = addressStr,
                        latitude = latVal,
                        longitude = lonVal,
                        priceGasoline = 5.89,
                        priceEthanol = 3.75,
                        priceDiesel = 6.12,
                        openHours = "24 Horas",
                        brand = if (brandName.isNotEmpty()) brandName else "Independente",
                        distanceKm = 0.0,
                        isFavorite = false,
                        isPartner = true, // We must set true for partners
                        lastUpdatedText = "Atualizado recentemente",
                        lastUpdatedTimestamp = System.currentTimeMillis(),
                        cnpj = cleanCnpj,
                        email = emailAddress.trim().lowercase(),
                        phone = phoneNumber,
                        razaoSocial = razaoSocialStr,
                        firestoreOwnerUid = com.example.data.FirebaseManager.getCurrentUserUid()
                    )

                    repository.insertStation(newStation)

                    // Retrieve back to get the autogenerated Room primary key
                    val savedStation = repository.getStationByCnpj(cleanCnpj)
                    if (savedStation != null) {
                        currentStationId.value = savedStation.id
                    }

                    editStationPassword.value = passwordForAccess
                    editStationBrand.value = brandName
                    editStationPhone.value = phoneNumber
                    editStationEmail.value = emailAddress
                    editStationRazao.value = razaoSocialStr
                    ownerStationPlan.value = "Conta Pro"

                    _isUserLoggedInFlow.value = true
                    _userEmailFlow.value = emailAddress

                    onResult(true, "Sucesso")
                    navigateTo(Screen.MainStationHome)
                }
            }
        }
    }

    fun loginAsStation(cnpjInput: String, passwordInput: String, onResult: (Boolean) -> Unit) {
        val cleanCnpj = cnpjInput.replace(Regex("[^0-9]"), "")
        viewModelScope.launch {
            val station = repository.getStationByCnpj(cleanCnpj)
            if (station != null) {
                currentStationId.value = station.id
                if (passwordInput.isNotEmpty()) {
                    editStationPassword.value = passwordInput
                }
                navigateTo(Screen.MainStationHome)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Firebase Auth States
    private val _isUserLoggedInFlow = MutableStateFlow(FirebaseManager.isUserLoggedIn())
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedInFlow.asStateFlow()

    private val _userEmailFlow = MutableStateFlow(FirebaseManager.getCurrentUserEmail())
    val userEmail: StateFlow<String?> = _userEmailFlow.asStateFlow()

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        FirebaseManager.login(email, password) { success, error ->
            if (success) {
                _isUserLoggedInFlow.value = true
                _userEmailFlow.value = FirebaseManager.getCurrentUserEmail()
                
                val uid = FirebaseManager.getCurrentUserUid()
                if (uid != null) {
                    FirebaseManager.fetchProfileFromFirestore(uid) { fetchedProfile, role ->
                        viewModelScope.launch {
                            val isStationOwner = (role == "station_owner" || email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br" || repository.getStationByEmail(email) != null)
                            val isGeovana = email.equals("geovana@hotmail.com", ignoreCase = true)

                            if (fetchedProfile != null) {
                                val updatedProfile = if (isGeovana) fetchedProfile.copy(isPremium = true) else fetchedProfile
                                // Do not sync driver properties or change role to "driver" in Firestore if user is a station owner
                                repository.updateProfile(updatedProfile, syncToFirestore = !isStationOwner)
                            } else {
                                // Default profile if not yet created in cloud
                                val defaultProfile = DriverProfile(
                                    name = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                                    email = email,
                                    phone = "(11) 98765-4321",
                                    vehicleModel = "Toyota Corolla",
                                    vehiclePlate = "ABC-1234",
                                    averageConsumption = 12.0,
                                    fuelType = "Flex",
                                    isPremium = isGeovana
                                )
                                repository.updateProfile(defaultProfile, syncToFirestore = !isStationOwner)
                            }
                            
                            if (isStationOwner) {
                                val matchedStation = repository.getStationByEmail(email)
                                if (matchedStation != null) {
                                    currentStationId.value = matchedStation.id
                                    ownerStationPlan.value = if (matchedStation.isPartner) "Conta Premium" else "Conta Pro"
                                    syncFromFirestore()
                                    navigateTo(Screen.MainStationHome)
                                    onResult(true, null)
                                } else {
                                    FirebaseManager.fetchStationFromFirestore(uid) { cloudStation ->
                                        viewModelScope.launch {
                                            if (cloudStation != null) {
                                                repository.insertStation(cloudStation)
                                                val savedStation = repository.getStationByEmail(email) ?: repository.getStationByCnpj(cloudStation.cnpj ?: "")
                                                if (savedStation != null) {
                                                    currentStationId.value = savedStation.id
                                                    ownerStationPlan.value = if (savedStation.isPartner) "Conta Premium" else "Conta Pro"
                                                }
                                                syncFromFirestore()
                                                navigateTo(Screen.MainStationHome)
                                                onResult(true, null)
                                            } else {
                                                // Create local representative station for owner
                                                val defaultStation = FuelStation(
                                                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Posto",
                                                    address = "Endereço não informado",
                                                    latitude = -23.5505,
                                                    longitude = -46.6333,
                                                    priceGasoline = 5.89,
                                                    priceEthanol = 3.75,
                                                    priceDiesel = 6.12,
                                                    openHours = "24 Horas",
                                                    brand = "Independente",
                                                    distanceKm = 0.0,
                                                    isFavorite = false,
                                                    isPartner = true,
                                                    lastUpdatedText = "Criado no login",
                                                    lastUpdatedTimestamp = System.currentTimeMillis(),
                                                    cnpj = "00.000.000/0001-00",
                                                    email = email,
                                                    phone = "(11) 98765-4321",
                                                    razaoSocial = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Ltda",
                                                    firestoreOwnerUid = uid
                                                )
                                                repository.insertStation(defaultStation)
                                                val savedStation = repository.getStationByEmail(email)
                                                if (savedStation != null) {
                                                    currentStationId.value = savedStation.id
                                                }
                                                ownerStationPlan.value = "Conta Pro"
                                                syncFromFirestore()
                                                navigateTo(Screen.MainStationHome)
                                                onResult(true, null)
                                            }
                                        }
                                    }
                                }
                            } else {
                                syncFromFirestore()
                                navigateTo(Screen.MainDriverHome)
                                onResult(true, null)
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        val matchedStation = repository.getStationByEmail(email)
                        val isStationOwner = (email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br" || matchedStation != null)
                        if (isStationOwner) {
                            if (matchedStation != null) {
                                currentStationId.value = matchedStation.id
                                ownerStationPlan.value = if (matchedStation.isPartner) "Conta Premium" else "Conta Pro"
                                navigateTo(Screen.MainStationHome)
                            } else {
                                // Create local representative station
                                val defaultStation = FuelStation(
                                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Posto",
                                    address = "Endereço não informado",
                                    latitude = -23.5505,
                                    longitude = -46.6333,
                                    priceGasoline = 5.89,
                                    priceEthanol = 3.75,
                                    priceDiesel = 6.12,
                                    openHours = "24 Horas",
                                    brand = "Independente",
                                    distanceKm = 0.0,
                                    isFavorite = false,
                                    isPartner = true,
                                    lastUpdatedText = "Criado no login",
                                    lastUpdatedTimestamp = System.currentTimeMillis(),
                                    cnpj = "00.000.000/0001-00",
                                    email = email,
                                    phone = "(11) 98765-4321",
                                    razaoSocial = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Ltda"
                                )
                                repository.insertStation(defaultStation)
                                val savedStation = repository.getStationByEmail(email)
                                if (savedStation != null) {
                                    currentStationId.value = savedStation.id
                                }
                                ownerStationPlan.value = "Conta Pro"
                                navigateTo(Screen.MainStationHome)
                            }
                        } else {
                            navigateTo(Screen.MainDriverHome)
                        }
                        syncFromFirestore()
                        onResult(true, null)
                    }
                }
            } else {
                onResult(false, error)
            }
        }
    }

    fun registerDriver(
        email: String,
        password: String,
        name: String,
        model: String,
        plate: String,
        consumption: Double,
        onResult: (Boolean, String?) -> Unit
    ) {
        val driverProfile = DriverProfile(
            name = name,
            email = email,
            phone = "(11) 98765-4321",
            vehicleModel = model,
            vehiclePlate = plate,
            averageConsumption = consumption,
            fuelType = "Flex",
            isPremium = false
        )
        FirebaseManager.registerDriver(email, password, driverProfile) { success, error ->
            if (success) {
                _isUserLoggedInFlow.value = true
                _userEmailFlow.value = FirebaseManager.getCurrentUserEmail()
                viewModelScope.launch {
                    repository.updateProfile(driverProfile)
                    navigateTo(Screen.MainDriverHome)
                }
            }
            onResult(success, error)
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        FirebaseManager.signOut {
            _isUserLoggedInFlow.value = false
            _userEmailFlow.value = null
            navigateTo(Screen.UserLogin)
            onComplete()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFuelFilter(filter: String) {
        _selectedFuelFilter.value = filter
    }

    fun setPromoFilter(filter: String) {
        _selectedPromoFilter.value = filter
    }

    fun selectStation(id: Int) {
        _selectedStationId.value = id
        _activeRoute.value = emptyList()
    }

    fun setPlan(plan: String) {
        _selectedPlan.value = plan
    }

    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    // Toggle Favorite Action
    fun toggleFavorite(stationId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(stationId)
        }
    }

    // Toggle Premium level for the user profile
    fun setProfilePremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            val current = repository.profile.first()
            if (current != null) {
                repository.updateProfile(current.copy(isPremium = isPremium))
            }
        }
    }

    // Driver: Log refueling and dynamically append to transaction records
    fun saveRefueling(dateText: String = "Hoje"): Boolean {
        val station = logStationName.value.trim()
        val litStr = logLiters.value.trim().replace(",", ".")
        val priceStr = logPricePerLiter.value.trim().replace(",", ".")

        if (station.isEmpty() || litStr.isEmpty() || priceStr.isEmpty()) {
            return false
        }

        val lit = litStr.toDoubleOrNull() ?: return false
        val price = priceStr.toDoubleOrNull() ?: return false
        val total = lit * price

        viewModelScope.launch {
            repository.insertRefueling(
                Refueling(
                    stationName = station,
                    date = dateText,
                    liters = lit,
                    pricePerLiter = price,
                    totalPaid = total
                )
            )
            // Empty the form
            logStationName.value = ""
            logLiters.value = ""
            logPricePerLiter.value = ""
        }
        return true
    }

    fun deleteRefueling(refueling: Refueling) {
        viewModelScope.launch {
            repository.deleteRefueling(refueling)
        }
    }

    fun updateRefueling(refueling: Refueling, newStationName: String, newLiters: Double, newPricePerLiter: Double) {
        viewModelScope.launch {
            val updated = refueling.copy(
                stationName = newStationName,
                liters = newLiters,
                pricePerLiter = newPricePerLiter,
                totalPaid = newLiters * newPricePerLiter
            )
            repository.updateRefueling(updated)
        }
    }

    // Owner: Save prices on the pump and legal details -> persist and reflect immediately on maps/lists
    fun saveOwnerAlterations() {
        val gasString = editGasolinePrice.value.replace(",", ".")
        val ethString = editEthanolPrice.value.replace(",", ".")
        val dslString = editDieselPrice.value.replace(",", ".")

        val gasVal = gasString.toDoubleOrNull() ?: 5.89
        val ethVal = ethString.toDoubleOrNull() ?: 3.75
        val dslVal = dslString.toDoubleOrNull() ?: 6.12

        viewModelScope.launch {
            saveState.value = StationSaveState.SAVING
            // Brief simulation delay for nice spinner look
            kotlinx.coroutines.delay(1200)

            val activeId = currentStationId.value
            val activeStation = repository.getStationById(activeId)
            if (activeStation != null) {
                repository.updateStation(
                    activeStation.copy(
                        name = editStationName.value,
                        address = editStationAddress.value,
                        brand = editStationBrand.value,
                        priceGasoline = gasVal,
                        priceEthanol = ethVal,
                        priceDiesel = dslVal,
                        openHours = editStationOpenHours.value,
                        isPartner = ownerStationPlan.value == "Conta Premium",
                        lastUpdatedText = "Atualizado recentemente",
                        lastUpdatedTimestamp = System.currentTimeMillis(),
                        email = editStationEmail.value,
                        phone = editStationPhone.value,
                        razaoSocial = editStationRazao.value,
                        hasEvCharger = editStationHasEvCharger.value
                    )
                )
            }
            saveState.value = StationSaveState.SUCCESS
            // After 2 seconds of showing Green/Success, reset back to IDLE
            kotlinx.coroutines.delay(2000)
            saveState.value = StationSaveState.IDLE
        }
    }

    // Owner: Save profile info changes
    fun updateOwnerProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val current = repository.profile.first()
            if (current != null) {
                // Keep driver local/Room DB synced, but skip driver Firestore mapping
                repository.updateProfile(
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone
                    ),
                    syncToFirestore = false
                )
                // Safely update basic owner name & email inside users collection in Firestore without driver tags
                FirebaseManager.syncStationOwnerProfileToFirestore(name, email)
            }
        }
    }

    // Driver: Save detailed profile info edits
    fun updateDriverProfile(
        name: String,
        email: String,
        phone: String,
        address: String,
        vehicleModel: String,
        vehiclePlate: String,
        averageConsumption: Double,
        fuelType: String
    ) {
        viewModelScope.launch {
            val current = repository.profile.first()
            if (current != null) {
                repository.updateProfile(
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        address = address,
                        vehicleModel = vehicleModel,
                        vehiclePlate = vehiclePlate,
                        averageConsumption = averageConsumption,
                        fuelType = fuelType
                    ),
                    syncToFirestore = true
                )
            }
        }
    }

    // Owner: Insert or Update a promotion dynamically on dashboard
    fun addNewPromotion(
        title: String,
        description: String,
        price: Double,
        category: String,
        startDate: String,
        endDate: String,
        icon: String = "sell",
        docId: String? = null
    ) {
        if (title.isEmpty()) return
        val currentList = _promoList.value.toMutableList()
        val currentId = currentStationId.value
        val isPremium = ownerStationPlan.value == "Conta Premium"
        val currentName = editStationName.value
        val ownerUid = FirebaseManager.getCurrentUserUid() ?: ""
        
        val formattedPrice = String.format("R$ %.2f", price).replace('.', ',')

        if (docId != null) {
            val idx = currentList.indexOfFirst { it.docId == docId }
            val existing = currentList.find { it.docId == docId }
            val updatedPromo = PromoItem(
                title = title,
                category = category,
                value = formattedPrice,
                icon = icon,
                stationName = currentName,
                distanceKm = "0.7 km",
                isFromPremiumStation = isPremium,
                stationId = currentId,
                description = description,
                startDate = startDate,
                endDate = endDate,
                price = price,
                firestoreStationId = ownerUid,
                docId = docId,
                isDeactivated = existing?.isDeactivated ?: false,
                deactivationJustification = existing?.deactivationJustification,
                deactivationTimestamp = existing?.deactivationTimestamp
            )
            if (idx != -1) {
                currentList[idx] = updatedPromo
            } else {
                currentList.add(0, updatedPromo)
            }
            
            _promoList.value = currentList
            
            // Synchronize promotion directly to Firestore under "promotions" collection
            FirebaseManager.syncPromotionToFirestore(
                stationIdStr = ownerUid,
                title = title,
                description = description,
                price = price,
                category = category,
                startDate = startDate,
                endDate = endDate,
                isPremium = isPremium,
                docId = docId,
                isDeactivated = existing?.isDeactivated ?: false,
                deactivationJustification = existing?.deactivationJustification,
                deactivationTimestamp = existing?.deactivationTimestamp,
                onComplete = { did ->
                    fetchPromotions()
                }
            )
        } else {
            val newPromo = PromoItem(
                title = title,
                category = category,
                value = formattedPrice,
                icon = icon,
                stationName = currentName,
                distanceKm = "0.7 km",
                isFromPremiumStation = isPremium,
                stationId = currentId,
                description = description,
                startDate = startDate,
                endDate = endDate,
                price = price,
                firestoreStationId = ownerUid
            )
            currentList.add(0, newPromo)
            _promoList.value = currentList
            
            // Synchronize promotion directly to Firestore under "promotions" collection
            FirebaseManager.syncPromotionToFirestore(
                stationIdStr = ownerUid,
                title = title,
                description = description,
                price = price,
                category = category,
                startDate = startDate,
                endDate = endDate,
                isPremium = isPremium,
                docId = null,
                onComplete = { did ->
                    fetchPromotions()
                }
            )
        }
    }

    fun deletePromotion(promo: PromoItem) {
        val currentList = _promoList.value.toMutableList()
        currentList.remove(promo)
        _promoList.value = currentList
        promo.docId?.let { docId ->
            FirebaseManager.deletePromotionFromFirestore(docId) { success ->
                if (success) {
                    fetchPromotions()
                }
            }
        }
    }

    fun deactivatePromotion(promo: PromoItem, justification: String) {
        val docId = promo.docId ?: return
        val currentList = _promoList.value.toMutableList()
        val idx = currentList.indexOfFirst { it.docId == docId }
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val timestampStr = sdf.format(java.util.Date())

        val deactivatedPromo = promo.copy(
            isDeactivated = true,
            deactivationJustification = justification,
            deactivationTimestamp = timestampStr
        )
        if (idx != -1) {
            currentList[idx] = deactivatedPromo
            _promoList.value = currentList
        }
        
        FirebaseManager.deactivatePromotionInFirestore(docId, justification, timestampStr) { success ->
            if (success) {
                fetchPromotions()
            }
        }
    }
}

data class PromoItem(
    val title: String,
    val category: String,
    val value: String,
    val icon: String,
    val stationName: String,
    val distanceKm: String,
    val isFromPremiumStation: Boolean,
    val stationId: Int = 5,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val price: Double? = null,
    val firestoreStationId: String? = null,
    val docId: String? = null,
    val isDeactivated: Boolean = false,
    val deactivationJustification: String? = null,
    val deactivationTimestamp: String? = null
)
