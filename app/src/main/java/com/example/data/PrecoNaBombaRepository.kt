package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrecoNaBombaRepository(private val dao: PrecoNaBombaDao) {

    private val _allStations = MutableStateFlow<List<FuelStation>>(emptyList())
    val allStations: Flow<List<FuelStation>> = _allStations.asStateFlow()

    private val _allRefuelings = MutableStateFlow<List<Refueling>>(emptyList())
    val allRefuelings: Flow<List<Refueling>> = _allRefuelings.asStateFlow()

    private val _profile = MutableStateFlow<DriverProfile?>(null)
    val profile: Flow<DriverProfile?> = _profile.asStateFlow()

    val favoriteStations: Flow<List<FuelStation>> = _allStations.map { stations ->
        stations.filter { it.isFavorite }
    }

    suspend fun getStationByCnpj(cnpj: String): FuelStation? {
        val cleanCnpj = cnpj.replace(Regex("[^0-9]"), "")
        return _allStations.value.find { (it.cnpj ?: "").replace(Regex("[^0-9]"), "") == cleanCnpj }
    }

    suspend fun deleteStationByCnpj(cnpj: String) {
        val cleanCnpj = cnpj.replace(Regex("[^0-9]"), "")
        _allStations.update { list -> list.filterNot { (it.cnpj ?: "").replace(Regex("[^0-9]"), "") == cleanCnpj } }
    }

    suspend fun getStationByEmail(email: String): FuelStation? {
        val trimmed = email.trim().lowercase()
        return _allStations.value.find { (it.email ?: "").trim().lowercase() == trimmed }
    }

    suspend fun getStationById(id: Int): FuelStation? {
        return _allStations.value.find { it.id == id }
    }

    // Seeds initial data if not already present
    suspend fun seedDatabaseIfNeeded() {
        // Checking if profile exists, if empty, seed default profile
        val existingProfile = _profile.value
        if (existingProfile == null) {
            val currentUserEmail = com.example.data.FirebaseManager.getCurrentUserEmail()
            val defaultEmail = currentUserEmail ?: "joao.silva@email.com"
            val isGeovana = defaultEmail.equals("geovana@hotmail.com", ignoreCase = true)
            val defaultName = if (currentUserEmail != null) defaultEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } else "João Silva"

            _profile.value = DriverProfile(
                id = 1,
                name = defaultName,
                email = defaultEmail,
                phone = "(11) 98765-4321",
                vehicleModel = "Toyota Corolla",
                vehiclePlate = "ABC-1234",
                averageConsumption = 12.0,
                fuelType = "Flex",
                isPremium = isGeovana
            )
        }

        // Checking if stations exist, if empty, seed default stations
        val stationsList = _allStations.value
        if (stationsList.isEmpty()) {
            _allStations.value = listOf(
                FuelStation(
                    id = 1,
                    name = "Posto Shell - Av. Central",
                    address = "Av. Central, 1200 - Centro",
                    latitude = -23.5512,
                    longitude = -46.6340,
                    priceGasoline = 5.89,
                    priceEthanol = 3.95,
                    priceDiesel = 6.12,
                    openHours = "Aberto 24H",
                    brand = "Shell",
                    distanceKm = 1.2,
                    isFavorite = false,
                    lastUpdatedText = "Atualizado há 15 min",
                    cnpj = "55555555000155"
                ),
                FuelStation(
                    id = 2,
                    name = "Ipiranga - Jd. das Flores",
                    address = "Av. das Flores, 450 - Jd. das Flores",
                    latitude = -23.5530,
                    longitude = -46.6322,
                    priceGasoline = 5.98,
                    priceEthanol = 3.85,
                    priceDiesel = 5.99,
                    openHours = "Aberto até 22:00",
                    brand = "Ipiranga",
                    distanceKm = 2.8,
                    isFavorite = true,
                    lastUpdatedText = "Atualizado há 1 h",
                    cnpj = "44444444000144"
                ),
                FuelStation(
                    id = 3,
                    name = "Petrobras - BR-101",
                    address = "Rodovia BR-101, Km 220",
                    latitude = -23.5480,
                    longitude = -46.6355,
                    priceGasoline = 6.05,
                    priceEthanol = 4.12,
                    priceDiesel = 5.85,
                    openHours = "Aberto 24H",
                    brand = "Petrobras",
                    distanceKm = 3.5,
                    isFavorite = false,
                    lastUpdatedText = "Atualizado há 3 h",
                    cnpj = "33333333000133"
                ),
                FuelStation(
                    id = 4,
                    name = "Posto Shell - Marginal Tiete",
                    address = "Av. Otaviano Alves de Lima, 1200",
                    latitude = -23.5395,
                    longitude = -46.6432,
                    priceGasoline = 5.89,
                    priceEthanol = 3.89,
                    priceDiesel = 6.09,
                    openHours = "Aberto",
                    brand = "Shell",
                    distanceKm = 4.5,
                    isFavorite = false,
                    lastUpdatedText = "Atualizado recentemente",
                    cnpj = "22222222000122"
                ),
                FuelStation(
                    id = 5,
                    name = "Posto Estrela do Sul",
                    address = "Av. das Nações, 1500 - São Paulo, SP",
                    latitude = -23.5505,
                    longitude = -46.6333,
                    priceGasoline = 5.89,
                    priceEthanol = 3.75,
                    priceDiesel = 6.12,
                    openHours = "24 Horas",
                    brand = "Ipiranga",
                    distanceKm = 1.8,
                    isFavorite = true,
                    isPartner = true,
                    lastUpdatedText = "Atualizado há 10 min",
                    cnpj = "12345678000199",
                    email = "contato@estreladosul.com.br",
                    phone = "(11) 98765-4321"
                )
            )
        }

        // Checking if refueling logs exist, if empty, seed default logs
        val refuelingList = _allRefuelings.value
        if (refuelingList.isEmpty()) {
            _allRefuelings.value = listOf(
                Refueling(
                    id = 1,
                    stationName = "Posto Shell Central",
                    date = "15/10/2023 • 14:30",
                    liters = 45.45,
                    pricePerLiter = 5.50,
                    totalPaid = 250.00
                ),
                Refueling(
                    id = 2,
                    stationName = "Ipiranga Marginal",
                    date = "02/10/2023 • 09:15",
                    liters = 32.10,
                    pricePerLiter = 5.78,
                    totalPaid = 185.50
                ),
                Refueling(
                    id = 3,
                    stationName = "Posto Petrobras Av. 1",
                    date = "20/09/2023 • 18:45",
                    liters = 40.00,
                    pricePerLiter = 5.25,
                    totalPaid = 210.00
                )
            )
        }
    }

    // Helper functions
    suspend fun updateStation(station: FuelStation) {
        _allStations.update { list ->
            list.map { if (it.id == station.id || (station.cnpj != null && it.cnpj == station.cnpj)) station else it }
        }
        com.example.data.FirebaseManager.syncStationPriceToFirestore(station)
    }

    suspend fun insertStationLocally(station: FuelStation) {
        _allStations.update { list ->
            val alreadyExists = list.any { it.cnpj == station.cnpj || it.email == station.email }
            if (alreadyExists) {
                list.map { if (it.cnpj == station.cnpj || it.email == station.email) station.copy(id = it.id) else it }
            } else {
                val nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
                list + station.copy(id = nextId)
            }
        }
    }

    suspend fun insertStationsLocally(stations: List<FuelStation>) {
        stations.forEach { insertStationLocally(it) }
    }

    suspend fun insertStation(station: FuelStation) {
        insertStationLocally(station)
        val stationToSync = getStationByCnpj(station.cnpj ?: "") ?: station
        com.example.data.FirebaseManager.syncStationPriceToFirestore(stationToSync)
    }

    suspend fun insertStations(stations: List<FuelStation>) {
        stations.forEach { insertStationLocally(it) }
        _allStations.value.forEach { station ->
            com.example.data.FirebaseManager.syncStationPriceToFirestore(station)
        }
    }

    suspend fun insertRefueling(refueling: Refueling) {
        _allRefuelings.update { list ->
            val nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
            list + refueling.copy(id = nextId)
        }
        com.example.data.FirebaseManager.syncRefuelingToFirestore(refueling)
    }

    suspend fun insertRefuelingLocally(refueling: Refueling) {
        _allRefuelings.update { list ->
            val exists = list.any { it.stationName == refueling.stationName && it.date == refueling.date && it.totalPaid == refueling.totalPaid }
            if (exists) {
                list
            } else {
                val nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
                list + refueling.copy(id = nextId)
            }
        }
    }

    suspend fun updateProfile(profile: DriverProfile, syncToFirestore: Boolean = true) {
        _profile.value = profile
        if (syncToFirestore) {
            com.example.data.FirebaseManager.syncProfileToFirestore(profile)
        }
    }

    suspend fun toggleFavorite(stationId: Int) {
        _allStations.update { list ->
            list.map { if (it.id == stationId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    suspend fun updateStationPrices(stationId: Int, gasoline: Double, ethanol: Double, diesel: Double) {
        val station = getStationById(stationId)
        if (station != null) {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            val timeText = dateFormat.format(Date(timestamp))
            updateStation(
                station.copy(
                    priceGasoline = gasoline,
                    priceEthanol = ethanol,
                    priceDiesel = diesel,
                    lastUpdatedText = "Atualizado em $timeText"
                )
            )
        }
    }
}
