package com.example.app_previso_do_tempo

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(
    private val api: WeatherService = ApiService.weatherService
) {
    suspend fun fetchWeather(city: String): WeatherDisplayData {
        val cityQuery = city.trim()

        val currentResponse = api.getCurrentWeather(cityQuery, apiKey = API_KEY)
        val forecastResponse = api.getForecast(cityQuery, apiKey = API_KEY)

        if (currentResponse.code() == 404) throw IOException("Cidade não encontrada. Verifique o nome e tente novamente.")
        if (currentResponse.code() == 401) throw IOException("Erro 401: Chave de API inválida ou não ativada.")

        if (currentResponse.isSuccessful && forecastResponse.isSuccessful) {
            val current = currentResponse.body()
            val forecast = forecastResponse.body()

            if (current != null && forecast != null) {
                // 🔹 Calcula data e hora locais com base no timezone da cidade
                val timezoneOffset = current.timezone * 1000L
                val localTimeMillis = System.currentTimeMillis() + timezoneOffset - TimeZone.getDefault().rawOffset
                val cityDate = Date(localTimeMillis)

                val dateFormatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
                val timeFormatter = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

                val dateFormatted = dateFormatter.format(cityDate)
                val timeFormatted = timeFormatter.format(cityDate)


                val cityDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cityDate)
                val todayForecasts = forecast.list.filter { it.dt_txt.startsWith(cityDay) }

                val tempMin = todayForecasts.minOfOrNull { it.main.temp_min }?.toInt() ?: current.main.temp_min.toInt()
                val tempMax = todayForecasts.maxOfOrNull { it.main.temp_max }?.toInt() ?: current.main.temp_max.toInt()
                val rainChance = ((todayForecasts.maxOfOrNull { it.pop ?: 0.0 } ?: 0.0) * 100).toInt()

                return WeatherDisplayData(
                    city = current.name,
                    country = current.sys.country,
                    temperature = current.main.temp.toInt(),
                    description = current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Sem descrição",
                    humidity = current.main.humidity,
                    windSpeed = String.format("%.1f", current.wind.speed * 3.6).toDoubleOrNull() ?: 0.0,
                    iconCode = current.weather.firstOrNull()?.icon ?: "",
                    tempMin = tempMin,
                    tempMax = tempMax,
                    dateText = dateFormatted.replaceFirstChar { it.uppercase() },
                    rainChance = rainChance,
                    localTime = timeFormatted
                )
            }
        }

        throw IOException("Falha ao buscar dados do clima. Verifique sua conexão ou tente novamente.")
    }
}
