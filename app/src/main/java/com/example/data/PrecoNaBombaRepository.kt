package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrecoNaBombaRepository(private val dao: PrecoNaBombaDao) {

    val allStations: Flow<List<FuelStation>> = dao.getAllStations()
    val favoriteStations: Flow<List<FuelStation>> = dao.getFavoriteStations()
    val allRefuelings: Flow<List<Refueling>> = dao.getAllRefuelings()
    val profile: Flow<DriverProfile?> = dao.getProfileFlow()

    // Seeds initial data if not already present
    suspend fun seedDatabaseIfNeeded() {
        // Checking if profile exists, if empty, seed default profile
        val existingProfile = dao.getProfile()
        if (existingProfile == null) {
            dao.insertProfile(
                DriverProfile(
                    id = 1,
                    name = "João Silva",
                    email = "joao.silva@email.com",
                    phone = "(11) 98765-4321",
                    vehicleModel = "Toyota Corolla",
                    vehiclePlate = "ABC-1234",
                    averageConsumption = 12.0,
                    fuelType = "Flex",
                    isPremium = false
                )
            )
        }

        // Checking if stations exist, if empty, seed default stations
        val stationsList = dao.getAllStations().first()
        if (stationsList.isEmpty()) {
            dao.insertStations(
                listOf(
                    FuelStation(
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
                        lastUpdatedText = "Atualizado há 15 min"
                    ),
                    FuelStation(
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
                        lastUpdatedText = "Atualizado há 1 h"
                    ),
                    FuelStation(
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
                        lastUpdatedText = "Atualizado há 3 h"
                    ),
                    FuelStation(
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
                        lastUpdatedText = "Atualizado recentemente"
                    ),
                    FuelStation(
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
                        lastUpdatedText = "Atualizado há 10 min"
                    )
                )
            )
        }

        // Checking if refueling logs exist, if empty, seed default logs
        val refuelingList = dao.getAllRefuelings().first()
        if (refuelingList.isEmpty()) {
            dao.insertRefueling(
                Refueling(
                    stationName = "Posto Shell Central",
                    date = "15/10/2023 • 14:30",
                    liters = 45.45,
                    pricePerLiter = 5.50,
                    totalPaid = 250.00
                )
            )
            dao.insertRefueling(
                Refueling(
                    stationName = "Ipiranga Marginal",
                    date = "02/10/2023 • 09:15",
                    liters = 32.10,
                    pricePerLiter = 5.78,
                    totalPaid = 185.50
                )
            )
            dao.insertRefueling(
                Refueling(
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
        dao.updateStation(station)
    }

    suspend fun insertRefueling(refueling: Refueling) {
        dao.insertRefueling(refueling)
    }

    suspend fun updateProfile(profile: DriverProfile) {
        dao.insertProfile(profile)
    }

    suspend fun toggleFavorite(stationId: Int) {
        val station = dao.getStationById(stationId)
        if (station != null) {
            dao.updateStation(station.copy(isFavorite = !station.isFavorite))
        }
    }

    suspend fun updateStationPrices(stationId: Int, gasoline: Double, ethanol: Double, diesel: Double) {
        val station = dao.getStationById(stationId)
        if (station != null) {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            val timeText = dateFormat.format(Date(timestamp))
            dao.updateStation(
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
