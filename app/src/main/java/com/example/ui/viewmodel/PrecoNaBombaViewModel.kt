package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
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
    object StationProfileArea : Screen // Station legal info (CNPJ, Razão Social, logout)
}

class PrecoNaBombaViewModel(private val repository: PrecoNaBombaRepository) : ViewModel() {

    // Main Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.OnboardingRoleSelection)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Interactive state flows from Room Database
    val allStations: StateFlow<List<FuelStation>> = repository.allStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteStations: StateFlow<List<FuelStation>> = repository.favoriteStations
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

    // Dynamic Station Promotions list managed locally
    private val _promoList = MutableStateFlow(
        listOf(
            PromoItem("Combo Café + Pão de Queijo", "Loja de Conveniência", "R$ 9,90", "shopping_basket"),
            PromoItem("Troca de Óleo Shell Helix", "Serviços", "15% OFF", "build"),
            PromoItem("Pernoite para Caminhoneiros", "Hospedaria", "Grátis*", "bed")
        )
    )
    val promoList: StateFlow<List<PromoItem>> = _promoList.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            // Sync initial owner dashboard prices with database or defaults
            allStations.collect { stations ->
                val estreladoSul = stations.find { it.name.contains("Estrela", ignoreCase = true) }
                if (estreladoSul != null) {
                    editGasolinePrice.value = estreladoSul.priceGasoline.toString()
                    editEthanolPrice.value = estreladoSul.priceEthanol.toString()
                    editDieselPrice.value = estreladoSul.priceDiesel.toString()
                    editStationAddress.value = estreladoSul.address
                }
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
                    FirebaseManager.fetchProfileFromFirestore(uid) { fetchedProfile ->
                        viewModelScope.launch {
                            if (fetchedProfile != null) {
                                repository.updateProfile(fetchedProfile)
                            } else {
                                // Default profile if not yet created in cloud
                                val defaultProfile = DriverProfile(
                                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                                    email = email,
                                    phone = "(11) 98765-4321",
                                    vehicleModel = "Toyota Corolla",
                                    vehiclePlate = "ABC-1234",
                                    averageConsumption = 12.0,
                                    fuelType = "Flex",
                                    isPremium = false
                                )
                                repository.updateProfile(defaultProfile)
                            }
                            
                            if (email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br") {
                                navigateTo(Screen.MainStationHome)
                            } else {
                                navigateTo(Screen.MainDriverHome)
                            }
                            onResult(true, null)
                        }
                    }
                } else {
                    if (email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br") {
                        navigateTo(Screen.MainStationHome)
                    } else {
                        navigateTo(Screen.MainDriverHome)
                    }
                    onResult(true, null)
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
            navigateTo(Screen.OnboardingRoleSelection)
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
        val litStr = logLiters.value.trim()
        val priceStr = logPricePerLiter.value.trim()

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

    // Owner: Save prices on the pump and legal details -> persist and reflect immediately on maps/lists
    fun saveOwnerAlterations() {
        val gasVal = editGasolinePrice.value.toDoubleOrNull() ?: 5.89
        val ethVal = editEthanolPrice.value.toDoubleOrNull() ?: 3.75
        val dslVal = editDieselPrice.value.toDoubleOrNull() ?: 6.12

        viewModelScope.launch {
            // Find Posto Estrela do Sul and update its prices
            val stations = allStations.value
            val estreladoSul = stations.find { it.name.contains("Estrela", ignoreCase = true) }
            if (estreladoSul != null) {
                repository.updateStation(
                    estreladoSul.copy(
                        name = editStationName.value,
                        address = editStationAddress.value,
                        priceGasoline = gasVal,
                        priceEthanol = ethVal,
                        priceDiesel = dslVal,
                        openHours = editStationOpenHours.value
                    )
                )
            }
        }
    }

    // Owner: Save profile info changes
    fun updateOwnerProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val current = repository.profile.first()
            if (current != null) {
                repository.updateProfile(
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone
                    )
                )
            }
        }
    }

    // Owner: Insert a new promotion dynamically on dashboard
    fun addNewPromotion(title: String, category: String, value: String, icon: String = "sell") {
        if (title.isEmpty() || value.isEmpty()) return
        val currentList = _promoList.value.toMutableList()
        currentList.add(0, PromoItem(title, category, value, icon))
        _promoList.value = currentList
    }
}

data class PromoItem(
    val title: String,
    val category: String,
    val value: String,
    val icon: String
)
