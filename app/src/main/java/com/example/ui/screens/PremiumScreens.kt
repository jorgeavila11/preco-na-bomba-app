package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PrecoNaBombaViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDetailsScreen(
    viewModel: PrecoNaBombaViewModel
) {
    var selectAnnualPlan by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plano Premium", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        modifier = Modifier.testTag("premium_details_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant background banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("EXCLUSIVO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AlertOnYellow)
                    }
                    Text(
                        text = "Seja Premium e economize mais",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Desbloqueie ferramentas avançadas para encontrar o melhor preço em cada gota.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Benefits checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Benefícios Exclusivos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Benefit item row 1
                CardBenefitRow(
                    icon = Icons.Default.Notifications,
                    title = "Alertas de Preço Baixo",
                    subtitle = "Seja o primeiro a saber quando o posto mais próximo baixar os valores."
                )

                // Benefit item row 2
                CardBenefitRow(
                    icon = Icons.Default.Info,
                    title = "Gráficos de Consumo Detalhados",
                    subtitle = "Visualize tendências históricas e otimize seu gasto mensal com precisão."
                )

                // Benefit item row 3
                CardBenefitRow(
                    icon = Icons.Default.Warning,
                    title = "Sem Anúncios",
                    subtitle = "Uma experiência limpa, focada no que importa: sua economia no trajeto."
                )
            }

            // Plan Picker Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Escolha o seu plano",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Monthly Card option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectAnnualPlan = false }
                        .border(
                            2.dp,
                            if (!selectAnnualPlan) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Plano Mensal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Cancele quando quiser, sem taxas.", fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("R$ 9,90", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            Text("/mês", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                }

                // Annual Card option (Best value!)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectAnnualPlan = true }
                        .border(
                            2.dp,
                            if (selectAnnualPlan) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectAnnualPlan) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // "MELHOR VALOR" Ribbon tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(bottomStart = 8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MELHOR VALOR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = AlertOnYellow)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Plano Anual", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("O equivalente a R$ 6,65 por mês.", fontSize = 12.sp, color = Color.Gray)
                                Text("Economize 33% comparado ao mensal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("R$ 79,90", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text("/ano", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Assinar CTA Button
                Button(
                    onClick = {
                        viewModel.setPlan(if (selectAnnualPlan) "Annual" else "Monthly")
                        viewModel.navigateTo(Screen.PaymentCheckout)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assinar Agora", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Text(
                    text = "PAGAMENTO SEGURO VIA APPLE PAY OU GOOGLE PAY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CardBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// 2. Checkout Payment screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutScreen(
    viewModel: PrecoNaBombaViewModel
) {
    val selectedPlanState by viewModel.selectedPlan.collectAsState()
    val payMethodState by viewModel.selectedPaymentMethod.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Credit Card Entry inputs
    var ccNumber by remember { mutableStateOf("") }
    var ccName by remember { mutableStateOf("") }
    var ccExp by remember { mutableStateOf("") }
    var ccCVV by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagamento", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.PremiumDetails) }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky confirm payment action row inside footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // Confirm transaction and set dynamic premium profile badge!
                        viewModel.setProfilePremiumStatus(true)
                        Toast.makeText(context, "Pagamento Confirmado! Plano Premium Ativado com Sucesso.", Toast.LENGTH_LONG).show()
                        viewModel.navigateTo(Screen.DriverProfileArea)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("payment_submit_cta"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Confirmar Pagamento", fontWeight = FontWeight.Black, color = AlertOnYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, tint = AlertOnYellow)
                    }
                }
            }
        },
        modifier = Modifier.testTag("payment_checkout_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Plan summary box
            Text("Resumo do Plano", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedPlanState == "Annual") "Plano Anual" else "Plano Mensal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Selo Premium + Recursos Ativos", fontSize = 12.sp, color = Color.Gray)
                    }

                    Text(
                        text = if (selectedPlanState == "Annual") "R$ 79,90" else "R$ 9,90",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            // Payment Methods Selection trigger
            Text("Forma de Pagamento", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    // PIX Choice as default
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPaymentMethod("PIX") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = payMethodState == "PIX",
                                onClick = { viewModel.setPaymentMethod("PIX") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Aprovação Pix Imediata", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Gera um código copy-paste", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Credit Card Choice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPaymentMethod("CreditCard") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = payMethodState == "CreditCard",
                                onClick = { viewModel.setPaymentMethod("CreditCard") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cartão de Crédito", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Em até 12x sem juros", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.Gray)
                    }
                }
            }

            // Dynamic forms depending on PIX or card select
            if (payMethodState == "PIX") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)

                        Text(
                            "00020126580014BR.GOV.BCB.PIX0136preco-na-bomba-pix-matriz@banco.com520400005303986540510.005802BR5918Preco na Bomba Corp6009Sao Paulo62070503***6304CA21",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("00020126580014BR.GOV.BCB.PIX..."))
                                Toast.makeText(context, "Código PIX copiado para Área de Transferência!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copiar Código Pix", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Secure Card entry forms
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = ccNumber,
                            onValueChange = { ccNumber = it },
                            label = { Text("NÚMERO DO CARTÃO") },
                            placeholder = { Text("0000 0000 0000 0000") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = ccName,
                            onValueChange = { ccName = it },
                            label = { Text("NOME DO TITULAR") },
                            placeholder = { Text("COMO ESTÁ NO CARTÃO") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = ccExp,
                                onValueChange = { ccExp = it },
                                label = { Text("VALIDADE") },
                                placeholder = { Text("MM/AA") },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = ccCVV,
                                onValueChange = { ccCVV = it },
                                label = { Text("CVV") },
                                placeholder = { Text("***") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Security statement rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ambiente seguro e criptografado", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 3. Premium promotions feed matching images
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPromotionsScreen(
    viewModel: PrecoNaBombaViewModel
) {
    val activePromoFilter by viewModel.selectedPromoFilter.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Promocões Premium", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("PREÇO NA BOMBA", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                    }
                },
                actions = {
                    // Badge representation
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AlertOnYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            DriverBottomNavigationBar(Screen.PremiumPromotions) { screen ->
                viewModel.navigateTo(screen)
            }
        },
        modifier = Modifier.testTag("premium_promotions_screen")
    ) { innerPadding ->
        val profileState by viewModel.profile.collectAsState()
        val isPremium = profileState?.isPremium ?: false

        if (!isPremium) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFFEF3C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Desbloquear Premium",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Promoções Exclusivas Premium",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Apenas Motoristas Premium possuem acesso visual a promoções especiais, descontos na bomba e vantagens de parceiros credenciados na plataforma.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Button(
                        onClick = { viewModel.navigateTo(Screen.PremiumDetails) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("promo_upgrade_cta"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Seja Premium por R$ 9,90/mês", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // Horizontal categories filter
            val promoCategories = listOf("Tudo", "Combustível", "Conveniência", "Serviços")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promoCategories) { cat ->
                    val isSelected = activePromoFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.setPromoFilter(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Featured offer card matching image
                item {
                    Text("Ofertas em Destaque", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFCD400), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("EXCLUSIVO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AlertOnYellow)
                                }
                                Text("Expira em 4h 20m", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "R$ 0,20 de desconto por litro",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                "Válido para Gasolina Aditivada em postos selecionados da rede Shell e Ipiranga.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { Toast.makeText(context, "Cupom resgatado! Apresente o QR código na bomba.", Toast.LENGTH_LONG).show() },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Resgatar", fontWeight = FontWeight.Black, color = AlertOnYellow)
                            }
                        }
                    }
                }

                // Neighborhood deals list
                item {
                    Text("Mais Próximas de Você", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                item {
                    promoCardRowItem(
                        category = "Conveniência",
                        title = "Combo Café + Pão de Queijo",
                        place = "Posto Graal - Rod. Castelo Branco",
                        valuePrice = "R$ 7,50",
                        distance = "0.8 km",
                        onAction = { Toast.makeText(context, "Cupom de café resgatado!", Toast.LENGTH_SHORT).show() }
                    )
                }

                item {
                    promoCardRowItem(
                        category = "Serviços",
                        title = "Ducha Grátis na Troca de Óleo",
                        place = "Posto Petrobras - Av. das Nações",
                        valuePrice = "Grátis",
                        distance = "1.2 km",
                        onAction = { Toast.makeText(context, "Resgatado! Ducha grátis liberada.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}
}

@Composable
fun promoCardRowItem(
    category: String,
    title: String,
    place: String,
    valuePrice: String,
    distance: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon layout
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (category == "Conveniência") Icons.Default.Star else Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(distance, fontSize = 10.sp, color = Color.Gray)
                }

                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(top = 2.dp))
                Text(place, fontSize = 11.sp, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(valuePrice, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    Text(
                        text = "Ver detalhes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onAction() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
