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
                    // Sync profile to Firestore
                    saveProfileToFirestoreInternal(uid, profile)
                }
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Auth Register erro: ${exception.message}")
                onResult(false, exception.localizedMessage ?: "Erro desconhecido")
            }
    }

    private fun saveProfileToFirestoreInternal(uid: String, profile: DriverProfile) {
        val firestore = firestoreInstance ?: return
        val profileMap = hashMapOf(
            "name" to profile.name,
            "email" to profile.email,
            "phone" to profile.phone,
            "vehicleModel" to profile.vehicleModel,
            "vehiclePlate" to profile.vehiclePlate,
            "averageConsumption" to profile.averageConsumption,
            "fuelType" to profile.fuelType,
            "isPremium" to profile.isPremium
        )
        firestore.collection("profiles").document(uid)
            .set(profileMap)
            .addOnSuccessListener {
                Log.d(TAG, "Driver profile successfully synced with Firestore for uid: $uid")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing profile with Firestore: ${e.message}")
            }
    }

    fun syncProfileToFirestore(profile: DriverProfile) {
        val uid = authInstance?.currentUser?.uid ?: return
        saveProfileToFirestoreInternal(uid, profile)
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
        val data = hashMapOf(
            "id" to station.id,
            "name" to station.name,
            "address" to station.address,
            "priceGasoline" to station.priceGasoline,
            "priceEthanol" to station.priceEthanol,
            "priceDiesel" to station.priceDiesel,
            "openHours" to station.openHours,
            "brand" to station.brand,
            "distanceKm" to station.distanceKm,
            "lastUpdatedText" to station.lastUpdatedText,
            "lastUpdatedTimestamp" to station.lastUpdatedTimestamp
        )
        firestore.collection("stations").document(docId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Station prices successfully synced to cloud Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed syncing station price to cloud: ${e.message}")
            }
    }
}
