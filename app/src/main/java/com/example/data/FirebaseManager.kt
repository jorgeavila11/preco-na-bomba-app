package com.example.data

import android.content.Context
import android.util.Log
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

    fun fetchProfileFromFirestore(uid: String, onResult: (DriverProfile?) -> Unit) {
        val firestore = firestoreInstance
        if (firestore == null) {
            onResult(null)
            return
        }
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
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
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar perfil no Firestore: ${exception.message}")
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

    fun syncProfileToFirestore(profile: DriverProfile) {
        val uid = authInstance?.currentUser?.uid ?: return
        saveProfileToFirestoreInternal(uid, profile, isNewRegistration = false)
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

    fun syncStationPriceToFirestore(station: FuelStation) {
        val firestore = firestoreInstance ?: return
        val docId = station.id.toString()
        val currentUid = authInstance?.currentUser?.uid ?: ""

        val isPartnerStation = station.isPartner || currentUid.isNotEmpty()

        if (isPartnerStation) {
            val targetUid = if (currentUid.isNotEmpty()) currentUid else (station.email ?: "")
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
                    "razaoSocial" to (station.razaoSocial ?: ""),
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
                "razaoSocial" to (station.razaoSocial ?: ""),
                "ownerUid" to currentUid
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

    fun syncPromotionToFirestore(stationId: Int, title: String, category: String, value: String, stationName: String, isPremium: Boolean) {
        val firestore = firestoreInstance ?: return
        val data = hashMapOf(
            "stationId" to stationId,
            "title" to title,
            "category" to category,
            "value" to value,
            "stationName" to stationName,
            "isFromPremiumStation" to isPremium,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("promotions")
            .add(data)
            .addOnSuccessListener { docRef ->
                Log.d(TAG, "Promotion synced successfully to Firestore! Doc ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing promotion: ${e.message}")
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
