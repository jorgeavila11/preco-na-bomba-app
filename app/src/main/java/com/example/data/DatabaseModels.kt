package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// 1. Station Entity - Keeps track of gas stations and their live prices on the pump
@Entity(tableName = "stations")
data class FuelStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val priceGasoline: Double,
    val priceEthanol: Double,
    val priceDiesel: Double,
    val openHours: String,
    val brand: String, // Shell, Ipiranga, Petrobras, independent
    val distanceKm: Double,
    val isFavorite: Boolean = false,
    val isPartner: Boolean = false,
    val lastUpdatedText: String = "Atualizado recentemente",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

// 2. Refueling Log Entity - History of fuelings logged by the driver
@Entity(tableName = "refuelings")
data class Refueling(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val date: String,
    val liters: Double,
    val pricePerLiter: Double,
    val totalPaid: Double
)

// 3. User Profile - Keep profile info and driver preferences secure offline
@Entity(tableName = "user_profile")
data class DriverProfile(
    @PrimaryKey val id: Int = 1, // Single-row configuration
    val name: String,
    val email: String,
    val phone: String,
    val vehicleModel: String,
    val vehiclePlate: String,
    val averageConsumption: Double,
    val fuelType: String = "Flex", // Flex, Gasolina, Alcohol, Diesel
    val isPremium: Boolean = false
)

// DAOs (Data Access Objects)
@Dao
interface PrecoNaBombaDao {
    // Station operations
    @Query("SELECT * FROM stations ORDER BY distanceKm ASC")
    fun getAllStations(): Flow<List<FuelStation>>

    @Query("SELECT * FROM stations WHERE isFavorite = 1 ORDER BY distanceKm ASC")
    fun getFavoriteStations(): Flow<List<FuelStation>>

    @Query("SELECT * FROM stations WHERE id = :id LIMIT 1")
    suspend fun getStationById(id: Int): FuelStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<FuelStation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: FuelStation)

    @Update
    suspend fun updateStation(station: FuelStation)

    // Refueling operations
    @Query("SELECT * FROM refuelings ORDER BY id DESC")
    fun getAllRefuelings(): Flow<List<Refueling>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefueling(refueling: Refueling)

    // User Profile operations
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<DriverProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): DriverProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DriverProfile)
}

// AppDatabase definition
@Database(
    entities = [FuelStation::class, Refueling::class, DriverProfile::class],
    version = 2,
    exportSchema = false
)
abstract class PrecoNaBombaDatabase : RoomDatabase() {
    abstract fun precoNaBombaDao(): PrecoNaBombaDao
}
