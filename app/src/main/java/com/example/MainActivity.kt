package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.PrecoNaBombaDatabase
import com.example.data.PrecoNaBombaRepository
import com.example.ui.screens.*
import com.example.ui.theme.PrecoNaBombaTheme
import com.example.ui.viewmodel.PrecoNaBombaViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private lateinit var database: PrecoNaBombaDatabase
    private lateinit var repository: PrecoNaBombaRepository
    private lateinit var viewModel: PrecoNaBombaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Programmatic Firebase for project #502690676185
        com.example.data.FirebaseManager.initialize(applicationContext)

        // Local Persistence: Setup Room SQLite Database
        database = Room.databaseBuilder(
            applicationContext,
            PrecoNaBombaDatabase::class.java,
            "preco_na_bomba_database"
        ).fallbackToDestructiveMigration(true).build()

        repository = PrecoNaBombaRepository(
            database.precoNaBombaDao()
        )

        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PrecoNaBombaViewModel::class.java]

        setContent {
            PrecoNaBombaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    // MVVM navigation coordinator mapping
                    when (currentScreen) {
                        Screen.OnboardingIntro -> {
                            OnboardingIntroScreen(viewModel = viewModel)
                        }
                        Screen.OnboardingRoleSelection -> {
                            OnboardingRoleSelectionScreen(
                                viewModel = viewModel,
                                onNavigateDirectlyToHome = {
                                    viewModel.navigateTo(Screen.MainDriverHome)
                                }
                            )
                        }
                        Screen.DriverRegister -> {
                            DriverRegisterScreen(viewModel = viewModel)
                        }
                        Screen.StationRegister -> {
                            StationRegisterScreen(viewModel = viewModel)
                        }
                        Screen.UserLogin -> {
                            UserLoginScreen(viewModel = viewModel)
                        }
                        Screen.MainDriverHome -> {
                            MainDriverHome(viewModel = viewModel)
                        }
                        Screen.DriverMap -> {
                            DriverMap(viewModel = viewModel)
                        }
                        Screen.DriverProfileArea -> {
                            DriverProfileArea(viewModel = viewModel)
                        }
                        Screen.DriverPrivateArea -> {
                            DriverPrivateArea(viewModel = viewModel)
                        }
                        Screen.PremiumDetails -> {
                            PremiumDetailsScreen(viewModel = viewModel)
                        }
                        Screen.PaymentCheckout -> {
                            PaymentCheckoutScreen(viewModel = viewModel)
                        }
                        Screen.PremiumPromotions -> {
                            PremiumPromotionsScreen(viewModel = viewModel)
                        }
                        Screen.MainStationHome -> {
                            MainStationHome(viewModel = viewModel)
                        }
                        Screen.StationProfileArea -> {
                            StationProfileArea(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Custom Factory for clean Architecture injection
class ViewModelFactory(private val repository: PrecoNaBombaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrecoNaBombaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrecoNaBombaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
