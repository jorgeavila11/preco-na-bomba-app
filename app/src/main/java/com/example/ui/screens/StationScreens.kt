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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStationHome(
    viewModel: PrecoNaBombaViewModel
) {
    val context = LocalContext.current
    var isAddPromoOpen by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<PromoItem?>(null) }
    var isUpgradeOfferOpen by remember { mutableStateOf(false) }

    var deactivatingPromo by remember { mutableStateOf<PromoItem?>(null) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var selectedJustificationIndex by remember { mutableStateOf(-1) }
    var customJustificationText by remember { mutableStateOf("") }

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
    val hasEvCharger by viewModel.editStationHasEvCharger.collectAsState()

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

                // EV Charging spot block
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ev_charger_toggle_card"),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("⚡ PONTO DE RECARGA ELÉTRICA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Posto tem local para recarregar carros elétricos", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }

                        Switch(
                            checked = hasEvCharger,
                            onCheckedChange = { viewModel.editStationHasEvCharger.value = it },
                            modifier = Modifier.testTag("ev_charger_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            )
                        )
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
            selected = currentScreen == Screen.StationSales,
            onClick = { onTabSelected(Screen.StationSales) },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Vendas", fontSize = 10.sp) }
        )

        NavigationBarItem(
            selected = currentScreen == Screen.StationPromotions,
            onClick = { onTabSelected(Screen.StationPromotions) },
            icon = { Icon(Icons.Default.Star, null) },
            label = { Text("Promoções", fontSize = 10.sp) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationPromotionsScreen(
    viewModel: PrecoNaBombaViewModel
) {
    val context = LocalContext.current
    var isAddPromoOpen by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<PromoItem?>(null) }
    var isUpgradeOfferOpen by remember { mutableStateOf(false) }

    var deactivatingPromo by remember { mutableStateOf<PromoItem?>(null) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var selectedJustificationIndex by remember { mutableStateOf(-1) }
    var customJustificationText by remember { mutableStateOf("") }

    // Subscription status
    val planState by viewModel.ownerStationPlan.collectAsState()
    val promos by viewModel.promoList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Promoções", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.MainStationHome) }) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            StationBottomNavigationBar(Screen.StationPromotions) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("station_promotions_screen")
    ) { innerPadding ->
        if (planState == "Conta Premium") {
            // Premium/Partner Screen: Full active promotion control
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card showcasing partner status
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text("Painel de Parceiro Ativo ✨", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF15803D))
                            Text("Publique promoções de combustíveis, itens de conveniência ou serviços gerais da sua loja diretamente para milhares de motoristas.", fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Gerenciar Promoções",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Suas Promoções", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    TextButton(onClick = {
                        editingPromo = null
                        isAddPromoOpen = true
                    }) {
                        Text("Publicar Nova", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 14.sp)
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Star, "Vazio", tint = Color.LightGray, modifier = Modifier.size(48.dp).padding(bottom = 12.dp))
                                Text(
                                    text = "Nenhuma promoção cadastrada ainda.\nToque em 'Publicar Nova' acima para começar!",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
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

                                // Row 2: Date period & Action Buttons
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
                                        if (item.isDeactivated) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Encerrada",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFEF4444)
                                                    )
                                                }
                                                if (!item.deactivationJustification.isNullOrBlank()) {
                                                    Text(
                                                        text = item.deactivationJustification,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFEF4444),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        } else if (expired) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Expirada",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!item.isDeactivated && !expired) {
                                            IconButton(
                                                onClick = {
                                                    deactivatingPromo = item
                                                    showDeactivateDialog = true
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Encerrar",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

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
        } else {
            // Conta Pro / Non-Partner Upgrade Screen (Inspirational, high-fidelity landing page!)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large attractive Premium Diamond Icon / Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Feature",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Área de Promoções e Parcerias",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lançar cupons e vantagens diretamente no mapa dos motoristas é um recurso exclusivo das redes parceiras.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Beautiful custom bulleted cards for premium features
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumBenefitRow(
                        title = "Cupons de desconto no mapa",
                        desc = "Seus cupons de combustível ou conveniência aparecem para motoristas rodando próximos."
                    )
                    PremiumBenefitRow(
                        title = "Destaque Visual & Selo Parceiro 💎",
                        desc = "Seu posto ganha um selo diferenciado dourado no mapa, com prioridade de ranqueamento."
                    )
                    PremiumBenefitRow(
                        title = "Atração de Clientes 4x Maior",
                        desc = "Estabelecimentos parceiros têm aumento comprovado de motoristas por conta de promoções exclusivas."
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Beautiful Call to Action buy/activation button
                Button(
                    onClick = {
                        viewModel.ownerStationPlan.value = "Conta Premium"
                        Toast.makeText(context, "Parabéns! Plano Parceiro Premium ativado com sucesso! 🎉", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Seja Parceiro Premium", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFBBF24))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ativação instantânea • Sem fidelidade contratual por R$ 99,90/mês",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Deactivation Dialog
    if (showDeactivateDialog && deactivatingPromo != null) {
        val justifications = listOf(
            "Estoque esgotado (lote promocional finalizado)",
            "Aguardando reabastecimento",
            "Meta de vendas da campanha atingida",
            "Encerramento antecipado por decisão do posto",
            "Sistema de desconto fora do ar temporariamente",
            "Brindes ou itens da promoção esgotados",
            "Outro motivo (justificativa digitada pelo posto)"
        )

        AlertDialog(
            onDismissRequest = {
                showDeactivateDialog = false
                selectedJustificationIndex = -1
                customJustificationText = ""
            },
            title = {
                Text(
                    text = "Encerrar Promoção",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Selecione uma justificativa para encerrar a promoção \"${deactivatingPromo?.title}\":",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )

                    justifications.forEachIndexed { index, option ->
                        val isSelected = selectedJustificationIndex == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedJustificationIndex = index }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedJustificationIndex = index }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    if (selectedJustificationIndex == justifications.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Digite o motivo personalizado:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        OutlinedTextField(
                            value = customJustificationText,
                            onValueChange = { customJustificationText = it },
                            placeholder = { Text("Justificativa...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (selectedJustificationIndex == justifications.lastIndex) {
                            customJustificationText.trim()
                        } else if (selectedJustificationIndex >= 0) {
                            justifications[selectedJustificationIndex]
                        } else {
                            ""
                        }

                        if (finalReason.isEmpty()) {
                            Toast.makeText(context, "Por favor, defina uma justificativa.", Toast.LENGTH_SHORT).show()
                        } else {
                            deactivatingPromo?.let {
                                viewModel.deactivatePromotion(it, finalReason)
                                Toast.makeText(context, "Promoção encerrada com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            showDeactivateDialog = false
                            selectedJustificationIndex = -1
                            customJustificationText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmar Encerramento", color = Color.White, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeactivateDialog = false
                        selectedJustificationIndex = -1
                        customJustificationText = ""
                    }
                ) {
                    Text("Cancelar", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // Add Promo Dialog
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
                if (braDate.contains("-")) return braDate
                val formatBra = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                val formatIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val parsed = formatBra.parse(braDate)
                if (parsed != null) formatIso.format(parsed) else braDate
            } catch (e: Exception) {
                braDate
            }
        }

        AlertDialog(
            onDismissRequest = { isAddPromoOpen = false },
            title = {
                Text(
                    text = if (editingPromo == null) "Publicar Nova Promoção" else "Editar Promoção",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("Nome da Promoção ou Oferta", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_promo_title")
                    )

                    OutlinedTextField(
                        value = promoDesc,
                        onValueChange = { promoDesc = it },
                        label = { Text("Breve Descrição / Destaques da Oferta", fontSize = 13.sp) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = promoPriceStr,
                            onValueChange = { promoPriceStr = it },
                            label = { Text("Preço R$ (Opcional)", fontSize = 13.sp) },
                            placeholder = { Text("Ex: 15,90") },
                            modifier = Modifier.weight(1f).testTag("dialog_promo_price"),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = promoCat,
                                onValueChange = {},
                                label = { Text("Categoria", fontSize = 13.sp) },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                        Icon(
                                            imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker { promoStartDate = it } }
                        ) {
                            OutlinedTextField(
                                value = promoStartDate,
                                onValueChange = {},
                                label = { Text("Início", fontSize = 13.sp) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = Color.Gray
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker { promoEndDate = it } }
                        ) {
                            OutlinedTextField(
                                value = promoEndDate,
                                onValueChange = {},
                                label = { Text("Término", fontSize = 13.sp) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.Gray,
                                    disabledLabelColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoTitle.trim().isEmpty()) {
                            Toast.makeText(context, "Nome é obrigatório!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
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
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (editingPromo == null) "Publicar" else "Salvar Alterações", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddPromoOpen = false }) {
                    Text("Cancelar", fontSize = 14.sp)
                }
            }
        )
    }
}

@Composable
fun PremiumBenefitRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF1E3A8A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 12.sp, color = Color(0xFF64748B), lineHeight = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSalesScreen(viewModel: PrecoNaBombaViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()

    var timeRange by remember { mutableStateOf("Últimos 30 dias") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val rawSalesList by viewModel.salesList.collectAsState()
    val rawRedemptionsList by viewModel.redemptionsList.collectAsState()

    // Trigger loading of sales and redemptions when screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchSales()
        viewModel.fetchRedemptions()
    }

    val filteredSalesList = remember(rawSalesList, timeRange) {
        val now = System.currentTimeMillis()
        val filterDurationMs = when (timeRange) {
            "Hoje" -> 24L * 60 * 60 * 1000 // Today
            "Últimos 7 dias" -> 7L * 24 * 60 * 60 * 1000
            else -> 30L * 24 * 60 * 60 * 1000 // 30 days
        }
        rawSalesList.filter { (now - it.timestamp) <= filterDurationMs }
    }

    val filteredRedemptionsList = remember(rawRedemptionsList, timeRange) {
        val now = System.currentTimeMillis()
        val filterDurationMs = when (timeRange) {
            "Hoje" -> 24L * 60 * 60 * 1000 // Today
            "Últimos 7 dias" -> 7L * 24 * 60 * 60 * 1000
            else -> 30L * 24 * 60 * 60 * 1000 // 30 days
        }
        rawRedemptionsList.filter { (now - it.timestamp) <= filterDurationMs }
    }

    // Calculations based on filtered sales
    val totalSalesDouble = filteredSalesList.sumOf { it.amount }
    val totalSalesFormatted = String.format("R$ %,.2f", totalSalesDouble).replace('.', 'X').replace(',', '.').replace('X', ',')
    val totalSales = totalSalesFormatted.substringBefore(",")
    val totalSalesFraction = if (totalSalesFormatted.contains(",")) "," + totalSalesFormatted.substringAfter(",") else ",00"

    val performancePercentage = when (timeRange) {
        "Últimos 30 dias" -> "+12.4%"
        "Últimos 7 dias" -> "+8.7%"
        else -> "+4.2%"
    }
    val performanceSub = when (timeRange) {
        "Últimos 30 dias" -> "Performance acima da média mensal"
        "Últimos 7 dias" -> "Performance acima da semana anterior"
        else -> "Performance diária dentro do esperado"
    }

    val ticketMedioDouble = if (filteredSalesList.isNotEmpty()) totalSalesDouble / filteredSalesList.size else 0.0
    val ticketMedioVal = String.format("R$ %,.2f", ticketMedioDouble).replace('.', 'X').replace(',', '.').replace('X', ',')
    val ticketMedioSub = when (timeRange) {
        "Últimos 30 dias" -> "⬇ 2.1% vs ant."
        "Últimos 7 dias" -> "⬆ 1.5% vs ant."
        else -> "⬆ 0.8% vs ant."
    }
    val ticketMedioColor = when (timeRange) {
        "Últimos 30 dias" -> Color(0xFFEF4444) // red
        else -> Color(0xFF2563EB) // blue-green
    }

    val promoSalesCount = filteredSalesList.count { !it.promotionTitle.isNullOrBlank() }
    val conversionPercentage = if (filteredSalesList.isNotEmpty()) (promoSalesCount.toDouble() / filteredSalesList.size) * 100.0 else 32.5
    val conversionVal = String.format("%.1f%%", conversionPercentage).replace('.', ',')
    val conversionSub = when (timeRange) {
        "Últimos 30 dias" -> "⬆ 5.4% vs ant."
        "Últimos 7 dias" -> "⬆ 2.1% vs ant."
        else -> "⬇ 1.2% vs ant."
    }
    val conversionColor = when (timeRange) {
        "Hoje" -> Color(0xFFEF4444) // red
        else -> Color(0xFF2563EB) // blue-green
    }

    // Pie chart segments (combustível, conveniência, serviços)
    val totalFuel = filteredSalesList.filter { it.category == "Combustível" }.sumOf { it.amount }
    val totalConvenience = filteredSalesList.filter { it.category == "Conveniência" }.sumOf { it.amount }
    val totalServices = filteredSalesList.filter { it.category == "Serviços" }.sumOf { it.amount }
    
    val denom = if (totalSalesDouble > 0) totalSalesDouble else 1.0
    val combustivelPct = (totalFuel / denom).toFloat()
    val convenienciaPct = (totalConvenience / denom).toFloat()
    val servicosPct = (totalServices / denom).toFloat()

    // Promo 1 - Gasolina Premium - R$0,20 OFF
    val promo1TotalAmount = filteredSalesList.filter { it.promotionTitle == "Gasolina Premium - R$0,20 OFF" }.sumOf { it.amount }
    val promo1Count = filteredSalesList.count { it.promotionTitle == "Gasolina Premium - R$0,20 OFF" }
    val promo1Value = String.format("R$ %,.2f", promo1TotalAmount).replace('.', 'X').replace(',', '.').replace('X', ',').substringBefore(",")
    val promo1Progress = (promo1Count.toFloat() / 1000f).coerceIn(0f, 1f)
    val promo1Counter = "$promo1Count cupons usados"

    // Promo 2 - Combo Café + Pão de Queijo
    val promo2TotalAmount = filteredSalesList.filter { it.promotionTitle == "Combo Café + Pão de Queijo" }.sumOf { it.amount }
    val promo2Count = filteredSalesList.count { it.promotionTitle == "Combo Café + Pão de Queijo" }
    val promo2Value = String.format("R$ %,.2f", promo2TotalAmount).replace('.', 'X').replace(',', '.').replace('X', ',').substringBefore(",")
    val promo2Progress = (promo2Count.toFloat() / 800f).coerceIn(0f, 1f)
    val promo2Counter = "$promo2Count pedidos"

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.MainStationHome) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vendas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                IconButton(
                    onClick = { Toast.makeText(context, "Nenhuma notificação recente.", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = Color(0xFF1E293B)
                    )
                }
            }
        },
        bottomBar = {
            StationBottomNavigationBar(
                currentScreen = Screen.StationSales,
                onTabSelected = { viewModel.navigateTo(it) }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Choice & Floating Action Funnel Button in a single horizontal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Box Pill
                Box {
                    Button(
                        onClick = { isDropdownExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = timeRange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Últimos 30 dias", "Últimos 7 dias", "Hoje").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    timeRange = option
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Funnel indicator button circle shape matching image precisely
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF032B70), CircleShape)
                        .clickable {
                            Toast.makeText(context, "Filtro avançado no plano Premium!", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.width(14.dp).height(2.dp).background(Color.White))
                        Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color.White))
                        Box(modifier = Modifier.width(6.dp).height(2.dp).background(Color.White))
                    }
                }
            }

            // PDV Simulator Panel for Firestore feeding demonstration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulador de Vendas do PDV (Integrado ao Firebase)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E40AF),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Toque abaixo para simular uma transação de PDV em tempo real no seu posto. Cada clique envia uma venda real para o Firestore no Firebase, atualizando instantaneamente os gráficos desta tela!",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 15.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.addNewSale(
                                    category = "Combustível",
                                    amount = 180.0,
                                    promotionTitle = "Gasolina Premium - R$0,20 OFF"
                                )
                                Toast.makeText(context, "Transação de Combustível enviada ao Firebase!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF033F9E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ R$180 Gás", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.addNewSale(
                                    category = "Conveniência",
                                    amount = 25.0,
                                    promotionTitle = "Combo Café + Pão de Queijo"
                                )
                                Toast.makeText(context, "Pedido do Café enviado ao Firebase!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ R$25 Café", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF713F12))
                        }

                        Button(
                            onClick = {
                                viewModel.addNewSale(
                                    category = "Serviços",
                                    amount = 120.0,
                                    promotionTitle = "Troca de Óleo"
                                )
                                Toast.makeText(context, "Serviço enviado ao Firebase!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ R$120 Serv.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Client Redemptions Live Panel synchronized with Firestore
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // Light elegant green background
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Últimos Resgates de Clientes (Firestore)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                letterSpacing = 0.5.sp
                            )
                        }
                        
                        Text(
                            text = "${filteredRedemptionsList.size} resgates",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }

                    Text(
                        text = "Exibição em tempo real de motoristas que resgataram descontos pelo aplicativo. Quando um motorista clica em resgatar no clube de benefícios, o cupom aparece aqui instantaneamente!",
                        fontSize = 11.sp,
                        color = Color(0xFF14532D),
                        lineHeight = 15.sp
                    )

                    HorizontalDivider(color = Color(0xFFDCFCE7), thickness = 1.dp)

                    if (filteredRedemptionsList.isEmpty()) {
                        Text(
                            text = "Nenhum resgate registrado para o período filtrado.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Display top 3 recent redemptions dynamically
                        filteredRedemptionsList.sortedByDescending { it.timestamp }.take(3).forEach { redemption ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = redemption.driverEmail,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = if (redemption.redemptionCode.isNotEmpty()) "${redemption.promotionTitle} • ${redemption.redemptionCode}" else redemption.promotionTitle,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (redemption.status == "Utilizado") Color(0xFFDBEAFE) else Color(0xFFFEF08A),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = redemption.status.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (redemption.status == "Utilizado") Color(0xFF1E40AF) else Color(0xFF854D0E)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Total de Vendas Card - linear gradient blue box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F42AD), Color(0xFF02256B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total de Vendas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF08A), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = performancePercentage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF713F12)
                                )
                            }
                        }

                        // Large formatted sales number
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = totalSales,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = totalSalesFraction,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Bottom trend indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = performanceSub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Ticket Médio and Conversão dual card columns row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ticket Médio Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "TICKET MÉDIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = ticketMedioVal,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = ticketMedioSub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ticketMedioColor
                        )
                    }
                }

                // Conversão Card with left golden/yellow accent strip
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Left 4.dp gold accent border strip internally embedded inside card
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(Color(0xFFEAB308))
                        )
                        // Content column
                        Column(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CONVERSÃO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = conversionVal,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = conversionSub,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = conversionColor
                            )
                        }
                    }
                }
            }

            // Vendas por Categoria Card with real canvas donut chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Header with blue vertical bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(Color(0xFF033F9E), RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = "Vendas por Categoria",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Content layout dual columns: left side Canvas chart, right side Legends
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Double overlapping boxes for Donut center text
                        Box(
                            modifier = Modifier
                                .size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Donut canvas drawing
                            Canvas(modifier = Modifier.size(110.dp)) {
                                val strokeWidthPx = 18.dp.toPx()
                                val parentSize = size
                                val boxRadius = Math.min(parentSize.width, parentSize.height) - strokeWidthPx

                                // Legend segments
                                // segment 1: Combustivel
                                drawArc(
                                    color = Color(0xFF033F9E),
                                    startAngle = -90f,
                                    sweepAngle = 360f * combustivelPct,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                                )
                                // segment 2: Conveniência
                                drawArc(
                                    color = Color(0xFFFACC15),
                                    startAngle = -90f + (360f * combustivelPct),
                                    sweepAngle = 360f * convenienciaPct,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                                )
                                // segment 3: Serviços
                                drawArc(
                                    color = Color(0xFFCBD5E1),
                                    startAngle = -90f + (360f * combustivelPct) + (360f * convenienciaPct),
                                    sweepAngle = 360f * servicosPct,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                                )
                            }

                            // Inner central text card overlap
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "TOTAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "100%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        // Right hand Legends matching the original layout beautifully
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            // Fuel category row
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(10.dp)
                                        .background(Color(0xFF033F9E), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "COMBUSTÍVEL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = String.format("%.0f%%", combustivelPct * 100),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            // Convenience retail row
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(10.dp)
                                        .background(Color(0xFFFACC15), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "CONVENIÊNCIA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = String.format("%.0f%%", convenienciaPct * 100),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            // Auto/Mechanical Service row
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(10.dp)
                                        .background(Color(0xFFCBD5E1), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "SERVIÇOS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = String.format("%.0f%%", servicosPct * 100),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Desempenho de Promoções title + list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Desempenho de Promoções",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.StationPromotions) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Ver todas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF033F9E)
                        )
                    }
                }

                // Promo Item 1 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Yellow background icon frame (matches gas theme)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFFDE047), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF713F12),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Gasolina Premium - R$0,20 OFF",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = promo1Value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF033F9E)
                            )
                        }

                        // Progress slider indicator
                        LinearProgressIndicator(
                            progress = promo1Progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF033F9E),
                            trackColor = Color(0xFFF1F5F9)
                        )

                        // Bottom counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = promo1Counter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Meta: 1.000",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Promo Item 2 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Light blue/indigo coffee icon background
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color(0xFF1D4ED8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Combo Café + Pão de Queijo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = promo2Value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF033F9E)
                            )
                        }

                        // Progress slider indicator
                        LinearProgressIndicator(
                            progress = promo2Progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFACC15),
                            trackColor = Color(0xFFF1F5F9)
                        )

                        // Bottom counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = promo2Counter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Meta: 800",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Tip Optimization Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Top gold accent line bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFFFACC15))
                            .align(Alignment.TopCenter)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Otimize seus ganhos hoje.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Baseado no fluxo de hoje, sugerimos ativar a promoção de 'Troca de Óleo' entre 14h e 16h.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                        }

                        // Beautiful generated oil illustration card
                        Image(
                            painter = painterResource(id = com.example.R.drawable.refuel_oil_tip),
                            contentDescription = "Dica de Otimização",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}


