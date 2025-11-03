package com.example.app_previso_do_tempo

import java.io.IOException

/**
 * Repositório responsável por buscar dados do clima na OpenWeatherMap.
 */
class WeatherRepository(
    private val api: WeatherService = ApiService.weatherService
) {


    suspend fun fetchWeather(city: String): WeatherDisplayData {

        val cityQuery = "$city,BR"

        val response = api.getCurrentWeather(
            cityQuery = cityQuery,
            apiKey = API_KEY
        )

        if (response.isSuccessful) {
            val apiData = response.body()
            if (apiData != null) {

                return WeatherDisplayData(
                    city = apiData.name,
                    temperature = apiData.main.temp.toInt(),
                    description = apiData.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Sem descrição",
                    humidity = apiData.main.humidity,

                    windSpeed = String.format("%.1f", apiData.wind.speed * 3.6).toDoubleOrNull() ?: 0.0,
                    iconCode = apiData.weather.firstOrNull()?.icon ?: ""
                )
            }
        }


        if (response.code() == 401) {
            throw IOException("Erro 401: Chave de API inválida ou não ativada. Verifique o NetworkModule.kt e aguarde a ativação.")
        }


        throw IOException("Falha ao buscar dados do clima: Código ${response.code()}")
    }
}
