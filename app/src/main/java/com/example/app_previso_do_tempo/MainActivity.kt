package com.example.app_previso_do_tempo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF00BFFF),
                background = Color(0xFFE0F7FA)
            )) {
                WeatherAppScreen()
            }
        }
    }
}

// ---------- Estados e ViewModel ----------

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: WeatherDisplayData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    var cityInput by mutableStateOf("Cambé")
    var showAboutDialog by mutableStateOf(false)

    init {
        loadWeather(cityInput)
    }

    fun loadWeather(city: String) {
        if (city.isBlank()) return
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val weather = repository.fetchWeather(city)
                _uiState.value = WeatherUiState.Success(weather)
            } catch (e: IOException) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Erro de rede ou API key inválida.")
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Ocorreu um erro: ${e.message}")
            }
        }
    }
}

// ---------- Tela Principal ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val cityInput = viewModel.cityInput
    val showAboutDialog = viewModel.showAboutDialog

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.showAboutDialog = false },
            nome = "Aécio Flávio de Paula Neto",
            ra = "09047082",
            curso = "Análise e Desenvolvimento de Sistemas" // ✅ Adicione seu curso
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previsão do Tempo", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E90FF)),
                actions = {
                    IconButton(onClick = { viewModel.showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Sobre o App",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF87CEEB), Color(0xFFE0F7FA))
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo e botão de pesquisa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { viewModel.cityInput = it },
                    label = { Text("Nome da Cidade") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.loadWeather(cityInput) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("🔎", fontSize = 20.sp)
                }
            }

            // Exibe os estados
            when (state) {
                WeatherUiState.Loading -> LoadingState()
                is WeatherUiState.Success -> WeatherCard(data = (state as WeatherUiState.Success).data)
                is WeatherUiState.Error -> ErrorState(message = (state as WeatherUiState.Error).message)
            }
        }
    }
}

// ---------- Diálogo "Sobre" ----------

@Composable
fun AboutDialog(onDismiss: () -> Unit, nome: String, ra: String, curso: String) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sobre o Aplicativo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E90FF)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Desenvolvido por:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Text(nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(8.dp))
                Text("RA:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Text(ra, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Curso:", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Text(curso, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}

// ---------- Estados da tela ----------

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun ErrorState(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5757).copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ Ocorreu um erro",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (!message.contains("Cidade não encontrada", ignoreCase = true)) {
                Text(
                    "Dica: Verifique sua chave API no arquivo NetworkModule.kt (erro 401).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ---------- Cartão do clima ----------

@Composable
fun WeatherCard(data: WeatherDisplayData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(top = 32.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${data.city}, ${data.country}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E90FF)
            )

            Text(
                text = "${data.dateText} • ${data.localTime}h",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val emoji = when {
                    data.description.contains("chuva", ignoreCase = true) -> "🌧️"
                    data.description.contains("sol", ignoreCase = true) || data.description.contains("limpo", ignoreCase = true) -> "☀️"
                    data.description.contains("nuvens", ignoreCase = true) -> "☁️"
                    else -> "❓"
                }
                Text(text = emoji, fontSize = 60.sp, modifier = Modifier.padding(end = 16.dp))
                Text(
                    text = "${data.temperature}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Máx: ${data.tempMax}°C  |  Mín: ${data.tempMin}°C",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.description,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0xFFDDDDDD), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailRow(icon = "💧", label = "Umidade", value = "${data.humidity}%")
            Spacer(modifier = Modifier.height(12.dp))
            WeatherDetailRow(icon = "💨", label = "Vento", value = "${data.windSpeed} km/h")
            Spacer(modifier = Modifier.height(12.dp))
            WeatherDetailRow(icon = "🌧️", label = "Chuva", value = "${data.rainChance}%")
        }
    }
}

@Composable
fun WeatherDetailRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )
    }
}
