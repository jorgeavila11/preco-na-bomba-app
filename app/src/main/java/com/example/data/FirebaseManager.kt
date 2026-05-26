package com.example.data

import android.content.Context
import android.util.Log
import com.example.ui.viewmodel.PromoItem
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Singleton manager for Firebase Auth and Firestore Cloud Sync, specifically
 * pre-configured for the requested project number 502690676185.
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Dynamically fetch configurations injected from the Secrets Panel
                val apiKey = if (com.example.BuildConfig.FIREBASE_API_KEY != "DUMMY_FIREBASE_API_KEY" && com.example.BuildConfig.FIREBASE_API_KEY.isNotEmpty()) {
                    com.example.BuildConfig.FIREBASE_API_KEY
                } else {
                    "AIzaSyB-dummyKeyForProgrammaticFirebaseInitialization"
                }

                val appId = if (com.example.BuildConfig.FIREBASE_APP_ID != "1:502690676185:android:d426a84dfc28e" && com.example.BuildConfig.FIREBASE_APP_ID.isNotEmpty()) {
                    com.example.BuildConfig.FIREBASE_APP_ID
                } else {
                    "1:502690676185:android:d426a84dfc28e"
                }

                val options = FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setGcmSenderId("502690676185")
                    .setProjectId("preco-na-bomba-app-android")
                    .setApiKey(apiKey)
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "Firebase programmatic initialization completed for project #502690676185!")
            }
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase programmatic initialization failed: ${e.message}", e)
        }
    }

    private val authInstance: FirebaseAuth?
        get() = if (isInitialized) FirebaseAuth.getInstance() else null

    private val firestoreInstance: FirebaseFirestore?
        get() = if (isInitialized) FirebaseFirestore.getInstance() else null

    fun isUserLoggedIn(): Boolean {
        return authInstance?.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return authInstance?.currentUser?.email
    }

    fun getCurrentUserUid(): String? {
        return authInstance?.currentUser?.uid
    }

    fun fetchProfileFromFirestore(uid: String, onResult: (DriverProfile?, String?) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(null, null)
            return
        }
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    val profile = DriverProfile(
                        id = 1,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        vehicleModel = document.getString("vehicleModel") ?: "",
                        vehiclePlate = document.getString("vehiclePlate") ?: "",
                        averageConsumption = document.getDouble("averageConsumption") ?: 12.0,
                        fuelType = document.getString("fuelType") ?: "Flex",
                        isPremium = document.getBoolean("isPremium") ?: false
                    )
                    onResult(profile, role)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar perfil no Firestore: ${exception.message}")
                onResult(null, null)
            }
    }

    fun fetchStationFromFirestore(uid: String, onResult: (FuelStation?) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(null)
            return
        }
        firestore.collection("stations").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Meu Posto"
                    val address = document.getString("address") ?: ""
                    val brand = document.getString("brand") ?: "Independente"
                    val cnpj = document.getString("cnpj") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val razaoSocial = document.getString("razaoSocial") ?: ""
                    
                    // Parse location
                    val locationMap = document.get("location") as? Map<*, *>
                    val lat = (locationMap?.get("lat") as? Number)?.toDouble() ?: -23.5505
                    val lng = (locationMap?.get("lng") as? Number)?.toDouble() ?: -46.6333
                    
                    // Parse prices
                    val pricesMap = document.get("prices") as? Map<*, *>
                    val gasoline = (pricesMap?.get("gasoline") as? Number)?.toDouble() ?: 5.89
                    val ethanol = (pricesMap?.get("ethanol") as? Number)?.toDouble() ?: 3.75
                    val diesel = (pricesMap?.get("diesel") as? Number)?.toDouble() ?: 6.12
                    
                    val station = FuelStation(
                        id = 0, // Auto-generated by Room
                        name = name,
                        address = address,
                        latitude = lat,
                        longitude = lng,
                        priceGasoline = gasoline,
                        priceEthanol = ethanol,
                        priceDiesel = diesel,
                        openHours = "24 Horas",
                        brand = brand,
                        distanceKm = 0.0,
                        isFavorite = false,
                        isPartner = true,
                        lastUpdatedText = "Sincronizado da nuvem",
                        lastUpdatedTimestamp = System.currentTimeMillis(),
                        cnpj = cnpj,
                        email = email,
                        phone = phone,
                        razaoSocial = if (razaoSocial.isNotEmpty()) razaoSocial else uid
                    )
                    onResult(station)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar posto no Firestore: ${exception.message}")
                onResult(null)
            }
    }

    fun signOut(onComplete: () -> Unit) {
        try {
            authInstance?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}")
        }
        onComplete()
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val auth = authInstance
        if (auth == null) {
            onResult(false, "Firebase não inicializado.")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Auth Login erro: ${exception.message}")
                onResult(false, exception.localizedMessage ?: "Erro desconhecido")
            }
    }

    fun registerDriver(
        email: String,
        password: String,
        profile: DriverProfile,
        onResult: (Boolean, String?) -> Unit
    ) {
        val auth = authInstance
        if (auth == null) {
            onResult(false, "Firebase não inicializado.")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // Sync profile directly to internal firebase "users" with createdAt flag true
                    saveProfileToFirestoreInternal(uid, profile, isNewRegistration = true)
                }
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Auth Register erro: ${exception.message}")
                onResult(false, exception.localizedMessage ?: "Erro desconhecido")
            }
    }

    private fun saveProfileToFirestoreInternal(uid: String, profile: DriverProfile, isNewRegistration: Boolean) {
        val firestore = firestoreInstance ?: return
        
        // Safety lock: Fetch the user document first to check if they are already registered as a station owner.
        // This ensures a station owner profile is NEVER accidentally converted or overwritten to a driver format.
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val existingRole = document.getString("role")
                    if (existingRole == "station_owner") {
                        Log.d(TAG, "Failsafe triggered: Skipping saveProfileToFirestoreInternal since user is a 'station_owner'.")
                        return@addOnSuccessListener
                    }
                }

                val userMap = hashMapOf<String, Any>(
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "vehicleModel" to profile.vehicleModel,
                    "vehiclePlate" to profile.vehiclePlate,
                    "averageConsumption" to profile.averageConsumption,
                    "fuelType" to profile.fuelType,
                    "isPremium" to profile.isPremium,
                    "role" to "driver",
                    "uid" to uid
                )

                if (isNewRegistration) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val createdAtStr = sdf.format(java.util.Date())
                    userMap["createdAt"] = createdAtStr
                }

                firestore.collection("users").document(uid)
                    .set(userMap, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Driver profile successfully synced with Firestore 'users' collection for uid: $uid")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed syncing profile with Firestore 'users' collection: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed verifying existing user role before write: ${e.message}")
            }
    }

    fun syncProfileToFirestore(profile: DriverProfile) {
        val uid = authInstance?.currentUser?.uid ?: return
        saveProfileToFirestoreInternal(uid, profile, isNewRegistration = false)
    }

    fun syncStationOwnerProfileToFirestore(name: String, email: String) {
        val uid = authInstance?.currentUser?.uid ?: return
        val firestore = firestoreInstance ?: return
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "email" to email
        )
        // Only update name and email, maintaining the station_owner role and avoiding any driver attributes
        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Station owner basic info (name/email) successfully updated at users/$uid.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed updating station owner details: ${e.message}")
            }
    }

    fun registerStationUser(
        emailAddress: String,
        passwordForAccess: String,
        nomeFantasia: String,
        cnpj: String,
        phoneNumber: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val auth = authInstance
        if (auth == null) {
            onResult(true, null) // Fail gracefully (offline fallback) if firebase is not fully active
            return
        }
        auth.createUserWithEmailAndPassword(emailAddress, passwordForAccess)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val createdAtStr = sdf.format(java.util.Date())

                    val userMap = hashMapOf<String, Any>(
                        "createdAt" to createdAtStr,
                        "email" to emailAddress,
                        "name" to nomeFantasia,
                        "role" to "station_owner",
                        "uid" to uid
                    )
                    firestoreInstance?.collection("users")?.document(uid)?.set(userMap)
                        ?.addOnSuccessListener {
                            Log.d(TAG, "Parceiro cadastrado com sucesso exclusivo para coleção 'users' para UID: $uid")
                        }
                        ?.addOnFailureListener { e ->
                            Log.e(TAG, "Erro ao cadastrar parceiro exclusivo para coleção 'users': ${e.message}")
                        }
                }
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Auth Station Register error: ${exception.message}")
                onResult(false, exception.localizedMessage ?: "Erro desconhecido")
            }
    }

    fun syncRefuelingToFirestore(refueling: Refueling) {
        val firestore = firestoreInstance ?: return
        val uid = authInstance?.currentUser?.uid ?: "anonymous"
        val data = hashMapOf(
            "userId" to uid,
            "stationName" to refueling.stationName,
            "date" to refueling.date,
            "liters" to refueling.liters,
            "pricePerLiter" to refueling.pricePerLiter,
            "totalPaid" to refueling.totalPaid,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("refuelings")
            .add(data)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Refueling synced successfully! Doc ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing refueling: ${e.message}")
            }
    }

    fun fetchAllRefuelingsFromFirestore(onResult: (List<Refueling>) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(emptyList())
            return
        }
        val uid = authInstance?.currentUser?.uid ?: "anonymous"
        firestore.collection("refuelings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<Refueling>()
                var refId = 1
                for (doc in querySnapshot.documents) {
                    val stationName = doc.getString("stationName") ?: "Posto"
                    val date = doc.getString("date") ?: ""
                    val liters = doc.getDouble("liters") ?: 0.0
                    val pricePerLiter = doc.getDouble("pricePerLiter") ?: 0.0
                    val totalPaid = doc.getDouble("totalPaid") ?: 0.0
                    list.add(
                        Refueling(
                            id = refId++,
                            stationName = stationName,
                            date = date,
                            liters = liters,
                            pricePerLiter = pricePerLiter,
                            totalPaid = totalPaid
                        )
                    )
                }
                onResult(list)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar abastecimentos do Firestore: ${exception.message}")
                onResult(emptyList())
            }
    }

    fun syncStationPriceToFirestore(station: FuelStation) {
        val firestore = firestoreInstance ?: return
        val docId = station.id.toString()
        val currentUid = authInstance?.currentUser?.uid ?: ""
        val currentUserEmail = authInstance?.currentUser?.email ?: ""

        val isValidTargetUid = !station.razaoSocial.isNullOrBlank() && 
                !station.razaoSocial.contains(" ") && 
                station.razaoSocial.length >= 10

        // Determine if this station belongs directly to the logged-in station owner
        val isMyStation = currentUid.isNotEmpty() && (
            (isValidTargetUid && station.razaoSocial == currentUid) ||
            (!station.email.isNullOrBlank() && station.email.trim().lowercase() == currentUserEmail.trim().lowercase())
        )

        // Only sync in nested/premium format if it is actually my station, or if it has its own valid target UID,
        // or if it has an email representing a partner and we are not logged in as a conflicting user.
        val shouldSyncAsPartner = isMyStation || (station.isPartner && (isValidTargetUid || !station.email.isNullOrBlank()))

        if (shouldSyncAsPartner) {
            val targetUid = if (isMyStation) {
                currentUid
            } else if (isValidTargetUid) {
                station.razaoSocial!!
            } else {
                station.email ?: ""
            }

            if (targetUid.isNotEmpty()) {
                val nestedData = hashMapOf(
                    "address" to station.address,
                    "brand" to station.brand,
                    "location" to hashMapOf(
                        "lat" to station.latitude,
                        "lng" to station.longitude
                    ),
                    "name" to station.name,
                    "prices" to hashMapOf(
                        "diesel" to station.priceDiesel,
                        "ethanol" to station.priceEthanol,
                        "gasoline" to station.priceGasoline
                    ),
                    "status" to "open",
                    "uid" to targetUid,
                    "userId" to targetUid,
                    "ownerUid" to targetUid,
                    "cnpj" to (station.cnpj ?: ""),
                    "email" to (station.email ?: ""),
                    "phone" to (station.phone ?: ""),
                    "razaoSocial" to (if (isValidTargetUid) station.razaoSocial!! else station.razaoSocial ?: ""),
                    "isPremium" to true
                )

                firestore.collection("stations").document(targetUid)
                    .set(nestedData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Station partner nested format successfully synced to cloud stations/$targetUid.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed syncing partner station nested format to stations/$targetUid: ${e.message}")
                    }
            } else {
                Log.w(TAG, "Station is partner but brand/email/currentUid are all empty, cannot sync to targetUid doc.")
            }
        } else {
            val flatData = hashMapOf(
                "id" to station.id,
                "name" to station.name,
                "address" to station.address,
                "latitude" to station.latitude,
                "longitude" to station.longitude,
                "priceGasoline" to station.priceGasoline,
                "priceEthanol" to station.priceEthanol,
                "priceDiesel" to station.priceDiesel,
                "openHours" to station.openHours,
                "brand" to station.brand,
                "distanceKm" to station.distanceKm,
                "isFavorite" to station.isFavorite,
                "isPartner" to station.isPartner,
                "isPremium" to station.isPartner,
                "lastUpdatedText" to station.lastUpdatedText,
                "lastUpdatedTimestamp" to station.lastUpdatedTimestamp,
                "cnpj" to (station.cnpj ?: ""),
                "email" to (station.email ?: ""),
                "phone" to (station.phone ?: ""),
                "razaoSocial" to (if (isValidTargetUid) "" else station.razaoSocial ?: ""),
                "ownerUid" to (if (isMyStation) currentUid else "")
            )

            firestore.collection("stations").document(docId)
                .set(flatData)
                .addOnSuccessListener {
                    Log.d(TAG, "Station prices flat format successfully synced to cloud Firestore at stations/$docId.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed syncing station flat format: ${e.message}")
                }
        }
    }

    fun syncPromotionToFirestore(
        stationIdStr: String,
        title: String,
        description: String,
        price: Double,
        category: String,
        startDate: String,
        endDate: String,
        isPremium: Boolean,
        docId: String? = null,
        onComplete: (String?) -> Unit = {}
    ) {
        val firestore = firestoreInstance ?: run { onComplete(null); return }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val timestampStr = sdf.format(java.util.Date())

        val data = hashMapOf(
            "stationId" to stationIdStr,
            "title" to title,
            "description" to description,
            "price" to price,
            "category" to category,
            "startDate" to startDate,
            "endDate" to endDate,
            "isFromPremiumStation" to isPremium,
            "createdAt" to timestampStr,
            "updatedAt" to timestampStr
        )
        val docRef = if (docId != null) {
            firestore.collection("promotions").document(docId)
        } else {
            firestore.collection("promotions").document()
        }
        
        docRef.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Promotion synced successfully to Firestore! Doc ID: ${docRef.id}")
                onComplete(docRef.id)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing promotion: ${e.message}")
                onComplete(null)
            }
    }

    fun fetchAllStationsFromFirestore(onResult: (List<FuelStation>) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(emptyList())
            return
        }
        firestore.collection("stations")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<FuelStation>()
                for (doc in querySnapshot.documents) {
                    val name = doc.getString("name") ?: continue
                    val address = doc.getString("address") ?: ""
                    val brand = doc.getString("brand") ?: "Independente"
                    val cnpj = doc.getString("cnpj") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val razaoSocial = doc.getString("razaoSocial") ?: ""
                    val uid = doc.id
                    
                    val locationMap = doc.get("location") as? Map<*, *>
                    val lat = (locationMap?.get("lat") as? Number)?.toDouble() ?: -23.5505
                    val lng = (locationMap?.get("lng") as? Number)?.toDouble() ?: -46.6333
                    
                    val pricesMap = doc.get("prices") as? Map<*, *>
                    val gasoline = (pricesMap?.get("gasoline") as? Number)?.toDouble() ?: 5.89
                    val ethanol = (pricesMap?.get("ethanol") as? Number)?.toDouble() ?: 3.75
                    val diesel = (pricesMap?.get("diesel") as? Number)?.toDouble() ?: 6.12
                    
                    val isPartner = doc.getBoolean("isPremium") ?: doc.getBoolean("isPartner") ?: true

                    val station = FuelStation(
                        id = 0,
                        name = name,
                        address = address,
                        latitude = lat,
                        longitude = lng,
                        priceGasoline = gasoline,
                        priceEthanol = ethanol,
                        priceDiesel = diesel,
                        openHours = "24 Horas",
                        brand = brand,
                        distanceKm = 1.5,
                        isFavorite = false,
                        isPartner = isPartner,
                        lastUpdatedText = "Sincronizado da nuvem",
                        lastUpdatedTimestamp = System.currentTimeMillis(),
                        cnpj = cnpj,
                        email = email,
                        phone = phone,
                        razaoSocial = uid // Map the Firestore owner UID to razaoSocial so we can match it back with promotions!
                    )
                    list.add(station)
                }
                onResult(list)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar todos os postos: ${exception.message}", exception)
                onResult(emptyList())
            }
    }

    fun fetchAllPromotionsFromFirestore(onResult: (List<PromoItem>) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(emptyList())
            return
        }
        firestore.collection("promotions")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = mutableListOf<PromoItem>()
                for (doc in querySnapshot.documents) {
                    val title = doc.getString("title") ?: continue
                    val category = doc.getString("category") ?: "Conveniência"
                    val description = doc.getString("description") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val startDate = doc.getString("startDate") ?: ""
                    val endDate = doc.getString("endDate") ?: ""
                    val stationIdStr = doc.getString("stationId") ?: ""
                    val isPremium = doc.getBoolean("isFromPremiumStation") ?: true
                    
                    val formattedPrice = String.format("R$ %.2f", price).replace('.', ',')
                    
                    list.add(PromoItem(
                        title = title,
                        category = category,
                        value = formattedPrice,
                        icon = when (category) {
                            "Combustível" -> "local_gas_station"
                            "Conveniência" -> "shopping_basket"
                            "Serviços" -> "build"
                            else -> "sell"
                        },
                        stationName = "Posto",
                        distanceKm = "0.7 km",
                        isFromPremiumStation = isPremium,
                        stationId = -1,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        price = price,
                        firestoreStationId = stationIdStr,
                        docId = doc.id
                    ))
                }
                onResult(list)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar todas as promoções: ${exception.message}", exception)
                onResult(emptyList())
            }
    }

    fun deletePromotionFromFirestore(docId: String, onComplete: (Boolean) -> Unit = {}) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onComplete(false)
            return
        }
        firestore.collection("promotions")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Promotion $docId deleted successfully from Firestore")
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting promotion $docId: ${exception.message}", exception)
                onComplete(false)
            }
    }

    suspend fun checkCnpjExistsInFirestore(cnpjStr: String): Boolean {
        val firestore = firestoreInstance ?: return false
        val cleanCnpj = cnpjStr.replace(Regex("[^0-9]"), "")
        if (cleanCnpj.isEmpty()) return false
        return try {
            // Check in users collection (where vehiclePlate is used to store CNPJ for station_owners)
            val usersQuery = firestore.collection("users")
                .whereEqualTo("vehiclePlate", cleanCnpj)
                .get()
                .await()
            if (!usersQuery.isEmpty) {
                Log.d(TAG, "CNPJ $cleanCnpj found in 'users' collection.")
                return true
            }

            // Check in stations collection
            val stationsQuery = firestore.collection("stations")
                .whereEqualTo("cnpj", cleanCnpj)
                .get()
                .await()
            if (!stationsQuery.isEmpty) {
                Log.d(TAG, "CNPJ $cleanCnpj found in 'stations' collection.")
                return true
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking CNPJ in Firestore: ${e.message}")
            false
        }
    }

    suspend fun checkEmailExistsInFirestore(emailStr: String): Boolean {
        val firestore = firestoreInstance ?: return false
        val cleanEmail = emailStr.trim().lowercase()
        if (cleanEmail.isEmpty()) return false
        return try {
            val usersQuery = firestore.collection("users")
                .whereEqualTo("email", cleanEmail)
                .get()
                .await()
            if (!usersQuery.isEmpty) {
                Log.d(TAG, "Email $cleanEmail found in 'users' collection.")
                return true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking email in Firestore: ${e.message}")
            false
        }
    }
}
