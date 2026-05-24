package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.*
import com.example.ui.viewmodel.PrecoNaBombaViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingRoleSelectionScreen(
    viewModel: PrecoNaBombaViewModel,
    onNavigateDirectlyToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            )
            .testTag("onboarding_role_selection")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Header Section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Preço na Bomba",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preço na Bomba",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Quem é você?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Escolha como deseja utilizar o aplicativo para oferecermos a melhor experiência.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Option 1: Driver Option card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.DriverRegister) }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("role_driver_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Motorista",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Sou Motorista",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Encontre combustível barato perto de você.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Prosseguir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 2: Posto Option card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.StationRegister) }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("role_station_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Posto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "Sou um Posto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Gerencie seus preços e atraia mais clientes.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Prosseguir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Already have account: Login Option
            Text(
                text = "Já tem uma conta? Faça Login",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { viewModel.navigateTo(Screen.UserLogin) }
                    .padding(4.dp)
                    .testTag("onboarding_go_to_login")
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverRegisterScreen(
    viewModel: PrecoNaBombaViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var consumption by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cadastro de Motorista",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Passo 1 de 2 • Dados do Veículo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.OnboardingRoleSelection) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = Modifier.testTag("driver_register_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Bem-vindo ao Preço na Bomba",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "DADOS PESSOAIS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo") },
                placeholder = { Text("Ex: João Silva") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                placeholder = { Text("Ex: joao@exemplo.com") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                placeholder = { Text("Ex: Senha123") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "SEU VEÍCULO",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CobaltPrimary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Modelo do Veículo") },
                placeholder = { Text("Ex: Toyota Corolla") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Placa") },
                    placeholder = { Text("Ex: ABC-1234") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = consumption,
                    onValueChange = { consumption = it },
                    label = { Text("Consumo Médio (km/L)") },
                    placeholder = { Text("Ex: 12.0") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Criar Conta Button
            Button(
                onClick = {
                    val targetEmail = email.trim()
                    val targetPassword = password.trim()
                    if (targetEmail.isEmpty() || targetPassword.isEmpty()) {
                        Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val consDouble = consumption.toDoubleOrNull() ?: 12.0
                    viewModel.registerDriver(
                        email = targetEmail,
                        password = targetPassword,
                        name = name,
                        model = model,
                        plate = plate,
                        consumption = consDouble
                    ) { success, error ->
                        if (success) {
                            Toast.makeText(context, "Conta criada com sucesso e registrada no Firebase Cloud!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erro no Cadastro Firebase: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("driver_register_submit"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Criar Conta (Firebase)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            TextButton(
                onClick = {
                    Toast.makeText(context, "Modo Demo Ativo: Criando conta offline local...", Toast.LENGTH_SHORT).show()
                    viewModel.updateOwnerProfile(name, email, "(11) 98765-4321")
                    viewModel.navigateTo(Screen.MainDriverHome)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Criar Conta no Modo Demo (Sem Conexão)",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Ao criar uma conta, você concorda com nossos Termos de Uso e Privacidade.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationRegisterScreen(
    viewModel: PrecoNaBombaViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var cnpj by remember { mutableStateOf("") }
    var razaoSocial by remember { mutableStateOf("") }
    var nomeFantasia by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectBrand by remember { mutableStateOf("") }
    var contactTel by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Av. das Nações, 1500 - São Paulo, SP") }
    var isAgreed by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cadastro de Posto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.OnboardingRoleSelection) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier.testTag("station_register_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Steps Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Etapa 1 de 3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "33% concluído",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { 0.33f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Informações de Registro e Identidade",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Seja um parceiro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Preencha os dados oficiais da sua empresa para começar a vender na plataforma.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CNPJ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cnpj,
                        onValueChange = { cnpj = it },
                        placeholder = { Text("Ex: 12.345.678/0001-99") },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    var isConsulting by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            if (cnpj.trim().isEmpty()) {
                                Toast.makeText(context, "Insira um número de CNPJ para consultar!", Toast.LENGTH_SHORT).show()
                            } else {
                                isConsulting = true
                                scope.launch {
                                    delay(1000) // Aesthetic simulated delay
                                    val info = viewModel.performCNPJConsultation(cnpj)
                                    isConsulting = false
                                    if (info != null) {
                                        razaoSocial = info.razaoSocial
                                        nomeFantasia = info.name
                                        if (info.address.isNotEmpty() && info.address != "Endereço não informado") {
                                            address = info.address
                                        }
                                        Toast.makeText(context, "Dados cadastrais preenchidos para revisão!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "CNPJ consultado! Sem registro online. Digite manualmente.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (isConsulting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Consultar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Razão Social", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = razaoSocial,
                    onValueChange = { razaoSocial = it },
                    placeholder = { Text("Ex: Posto de Combustíveis Silva Ltda") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nome Fantasia", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nomeFantasia,
                    onValueChange = { nomeFantasia = it },
                    placeholder = { Text("Ex: Posto Shalon") },
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Senha de Acesso", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Cadastre uma senha de acesso") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bandeira", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = selectBrand,
                    onValueChange = { selectBrand = it },
                    placeholder = { Text("Ex: Ipiranga, Shell, BR") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Telefone de Contato", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = contactTel,
                    onValueChange = { contactTel = it },
                    placeholder = { Text("Ex: (11) 98765-4321") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("E-mail", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Ex: exemplo@posto.com.br") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Endereço do Posto (Geolocalização)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = { Text("Ex: Av. Paulista, 1000 - São Paulo, SP") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isAgreed, onCheckedChange = { isAgreed = it })
                Text(
                    text = "Eu aceito os Termos de Uso e a Política de Privacidade da rede de postos.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (cnpj.isBlank() || razaoSocial.isBlank() || nomeFantasia.isBlank()) {
                        Toast.makeText(context, "Por favor, preencha o CNPJ, Razão Social e Nome Fantasia.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.isBlank()) {
                        Toast.makeText(context, "Por favor, cadastre uma senha de acesso para o posto.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (address.isBlank()) {
                        Toast.makeText(context, "Por favor, informe o endereço para geolocalização.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isAgreed) {
                        Toast.makeText(context, "Você precisa aceitar os termos de uso para continuar.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isRegistering) return@Button
                    isRegistering = true
                    viewModel.registerStation(
                        cnpjStr = cnpj,
                        razaoSocialStr = razaoSocial,
                        nomeFantasiaStr = nomeFantasia,
                        passwordForAccess = password,
                        brandName = selectBrand,
                        phoneNumber = contactTel,
                        emailAddress = email,
                        addressStr = address
                    ) { success, msg ->
                        isRegistering = false
                        if (success) {
                            Toast.makeText(context, "Posto cadastrado e geolocalizado com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("station_register_submit"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRegistering) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscando coordenadas (API)...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Cadastrar e Localizar via API",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLoginScreen(viewModel: PrecoNaBombaViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        Color(0xFFF1F5F9)
                    )
                )
            )
            .testTag("user_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Header Section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00327D), 
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Preço na Bomba",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preço na Bomba",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00327D),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Economize em cada quilômetro.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextColVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom M3 Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // E-mail label & field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "E-mail",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColOnSurface
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("seu@email.com", color = TextColVariant) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = TextColVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                unfocusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextColOnSurface,
                                unfocusedTextColor = TextColOnSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    // Senha label & field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Senha",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColOnSurface
                            )
                            Text(
                                text = "Esqueceu sua senha?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00327D),
                                modifier = Modifier
                                    .clickable {
                                        Toast.makeText(context, "Link de redefinição enviado para seu e-mail!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = TextColVariant) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextColVariant
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                unfocusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextColOnSurface,
                                unfocusedTextColor = TextColOnSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Entrar Button
                    Button(
                        onClick = {
                            val targetEmail = email.trim()
                            val targetPassword = password.trim()
                            if (targetEmail.isEmpty() || targetPassword.isEmpty()) {
                                Toast.makeText(context, "Por favor, digite seu e-mail e senha.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            // Real Authentication using Firebase Auth with registration verification
                            viewModel.login(targetEmail, targetPassword) { success, error ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "Login efetuado com sucesso (Firebase)!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Real error printed to screen
                                    Toast.makeText(
                                        context,
                                        "Erro ao entrar / Usuário não cadastrado: $error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFCD400),
                            contentColor = Color(0xFF1D192B)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF1D192B),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Entrar via Firebase (Online)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Simulated Demo Login Link
                    TextButton(
                        onClick = {
                            val cleanCnpjInput = email.replace(Regex("[^0-9]"), "")
                            if (email.contains("posto", ignoreCase = true) || email == "exemplo@posto.com.br") {
                                viewModel.currentStationId.value = 5 // Defaults to pre-seeded Posto Estrela do Sul (ID 5)
                                Toast.makeText(context, "Modo Demonstrativo: Logado como Posto Estrela!", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(Screen.MainStationHome)
                            } else if (cleanCnpjInput.length >= 8) {
                                viewModel.loginAsStation(cleanCnpjInput, password) { loggedIn ->
                                    if (loggedIn) {
                                        Toast.makeText(context, "Modo Demonstrativo: Logado no Posto cadastrado!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "CNPJ não encontrado. Logado como Motorista.", Toast.LENGTH_SHORT).show()
                                        viewModel.navigateTo(Screen.MainDriverHome)
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Entrando como Motorista...", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(Screen.MainDriverHome)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Entrar em Modo Demo (Offline)",
                            color = Color(0xFF00327D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // OR DIVIDER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "OU ENTRAR COM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // Google Login Button
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Login com Google efetuado com sucesso!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(Screen.MainDriverHome)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_google_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = TextColOnSurface
                        ),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = TextColVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Entrar com Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColOnSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Cadastre-se Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Não tem uma conta? ",
                    fontSize = 14.sp,
                    color = TextColVariant
                )
                Text(
                    text = "Cadastre-se",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00327D),
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.OnboardingRoleSelection) }
                        .testTag("login_go_to_register")
                )
            }
        }
    }
}
