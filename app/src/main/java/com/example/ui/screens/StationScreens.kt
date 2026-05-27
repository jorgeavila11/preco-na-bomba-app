package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrecoNaBombaViewModel
import com.example.ui.viewmodel.PromoItem
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StationSaveState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStationHome(
    viewModel: PrecoNaBombaViewModel
) {
    val context = LocalContext.current
    var isAddPromoOpen by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<PromoItem?>(null) }
    var isUpgradeOfferOpen by remember { mutableStateOf(false) }

    // Subscription status
    val planState by viewModel.ownerStationPlan.collectAsState()

    val saveState by viewModel.saveState.collectAsState()

    val buttonColor by animateColorAsState(
        targetValue = when (saveState) {
            StationSaveState.SUCCESS -> Color(0xFF10B981) // Beautiful Green
            else -> MaterialTheme.colorScheme.primary // Default Primary
        },
        label = "saveButtonColor"
    )

    LaunchedEffect(saveState) {
        if (saveState == StationSaveState.SUCCESS) {
            Toast.makeText(context, "Alterações salvas com sucesso! Preço transmitido para motoristas.", Toast.LENGTH_LONG).show()
        }
    }

    // Forms bound directly to Live ViewModel values
    val name by viewModel.editStationName.collectAsState()
    val cnpj by viewModel.editStationCNPJ.collectAsState()
    val address by viewModel.editStationAddress.collectAsState()
    val openHours by viewModel.editStationOpenHours.collectAsState()

    val gasPrice by viewModel.editGasolinePrice.collectAsState()
    val ethPrice by viewModel.editEthanolPrice.collectAsState()
    val dslPrice by viewModel.editDieselPrice.collectAsState()

    val promos by viewModel.promoList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel do Proprietário", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.OnboardingRoleSelection) }) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Todas as bombas em sincronia!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Column {
                // Sticky primary action button inside footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (saveState == StationSaveState.IDLE) {
                                viewModel.saveOwnerAlterations()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("station_submit_changes_cta"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        enabled = saveState != StationSaveState.SAVING
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (saveState) {
                                StationSaveState.SAVING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SALVANDO ALTERAÇÕES...", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                StationSaveState.SUCCESS -> {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ALTERAÇÕES SALVAS!", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                StationSaveState.IDLE -> {
                                    Icon(Icons.Default.Check, null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                StationBottomNavigationBar(Screen.MainStationHome) { screen ->
                    viewModel.navigateTo(screen)
                }
            }
        },
        modifier = Modifier.testTag("station_owner_home")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simulated Gas Station banner image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                // Background visual card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Camera upload toggle representing photos
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { Toast.makeText(context, "Alterar foto do posto!", Toast.LENGTH_SHORT).show() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Identification Header Profile Specs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (planState == "Conta Premium") MaterialTheme.colorScheme.secondaryContainer else Color(0xFFE2E8F0),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (planState == "Conta Premium") "PREMIUM 💎" else "CONTA PRO 🔒",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (planState == "Conta Premium") Color.Black else Color.DarkGray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (planState == "Conta Pro") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Star, "Upgrade", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Seja Parceiro Premium! 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Cadastre promoções e atraia até 4x mais motoristas.", fontSize = 10.sp, color = Color.DarkGray)
                        }
                        Button(
                            onClick = {
                                viewModel.ownerStationPlan.value = "Conta Premium"
                                Toast.makeText(context, "Upgrade realizado! Bem vindo ao plano Parceiro Premium 🎉", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Upgrade", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else if (planState == "Conta Premium") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.5.dp, Color(0xFF22C55E))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF22C55E), CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Check, "Ativo", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Conta Parceiro/Premium Ativa ✨", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF15803D))
                            Text("Você está com acesso total! Seu posto possui destaque e selação de destaque aos motoristas.", fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Price updates module with active form inputs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atualizar Preços na Bomba", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Text(
                    text = "Ajuste os valores direto no aplicativo. Eles são atualizados para os motoristas instantaneamente.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Gasoline Block Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("GASOLINA COMUM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Preço atual na bomba", fontSize = 11.sp, color = Color.LightGray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                            BasicPriceInputField(
                                value = gasPrice,
                                onValueChange = { viewModel.editGasolinePrice.value = it }
                            )
                        }
                    }
                }

                // Ethanol Block Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ETANOL COMUM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Preço atual na bomba", fontSize = 11.sp, color = Color.LightGray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                            BasicPriceInputField(
                                value = ethPrice,
                                onValueChange = { viewModel.editEthanolPrice.value = it }
                            )
                        }
                    }
                }

                // Diesel Block Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DIESEL S10", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Preço atual na bomba", fontSize = 11.sp, color = Color.LightGray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                            BasicPriceInputField(
                                value = dslPrice,
                                onValueChange = { viewModel.editDieselPrice.value = it }
                            )
                        }
                    }
                }
            }

            // Promotional manager module: Add promo dialog list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentStationId by viewModel.currentStationId.collectAsState()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Gerenciar Promoções",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerenciar Promoções", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    TextButton(onClick = {
                        if (planState == "Conta Pro") {
                            isUpgradeOfferOpen = true
                        } else {
                            editingPromo = null
                            isAddPromoOpen = true
                        }
                    }) {
                        Text("Nova", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 14.sp)
                    }
                }

                val currentStationId by viewModel.currentStationId.collectAsState()
                val currentStationName by viewModel.editStationName.collectAsState()
                val currentStationCnpj by viewModel.editStationCNPJ.collectAsState()
                val currentStationEmail by viewModel.editStationEmail.collectAsState()
                
                val currentUid = com.example.data.FirebaseManager.getCurrentUserUid() ?: ""
                
                val stationPromos = promos.filter { promo ->
                    val matchesId = promo.stationId == currentStationId
                    val matchesUid = !promo.firestoreStationId.isNullOrBlank() && (promo.firestoreStationId == currentUid)
                    
                    val sName = currentStationName.lowercase()
                    val pStationName = promo.stationName.lowercase()
                    val pStationId = promo.firestoreStationId?.lowercase() ?: ""
                    
                    val matchesEmail = !currentStationEmail.isNullOrBlank() && pStationId == currentStationEmail.lowercase()
                    val matchesCnpj = !currentStationCnpj.isNullOrBlank() && pStationId == currentStationCnpj.replace(Regex("[^0-9]"), "")
                    
                    val isCohabStation = sName.contains("cohab") || sName.contains("cohab 3") || sName.contains("cohab iii")
                    val isCohabPromo = pStationId.contains("cohab") || pStationName.contains("cohab") || promo.title.lowercase().contains("cohab") || (promo.description?.lowercase() ?: "").contains("cohab")
                    val cohabMatch = isCohabStation && isCohabPromo
                    
                    val matchesFuzzyName = pStationId.isNotEmpty() && (sName.contains(pStationId) || pStationId.contains(sName))
                    val matchesFuzzyStationName = sName.contains(pStationName) || pStationName.contains(sName)
                    
                    matchesId || matchesUid || matchesEmail || matchesCnpj || cohabMatch || matchesFuzzyName || matchesFuzzyStationName
                }

                if (stationPromos.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma promoção cadastrada ainda.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    stationPromos.forEach { item ->
                        val displayStart = formatBraDate(item.startDate)
                        val displayEnd = formatBraDate(item.endDate)
                        val expired = isPromoExpired(item.endDate)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, if (expired) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Row 1: Icon, Title & Description, Price
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (expired) Color(0xFFFEF2F2) else Color(0xFFEFF6FF),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.category) {
                                                "Combustível" -> Icons.Default.Star
                                                "Conveniência" -> Icons.Default.ShoppingCart
                                                "Serviços" -> Icons.Default.Build
                                                else -> Icons.Default.ShoppingCart
                                            },
                                            contentDescription = null,
                                            tint = if (expired) Color(0xFFEF4444) else Color(0xFF2563EB),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        if (!item.description.isNullOrEmpty()) {
                                            Text(
                                                text = item.description,
                                                fontSize = 13.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = item.value,
                                        fontWeight = FontWeight.Black,
                                        color = if (expired) Color(0xFF94A3B8) else Color(0xFF2563EB),
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Subtle separator line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF1F5F9))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Row 2: Date period (with badge) & Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f, fill = false),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (displayStart.isNotEmpty() && displayEnd.isNotEmpty()) {
                                                    "$displayStart até $displayEnd"
                                                } else {
                                                    "Período indeterminado"
                                                },
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        if (expired) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Expirada",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingPromo = item
                                                isAddPromoOpen = true
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deletePromotion(item)
                                                Toast.makeText(context, "Promoção excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upgrade Offer dialogue for Conta Pro owners attempting to create promos
    if (isUpgradeOfferOpen) {
        AlertDialog(
            onDismissRequest = { isUpgradeOfferOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.ownerStationPlan.value = "Conta Premium"
                        isUpgradeOfferOpen = false
                        Toast.makeText(context, "Upgrade efetuado com sucesso! Agora você possui acesso à Conta Premium 🚀", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Adquirir Conta Premium", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isUpgradeOfferOpen = false }) {
                    Text("Depois")
                }
            },
            title = { Text("Recurso da Conta Premium 💎", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "O cadastro de promoções é uma funcionalidade exclusiva para parceiros do plano Premium.\n\n" +
                    "Ao fazer o upgrade de Conta Pro para Conta Premium por apenas R$ 99,90/mês, " +
                    "você ganha o direito de lançar cupons e ganha visibilidade prioritária no mapa dos motoristas!"
                )
            }
        )
    }

    // Dynamic Promo append custom dialog (representing screenshot form exactly)
    if (isAddPromoOpen) {
        val initStart = if (editingPromo != null) formatBraDate(editingPromo?.startDate) else ""
        val initEnd = if (editingPromo != null) formatBraDate(editingPromo?.endDate) else ""
        val initPrice = if (editingPromo != null) {
            editingPromo?.price?.let { if (it > 0.0) String.format(java.util.Locale.US, "%.2f", it).replace('.', ',') else "" } ?: ""
        } else ""

        var promoTitle by remember(editingPromo) { mutableStateOf(editingPromo?.title ?: "") }
        var promoDesc by remember(editingPromo) { mutableStateOf(editingPromo?.description ?: "") }
        var promoPriceStr by remember(editingPromo) { mutableStateOf(initPrice) }
        var promoCat by remember(editingPromo) { mutableStateOf(editingPromo?.category ?: "Conveniência") }
        var promoStartDate by remember(editingPromo) { mutableStateOf(initStart) }
        var promoEndDate by remember(editingPromo) { mutableStateOf(initEnd) }
        var isDropdownExpanded by remember { mutableStateOf(false) }

        val categories = listOf("Conveniência", "Combustível", "Serviços")
        val calendar = java.util.Calendar.getInstance()

        fun showDatePicker(onDateSelected: (String) -> Unit) {
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    onDateSelected(formatted)
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            ).show()
        }

        fun formatBraDateToIso(braDate: String): String {
            return try {
                val parser = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val date = parser.parse(braDate)
                if (date != null) formatter.format(date) else braDate
            } catch (e: Exception) {
                braDate
            }
        }

        Dialog(onDismissRequest = { isAddPromoOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Blue Header Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2563EB))
                            .padding(vertical = 24.dp, horizontal = 24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (editingPromo != null) "Editar Promoção" else "Nova Promoção",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (editingPromo != null) "Ajuste os detalhes e o prazo" else "Defina os detalhes e o prazo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Fields Body Block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TÍTULO
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("TÍTULO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            TextField(
                                value = promoTitle,
                                onValueChange = { promoTitle = it },
                                placeholder = { Text("Ex: Promoção no Café", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color(0xFF1E293B),
                                    unfocusedTextColor = Color(0xFF1E293B)
                                )
                            )
                        }

                        // DESCRIÇÃO (OPCIONAL)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("DESCRIÇÃO (OPCIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            TextField(
                                value = promoDesc,
                                onValueChange = { promoDesc = it },
                                placeholder = { Text("Ex: Café expresso + pão de queijo", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color(0xFF1E293B),
                                    unfocusedTextColor = Color(0xFF1E293B)
                                )
                            )
                        }

                        // Row: PREÇO (R$) and CATEGORIA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // PREÇO (R$)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PREÇO (R$)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                TextField(
                                    value = promoPriceStr,
                                    onValueChange = { promoPriceStr = it },
                                    placeholder = { Text("9,90", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF1E293B),
                                        unfocusedTextColor = Color(0xFF1E293B)
                                    )
                                )
                            }

                            // CATEGORIA
                            Box(modifier = Modifier.weight(1f)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("CATEGORIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .clickable { isDropdownExpanded = true }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(promoCat, color = Color(0xFF1E293B), fontSize = 14.sp)
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    promoCat = cat
                                                    isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Row: INÍCIO and FIM
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // INÍCIO
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("INÍCIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .clickable { showDatePicker { promoStartDate = it } }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (promoStartDate.isEmpty()) "dd / mm / aaaa" else promoStartDate,
                                            color = if (promoStartDate.isEmpty()) Color(0xFF94A3B8) else Color(0xFF1E293B),
                                            fontSize = 14.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Selecionar data de início",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // FIM
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("FIM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .clickable { showDatePicker { promoEndDate = it } }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (promoEndDate.isEmpty()) "dd / mm / aaaa" else promoEndDate,
                                            color = if (promoEndDate.isEmpty()) Color(0xFF94A3B8) else Color(0xFF1E293B),
                                            fontSize = 14.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Selecionar data de fim",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bottom Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isAddPromoOpen = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            }
                            Button(
                                onClick = {
                                    val price = promoPriceStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val isoStart = formatBraDateToIso(promoStartDate)
                                    val isoEnd = formatBraDateToIso(promoEndDate)
                                    
                                    viewModel.addNewPromotion(
                                        title = promoTitle,
                                        description = promoDesc,
                                        price = price,
                                        category = promoCat,
                                        startDate = isoStart,
                                        endDate = isoEnd,
                                        icon = when (promoCat) {
                                            "Combustível" -> "local_gas_station"
                                            "Conveniência" -> "shopping_basket"
                                            "Serviços" -> "build"
                                            else -> "sell"
                                        },
                                        docId = editingPromo?.docId
                                    )
                                    isAddPromoOpen = false
                                    if (editingPromo != null) {
                                        Toast.makeText(context, "Promoção atualizada!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Nova promoção lançada!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.weight(1f).height(52.dp).testTag("dialog_save_promo"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (editingPromo != null) "Salvar" else "Criar",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicPriceInputField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(100.dp)
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFCBD5E1)
        )
    )
}

// 2. Administrative Profile info
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationProfileArea(
    viewModel: PrecoNaBombaViewModel
) {
    val cnpj by viewModel.editStationCNPJ.collectAsState()
    val razao by viewModel.editStationRazao.collectAsState()
    val stationName by viewModel.editStationName.collectAsState()
    val address by viewModel.editStationAddress.collectAsState()
    val brand by viewModel.editStationBrand.collectAsState()
    val phone by viewModel.editStationPhone.collectAsState()
    val email by viewModel.editStationEmail.collectAsState()
    val context = LocalContext.current

    val isUid = razao.length >= 20 && !razao.contains(" ") && !razao.contains("-") && !razao.contains(".")
    val cleanRazao = if (razao.isBlank() || isUid) {
        if (stationName.isNotEmpty()) {
            if (stationName.uppercase().endsWith("LTDA")) {
                stationName
            } else {
                "$stationName Ltda"
            }
        } else if (email.isNotEmpty()) {
            email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Ltda"
        } else {
            "Comércio de Combustíveis Ltda"
        }
    } else {
        razao
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil do Posto", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            StationBottomNavigationBar(Screen.StationProfileArea) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("station_profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("INFORMAÇÕES DA EMPRESA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // CNPJ
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("CNPJ", fontSize = 10.sp, color = Color.Gray)
                            Text(cnpj, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Razão Social
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Razão Social", fontSize = 10.sp, color = Color.Gray)
                            Text(cleanRazao, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Bandeira (Brand)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Bandeira (Distribuidora)", fontSize = 10.sp, color = Color.Gray)
                            Text(if (brand.isNotEmpty()) brand else "Independente (Sem Bandeira)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Telefone
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Telefone de Contato", fontSize = 10.sp, color = Color.Gray)
                            Text(if (phone.isNotEmpty()) phone else "Não informado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // E-mail
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("E-mail corporativo", fontSize = 10.sp, color = Color.Gray)
                            Text(if (email.isNotEmpty()) email else "Não informado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Main headquarters
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Endereço Principal", fontSize = 10.sp, color = Color.Gray)
                            Text(address, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            // Configurations list layout
            Text("CONFIGURAÇÕES DA CONTA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
            Column(modifier = Modifier.fillMaxWidth()) {
                StationProfileOptionRow(Icons.Default.Lock, "Alterar Senha") {
                    Toast.makeText(context, "Alteração de senha corporativa!", Toast.LENGTH_SHORT).show()
                }
                StationProfileOptionRow(Icons.Default.Notifications, "Alertas e Notificações") {
                    Toast.makeText(context, "Configurações de alerta salvas!", Toast.LENGTH_SHORT).show()
                }
            }

            // Support help module
            Text("SUPORTE E AJUDA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Column(modifier = Modifier.fillMaxWidth()) {
                StationProfileOptionRow(Icons.Default.Phone, "Falar com Suporte") {
                    Toast.makeText(context, "Abrindo chat de suporte comercial 24h!", Toast.LENGTH_SHORT).show()
                }
                StationProfileOptionRow(Icons.Default.Info, "F.A.Q. Revendedores") {
                    Toast.makeText(context, "Abrindo central de ajuda corporativa!", Toast.LENGTH_SHORT).show()
                }
                StationProfileOptionRow(Icons.Default.Info, "Termos de Uso e Licenciamento") {
                    Toast.makeText(context, "Termos de adesão e distribuição de postos.", Toast.LENGTH_SHORT).show()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout CTA
            Button(
                onClick = {
                    viewModel.logout {
                        Toast.makeText(context, "Sessão encerrada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da Conta", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }

            Text(
                text = "Versão 2.4.0 (Build 549210)",
                fontSize = 11.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StationProfileOptionRow(
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

@Composable
fun StationBottomNavigationBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.MainStationHome,
            onClick = { onTabSelected(Screen.MainStationHome) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Painel", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Simulated actions */ },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Vendas", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Simulated config */ },
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Ajustes", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == Screen.StationProfileArea,
            onClick = { onTabSelected(Screen.StationProfileArea) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Conta", fontSize = 10.sp) }
        )
    }
}

fun formatBraDate(rawDate: String?): String {
    if (rawDate.isNullOrEmpty()) return ""
    if (rawDate.contains("/")) return rawDate
    return try {
        val isoParser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val braFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
        val date = isoParser.parse(rawDate)
        if (date != null) braFormatter.format(date) else rawDate
    } catch (e: Exception) {
        rawDate
    }
}

fun isPromoExpired(rawEndDate: String?): Boolean {
    if (rawEndDate.isNullOrEmpty()) return false
    return try {
        val tz = java.util.TimeZone.getDefault()
        val parser = if (rawEndDate.contains("/")) {
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        }.apply {
            timeZone = tz
        }
        val end = parser.parse(rawEndDate) ?: return false
        
        val today = java.util.Calendar.getInstance(tz).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        end.before(today)
    } catch (e: Exception) {
        false
    }
}

fun isPromoLastDay(rawEndDate: String?): Boolean {
    if (rawEndDate.isNullOrEmpty()) return false
    return try {
        val tz = java.util.TimeZone.getDefault()
        val parser = if (rawEndDate.contains("/")) {
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        }.apply {
            timeZone = tz
        }
        val end = parser.parse(rawEndDate) ?: return false
        
        val endCal = java.util.Calendar.getInstance(tz).apply {
            time = end
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val todayCal = java.util.Calendar.getInstance(tz).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val diffMs = endCal.timeInMillis - todayCal.timeInMillis
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        diffDays == 0L || diffDays == 1L
    } catch (e: Exception) {
        false
    }
}

