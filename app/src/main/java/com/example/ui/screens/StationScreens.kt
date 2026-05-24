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
                        Text("Posto Estrela do Sul", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerenciar Promoções", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    TextButton(onClick = {
                        if (planState == "Conta Pro") {
                            isUpgradeOfferOpen = true
                        } else {
                            isAddPromoOpen = true
                        }
                    }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova", fontWeight = FontWeight.Bold)
                    }
                }

                promos.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.icon) {
                                        "shopping_basket" -> Icons.Default.ShoppingCart
                                        "build" -> Icons.Default.Info
                                        "bed" -> Icons.Default.Home
                                        else -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.category, fontSize = 12.sp, color = Color.Gray)
                            }

                            Text(
                                text = item.value,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
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

    // Dynamic Promo append alert dialog
    if (isAddPromoOpen) {
        var promoTitle by remember { mutableStateOf("") }
        var promoCat by remember { mutableStateOf("Conveniência") }
        var promoVal by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isAddPromoOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewPromotion(promoTitle, promoCat, promoVal)
                        isAddPromoOpen = false
                        Toast.makeText(context, "Nova promoção lançada!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("dialog_save_promo")
                ) {
                    Text("Lançar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddPromoOpen = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Lançar Promoção Premium", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("Nome da Promoção (ex: Combo Pão de Queijo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = promoCat,
                        onValueChange = { promoCat = it },
                        label = { Text("Categoria (Conveniência, Combustível, Serviços)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = promoVal,
                        onValueChange = { promoVal = it },
                        label = { Text("Desconto / Preço (ex: R$ 9,90 ou 10% OFF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
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
    val address by viewModel.editStationAddress.collectAsState()
    val brand by viewModel.editStationBrand.collectAsState()
    val phone by viewModel.editStationPhone.collectAsState()
    val email by viewModel.editStationEmail.collectAsState()
    val context = LocalContext.current

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
                            Text(razao, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
