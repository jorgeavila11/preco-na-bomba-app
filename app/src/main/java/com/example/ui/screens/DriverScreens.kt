package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.viewmodel.PromoItem
import com.example.ui.theme.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.PrecoNaBombaViewModel
import com.example.ui.viewmodel.Screen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun MapMiniPreview(onNavigateToMap: () -> Unit, countNearby: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, BorderSlate300, RoundedCornerShape(16.dp))
            .clickable { onNavigateToMap() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rawSize = 20.dp.toPx()
            if (rawSize > 1f) {
                val strokeWidth = 1.dp.toPx()
                var x = 0f
                while (x < this.size.width) {
                    drawLine(
                        color = Color(0xFF94A3B8).copy(alpha = 0.2f),
                        start = Offset(x, 0f),
                        end = Offset(x, this.size.height),
                        strokeWidth = strokeWidth
                    )
                    x += rawSize
                }
                var y = 0f
                while (y < this.size.height) {
                    drawLine(
                        color = Color(0xFF94A3B8).copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(this.size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += rawSize
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF475569), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Alternar para o mapa",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color.White, RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$countNearby postos próximos",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CobaltPrimary
            )
        }
    }
}

fun requestSystemLocation(context: Context, viewModel: PrecoNaBombaViewModel) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            Toast.makeText(context, "Serviço de localização indisponível.", Toast.LENGTH_SHORT).show()
            return
        }
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(context, "Por favor, ative o GPS/Localização no dispositivo.", Toast.LENGTH_LONG).show()
            return
        }

        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineCheck != PackageManager.PERMISSION_GRANTED && coarseCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão necessária.", Toast.LENGTH_SHORT).show()
            return
        }

        // Try last known location first for instantaneous load
        var lastKnown: Location? = null
        if (isGpsEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        if (lastKnown == null && isNetworkEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        lastKnown?.let {
            viewModel.updateUserLocation(it.latitude, it.longitude)
            Toast.makeText(context, "Localização atualizada via GPS!", Toast.LENGTH_SHORT).show()
            return
        }

        // If no last known location, request single update
        val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        locationManager.requestSingleUpdate(provider, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                viewModel.updateUserLocation(location.latitude, location.longitude)
                Toast.makeText(context, "Localização atualizada!", Toast.LENGTH_SHORT).show()
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }, context.mainLooper)

        Toast.makeText(context, "Obtendo sinal GPS...", Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Toast.makeText(context, "Erro de segurança ao acessar GPS.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao obter GPS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DriverLocationPanel(viewModel: PrecoNaBombaViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            requestSystemLocation(context, viewModel)
        } else {
            Toast.makeText(context, "Permissão de GPS necessária para atualizar sua distância em tempo real.", Toast.LENGTH_SHORT).show()
        }
    }

    val presetLocations = listOf(
        Triple("Av. Paulista", -23.5615, -46.6560),
        Triple("Centro SP", -23.5489, -46.6388),
        Triple("Marginal Tietê", -23.5180, -46.6710),
        Triple("Ibirapuera", -23.5874, -46.6576)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate300, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sua Localização GPS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColOnSurface
                    )
                    Text(
                        text = String.format("Lat: %.4f • Lon: %.4f", userLocation.first, userLocation.second),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {
                            requestSystemLocation(context, viewModel)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS Real", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Simular coordenada de teste (para testar recálculo de distâncias):",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetLocations.forEach { (label, lat, lng) ->
                    val isCurrentPreset = (userLocation.first == lat && userLocation.second == lng)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrentPreset) MaterialTheme.colorScheme.primaryContainer else BorderSlate100)
                            .border(
                                width = 1.dp,
                                color = if (isCurrentPreset) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                viewModel.updateUserLocation(lat, lng)
                                Toast.makeText(context, "Simulado em $label!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentPreset) MaterialTheme.colorScheme.onPrimaryContainer else TextColOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDriverHome(
    viewModel: PrecoNaBombaViewModel
) {
    val stations by viewModel.allStations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.selectedFuelFilter.collectAsState()
    val profileState by viewModel.profile.collectAsState()
    val isPremium = profileState?.isPremium ?: false
    val averageConsumption = profileState?.averageConsumption ?: 12.0
    val promos by viewModel.promoList.collectAsState()
    val context = LocalContext.current

    val startupLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            requestSystemLocation(context, viewModel)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            requestSystemLocation(context, viewModel)
        } else {
            startupLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Apply filtering logic
    val filteredStations = remember(stations, searchQuery, activeFilter) {
        var list = stations.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }
        when (activeFilter) {
            "Gasolina" -> list = list.sortedBy { it.priceGasoline }
            "Etanol" -> list = list.sortedBy { it.priceEthanol }
            "Diesel" -> list = list.sortedBy { it.priceDiesel }
            "Menor Preço" -> list = list.sortedBy { minOf(it.priceGasoline, it.priceEthanol) }
            "Mais Próximo" -> list = list.sortedBy { it.distanceKm }
        }
        list
    }

    // Identify busiest/cheapest station for dynamic "MAIS BARATO" high contrast highlighter
    val cheapestStationId = remember(filteredStations, activeFilter) {
        if (filteredStations.isEmpty()) null
        else {
            val keyFilter = if (activeFilter == "Menor Preço") "Gasolina" else activeFilter
            when (keyFilter) {
                "Gasolina" -> filteredStations.minByOrNull { it.priceGasoline }?.id
                "Etanol" -> filteredStations.minByOrNull { it.priceEthanol }?.id
                "Diesel" -> filteredStations.minByOrNull { it.priceDiesel }?.id
                else -> filteredStations.minByOrNull { minOf(it.priceGasoline, it.priceEthanol) }?.id
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                // Main Header Title Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                Toast.makeText(context, "Menu em breve!", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "≡",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Preço na Bomba",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.DriverMap) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Visualizar no Mapa",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Search Bar Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar endereço ou posto...", color = TextColVariant) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextColVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderSlate100,
                        focusedTextColor = TextColOnSurface,
                        unfocusedTextColor = TextColOnSurface
                    )
                )

                // Filter Rows
                val filterOptions = listOf("Gasolina", "Etanol", "Diesel", "Menor Preço", "Mais Próximo")
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { opt ->
                        val isSelected = activeFilter == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else BorderSlate300,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setFuelFilter(opt) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else TextColOnSurface
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            DriverBottomNavigationBar(Screen.MainDriverHome) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("main_driver_home")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header Map preview block inside column
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    MapMiniPreview(
                        onNavigateToMap = { viewModel.navigateTo(Screen.DriverMap) },
                        countNearby = filteredStations.size
                    )
                }

                // Header Content Sub-title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Postos Próximos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ver no mapa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.navigateTo(Screen.DriverMap) }
                        )
                    }
                }

                if (filteredStations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Nenhum posto encontrado.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredStations) { station ->
                        val isCheapest = station.id == cheapestStationId
                        val stationPromos = promos.filter { it.stationId == station.id }
                        StationCardItem(
                            station = station,
                            activeFilter = activeFilter,
                            onFavoriteToggle = { viewModel.toggleFavorite(station.id) },
                            onSelectOnMap = {
                                viewModel.selectStation(station.id)
                                viewModel.navigateTo(Screen.DriverMap)
                            },
                            isCheapest = isCheapest,
                            isPremium = isPremium,
                            averageConsumption = averageConsumption,
                            promotions = stationPromos
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StationCardItem(
    station: FuelStation,
    activeFilter: String,
    onFavoriteToggle: () -> Unit,
    onSelectOnMap: () -> Unit,
    isCheapest: Boolean = false,
    isPremium: Boolean = false,
    averageConsumption: Double = 12.0,
    promotions: List<PromoItem> = emptyList()
) {
    val context = LocalContext.current
    // Custom Brand initials & colors to match high contrast design theme
    val brandText: String
    val brandBg: Color
    val brandTextCol: Color
    val brandResource: String
    when {
        station.brand.contains("shell", ignoreCase = true) || station.name.contains("shell", ignoreCase = true) -> {
            brandText = "SH"
            brandBg = Color(0xFFFDE047)
            brandTextCol = Color(0xFF1E293B)
            brandResource = "Shell"
        }
        station.brand.contains("ipiranga", ignoreCase = true) || station.name.contains("ipiranga", ignoreCase = true) -> {
            brandText = "IP"
            brandBg = Color(0xFFF97316)
            brandTextCol = Color.White
            brandResource = "Ipiranga"
        }
        station.brand.contains("petrobras", ignoreCase = true) || station.name.contains("petrobras", ignoreCase = true) -> {
            brandText = "BR"
            brandBg = Color(0xFF047857)
            brandTextCol = Color.White
            brandResource = "Petrobras"
        }
        else -> {
            brandText = "PT"
            brandBg = Color(0xFF64748B)
            brandTextCol = Color.White
            brandResource = "Posto"
        }
    }

    val activePrice = when (activeFilter) {
        "Etanol" -> station.priceEthanol
        "Diesel" -> station.priceDiesel
        else -> station.priceGasoline
    }

    val fuelLabel = when (activeFilter) {
        "Etanol" -> "ETANOL"
        "Diesel" -> "DIESEL"
        else -> "GASOLINA"
    }

    val estCost = (station.distanceKm / averageConsumption) * activePrice

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("station_card_${station.id}")
            .clickable { onSelectOnMap() }
            .border(
                width = if (station.isPartner) 2.dp else 1.dp,
                color = if (station.isPartner) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else BorderSlate100,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (station.isPartner) MaterialTheme.colorScheme.primary.copy(alpha = 0.02f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (station.isPartner) 2.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top Section Row: Title on Left, Favorite/Navigation on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = station.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (station.isPartner) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFEAB308), // Gold
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PARCEIRO PREMIUM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD97706) // Yellow Gold
                            )
                        }
                    }
                }

                // Quick Actions (Favorite STAR & Select MAP)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favoritar",
                            tint = if (station.isFavorite) Color(0xFFEAB308) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onSelectOnMap() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "➜",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 2. Middle Section Row: Brand Logo on Left, Details (Status & Distance/Custo in a Row) on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand logo container (SH/Petrobras box)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brandBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brandText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = brandTextCol
                        )
                    }
                    Text(
                        text = brandResource.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                // Info Column: Status on top, Distance & Custo badge below
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status text
                    Text(
                        text = station.openHours.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A),
                        maxLines = 1
                    )

                    // Distance & Custo side-by-side Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = String.format("%.1f km", station.distanceKm),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )

                        // Blue Custo Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                                .clickable {
                                    if (!isPremium) {
                                        Toast.makeText(context, "Calculadora de gastos de combustível em tempo real é exclusiva para membros Premium!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier
                                    .size(11.dp)
                                    .graphicsLayer(rotationZ = -45f)
                            )
                            Text(
                                text = if (isPremium) {
                                    if (activePrice <= 0.0) "CUSTO: ---" else String.format("R$ %.2f", estCost).replace('.', ',')
                                } else {
                                    "VER CUSTO DE VIAGEM 🔒"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 4. Large Yellow/Amber Bordered Featured Price Container (Matching image style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = fuelLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activePrice <= 0.0) "Preço não informado" else String.format("R$ %.2f", activePrice),
                            fontSize = if (activePrice <= 0.0) 15.sp else 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB) // Blue color
                        )
                        if (isCheapest && activePrice > 0.0) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "MELHOR PREÇO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Secondary low-profile row below featuring the rest of the prices for full driver awareness
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary smaller price details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeFilter != "Gasolina") {
                        Text(
                            text = "G: " + if (station.priceGasoline <= 0.0) "---" else String.format("R$ %.2f", station.priceGasoline),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                    if (activeFilter != "Etanol") {
                        Text(
                            text = "E: " + if (station.priceEthanol <= 0.0) "---" else String.format("R$ %.2f", station.priceEthanol),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                    if (activeFilter != "Diesel") {
                        Text(
                            text = "D: " + if (station.priceDiesel <= 0.0) "---" else String.format("R$ %.2f", station.priceDiesel),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }

                // Updated text with visual Clock icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = station.lastUpdatedText,
                        fontSize = 10.5.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Compact Promotions Indicator inside the card (low-profile design)
            if (promotions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = BorderSlate100, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🎁",
                            fontSize = 12.sp
                        )
                        Column {
                            Text(
                                text = if (promotions.size == 1) "1 Promoção Ativa" else "${promotions.size} Promoções Ativas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            val promoListText = promotions.joinToString(", ") { it.title }
                            Text(
                                text = promoListText,
                                fontSize = 9.5.sp,
                                color = Color(0xFFD97706),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VER CLUB",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


// 2. Explorable Dynamic Map screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMap(
    viewModel: PrecoNaBombaViewModel
) {
    val stations by viewModel.allStations.collectAsState()
    val selectedId by viewModel.selectedStationId.collectAsState()
    val profileState by viewModel.profile.collectAsState()
    val isPremium = profileState?.isPremium ?: false
    val averageConsumption = profileState?.averageConsumption ?: 12.0
    val activeFilter by viewModel.selectedFuelFilter.collectAsState()
    val context = LocalContext.current

    var isPriceUpdateDialogOpen by remember { mutableStateOf(false) }
    var localStationToUpdate by remember { mutableStateOf<com.example.data.FuelStation?>(null) }

    val currentSelectedStation = remember(stations, selectedId) {
        stations.find { it.id == selectedId } ?: stations.firstOrNull()
    }

    Scaffold(
        topBar = {
            // Quick input overlays
            Card(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = colorSchemePrimaryTint(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Buscar postos ou endereços...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { Toast.makeText(context, "Notificações de preço ativas!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Notifications, null, tint = Color.Gray)
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Bottom Details Card
                currentSelectedStation?.let { station ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("map_station_details_sheet"),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .background(Color(0xFFE2E8F0), CircleShape)
                                        .align(Alignment.CenterHorizontally)
                                        .padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = station.name,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = station.address,
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ABERTO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Fast Prices Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                            Column {
                                                Text("Gasolina Comum", fontSize = 10.sp, color = Color.Gray)
                                                Text(
                                                    text = if (station.priceGasoline <= 0.0) "Não informado" else String.format("R$ %.2f", station.priceGasoline),
                                                    fontSize = if (station.priceGasoline <= 0.0) 12.sp else 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                                            Column {
                                                Text("Etanol", fontSize = 10.sp, color = Color.Gray)
                                                Text(
                                                    text = if (station.priceEthanol <= 0.0) "Não informado" else String.format("R$ %.2f", station.priceEthanol),
                                                    fontSize = if (station.priceEthanol <= 0.0) 12.sp else 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }

                                // Premium Travel Cost Estimation (Rule 2)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (isPremium) {
                                    val activePrice = when (activeFilter) {
                                         "Etanol" -> station.priceEthanol
                                         "Diesel" -> station.priceDiesel
                                         else -> station.priceGasoline
                                     }
                                     val estCost = (station.distanceKm / averageConsumption) * activePrice
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2E7D32))
                                        Column {
                                            Text("CUSTO ESTIMADO DE COMBUSTÍVEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(
                                                text = String.format("Gasto de R$ %.2f • Distância: %.1f km até o posto", estCost, station.distanceKm),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                            .clickable {
                                                Toast.makeText(context, "Upgrade para Premium para ter acesso automático às estimativas de custo!", Toast.LENGTH_LONG).show()
                                                viewModel.navigateTo(Screen.PremiumDetails)
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, null, tint = Color(0xFFE65100))
                                        Column {
                                            Text("CUSTO DE VIAGEM ATÉ O POSTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(
                                                text = "🔒 Calcular custo de viagem (Recurso Premium)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }

                                if (station.priceGasoline <= 0.0 || station.priceEthanol <= 0.0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706)
                                                )
                                                Text(
                                                    text = "Vimos que este posto existe, você sabe o preço do combustível lá?",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    localStationToUpdate = station
                                                    isPriceUpdateDialogOpen = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Preencher Preço", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Nav actions row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Calculando melhor trajeto com GPS no Google Maps!", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Como Chegar", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(station.id) },
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favoritar",
                                            tint = if (station.isFavorite) Color.Red else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                DriverBottomNavigationBar(Screen.DriverMap) { screen ->
                    viewModel.navigateTo(screen)
                }
            }
        },
        modifier = Modifier.testTag("driver_map_screen")
    ) { innerPadding ->
        // Styled canvas map vector overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0)) // Light background grid
        ) {
            // Draw city grid layout canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color(0xFFCBD5E1)
                
                // Draw horizontal streets
                drawLine(gridColor, Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), strokeWidth = 12f)
                drawLine(gridColor, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth = 24f)
                drawLine(gridColor, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), strokeWidth = 16f)

                // Draw vertical streets
                drawLine(gridColor, Offset(size.width * 0.25f, 0f), Offset(size.width * 0.25f, size.height), strokeWidth = 14f)
                drawLine(gridColor, Offset(size.width * 0.65f, 0f), Offset(size.width * 0.65f, size.height), strokeWidth = 18f)

                // Draw river
                val riverColor = Color(0xFFA5F3FC)
                drawLine(riverColor, Offset(0f, size.height * 0.1f), Offset(size.width, size.height * 0.3f), strokeWidth = 40f)
            }

            // Interactive Pins mapped visually on top
            val widthLimit = 320.dp
            val heightLimit = 450.dp
            stations.forEach { station ->
                val xPercent = when (station.id % 3) {
                    0 -> 0.2f
                    1 -> 0.7f
                    else -> 0.55f
                }
                val yPercent = when (station.id % 4) {
                    0 -> 0.15f
                    1 -> 0.45f
                    2 -> 0.72f
                    else -> 0.6f
                }

                val hasActiveMarkerSelected = station.id == selectedId

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (320 * xPercent).dp,
                            y = (450 * yPercent).dp
                        )
                        .clickable { viewModel.selectStation(station.id) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Tag box showing live price
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasActiveMarkerSelected) MaterialTheme.colorScheme.secondaryContainer 
                                    else if (station.isPartner) Color(0xFF0369A1) // Partner color
                                    else MaterialTheme.colorScheme.primary
                                )
                                .border(
                                    width = if (station.isPartner) 3.dp else 2.dp,
                                    color = if (station.isPartner) Color(0xFFF59E0B) else Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (station.isPartner) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Parceiro",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Text(
                                    text = String.format("R$ %.2f", station.priceGasoline),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasActiveMarkerSelected) Color.Black else Color.White
                                )
                            }
                        }

                        // Bottom indicator arrow
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (hasActiveMarkerSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(bottomStart = 2.dp)
                                )
                                .border(2.dp, Color.White, RoundedCornerShape(bottomStart = 2.dp))
                        )
                    }
                }
            }

            // Floating Controls: Zoom in, Zoom out, My Location
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { Toast.makeText(context, "Aumentando zoom!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, "Zoom In", tint = MaterialTheme.colorScheme.primary)
                }

                // Styled zoom-out subtraction text box layout
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { Toast.makeText(context, "Diminuindo zoom!", Toast.LENGTH_SHORT).show() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = { Toast.makeText(context, "Sua localização localizada!", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.LocationOn, "Minha Localização", tint = Color.White)
                }
            }
        }
    }

    if (isPriceUpdateDialogOpen && localStationToUpdate != null) {
        val targetStation = localStationToUpdate!!
        var inputGasoline by remember { mutableStateOf(if (targetStation.priceGasoline > 0.0) targetStation.priceGasoline.toString() else "") }
        var inputEthanol by remember { mutableStateOf(if (targetStation.priceEthanol > 0.0) targetStation.priceEthanol.toString() else "") }
        var inputDiesel by remember { mutableStateOf(if (targetStation.priceDiesel > 0.0) targetStation.priceDiesel.toString() else "") }

        AlertDialog(
            onDismissRequest = { isPriceUpdateDialogOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        val gasVal = inputGasoline.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val ethVal = inputEthanol.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val dieVal = inputDiesel.replace(',', '.').toDoubleOrNull() ?: 0.0
                        
                        viewModel.updateStationPrices(targetStation.id, gasVal, ethVal, dieVal)
                        isPriceUpdateDialogOpen = false
                        Toast.makeText(context, "Preços atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Enviar Preços")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPriceUpdateDialogOpen = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Informar Preços na Bomba", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(targetStation.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = inputGasoline,
                        onValueChange = { inputGasoline = it },
                        label = { Text("Preço da Gasolina (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputEthanol,
                        onValueChange = { inputEthanol = it },
                        label = { Text("Preço do Etanol (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputDiesel,
                        onValueChange = { inputDiesel = it },
                        label = { Text("Preço do Diesel (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

// 3. Driver Profile with interactive Corolla dashboard card
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileArea(
    viewModel: PrecoNaBombaViewModel
) {
    val currentProfile by viewModel.profile.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Sem novas notificações", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Notifications, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DriverBottomNavigationBar(Screen.DriverProfileArea) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("driver_profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Header with circular avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentProfile?.name ?: "João Silva",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (currentProfile?.isPremium == true) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Premium",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                    Text(
                        text = "São Paulo, SP",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Legal & Private inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("INFORMAÇÕES PESSOAIS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // Email block
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("E-mail", fontSize = 11.sp, color = Color.Gray)
                            Text(currentProfile?.email ?: "joao.silva@email.com", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Phone block
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Telefone", fontSize = 11.sp, color = Color.Gray)
                            Text(currentProfile?.phone ?: "(11) 98765-4321", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive vehicle card
            Text("MEU VEÍCULO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.DriverPrivateArea) }
                    .testTag("driver_vehicle_dashboard_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("MODELO", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                                Text(
                                    text = currentProfile?.vehicleModel ?: "Toyota Corolla",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentProfile?.vehiclePlate ?: "ABC-1234",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            Column {
                                Text("CONSUMO MÉDIO", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${currentProfile?.averageConsumption ?: 12.0} km/L",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column {
                                Text("TIPO COMBUSTÍVEL", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                                Text(
                                    text = currentProfile?.fuelType ?: "Flex",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Favorites quick link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setFuelFilter("Favoritos")
                        viewModel.navigateTo(Screen.MainDriverHome)
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, "Favoritos", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Postos Favoritos", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Gray)
                }
            }

            // Account config buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("CONFIGURAÇÕES DA CONTA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                driverProfileOptionRow(Icons.Default.Lock, "Alterar Senha") {
                    Toast.makeText(context, "Funcionalidade Premium: Alterar Senha!", Toast.LENGTH_SHORT).show()
                }
                driverProfileOptionRow(Icons.Default.Info, "Privacidade") {
                    Toast.makeText(context, "Política de privacidade atualizada em conformidade com a LGPD.", Toast.LENGTH_SHORT).show()
                }
                driverProfileOptionRow(Icons.Default.ExitToApp, "Sair da Conta") {
                    viewModel.logout {
                        Toast.makeText(context, "Sessão encerrada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun driverProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.PlayArrow, null, tint = Color.LightGray)
    }
}

// 4. Driver's Private dashboard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPrivateArea(
    viewModel: PrecoNaBombaViewModel
) {
    val currentProfile by viewModel.profile.collectAsState()
    val refuelingLogs by viewModel.allRefuelings.collectAsState()
    val context = LocalContext.current
    var isLogDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Motorista", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DriverProfileArea) }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DriverBottomNavigationBar(Screen.DriverProfileArea) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("driver_private_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Corolla specs module
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(currentProfile?.vehicleModel ?: "Toyota Corolla", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Placa: ${currentProfile?.vehiclePlate ?: "ABC-1234"}", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Consumo Médio: 12km/L", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    }
                }
            }

            // Inform New Price CTA
            Button(
                onClick = { isLogDialogOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp)
                    .testTag("driver_add_refueling_cta"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Informar Novo Preço", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction log head
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Histórico de Abastecimentos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ver tudo",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { Toast.makeText(context, "Histórico completo em sincronia!", Toast.LENGTH_SHORT).show() }
                )
            }

            if (refuelingLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum abastecimento logado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(refuelingLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.stationName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                        Text(String.format("R$ %.2f", log.totalPaid), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                    }
                                    Text(log.date, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text("${log.liters} L", fontSize = 12.sp, color = Color.Gray)
                                        Text("|", fontSize = 12.sp, color = Color.LightGray)
                                        Text(String.format("R$ %.2f/L", log.pricePerLiter), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Refueling Logger Trigger Dialog popup
    if (isLogDialogOpen) {
        var inputStation by remember { mutableStateOf("") }
        var inputLiters by remember { mutableStateOf("") }
        var inputPrice by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isLogDialogOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logStationName.value = inputStation
                        viewModel.logLiters.value = inputLiters
                        viewModel.logPricePerLiter.value = inputPrice
                        val isSaved = viewModel.saveRefueling("Hoje • 12:00")
                        if (isSaved) {
                            isLogDialogOpen = false
                            Toast.makeText(context, "Preço adicionado ao banco!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Por favor preencha os campos!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("dialog_save_refueling")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isLogDialogOpen = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Registrar Preço na Bomba", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputStation,
                        onValueChange = { inputStation = it },
                        label = { Text("Nome do Posto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputLiters,
                        onValueChange = { inputLiters = it },
                        label = { Text("Litros abastecidos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputPrice,
                        onValueChange = { inputPrice = it },
                        label = { Text("Preço por litro") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun DriverBottomNavigationBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.MainDriverHome,
            onClick = { onTabSelected(Screen.MainDriverHome) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Início", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == Screen.DriverMap,
            onClick = { onTabSelected(Screen.DriverMap) },
            icon = { Icon(Icons.Default.LocationOn, null) },
            label = { Text("Mapa", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == Screen.PremiumPromotions,
            onClick = { onTabSelected(Screen.PremiumPromotions) },
            icon = { Icon(Icons.Default.Star, null) },
            label = { Text("Promoções", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == Screen.DriverProfileArea,
            onClick = { onTabSelected(Screen.DriverProfileArea) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Perfil", fontSize = 10.sp) }
        )
    }
}

@Composable
fun colorSchemePrimaryTint(primary: Color) = if (isSystemInDarkTheme()) Color.White else primary
