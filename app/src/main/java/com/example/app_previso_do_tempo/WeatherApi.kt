package com.example.app_previso_do_tempo


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(

        @Query("q") cityQuery: String,
        // Sua chave API
        @Query("appid") apiKey: String,

        @Query("units") units: String = "metric",

        @Query("lang") lang: String = "pt"
    ): Response<OpenWeatherResponse>
}




data class OpenWeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData
)

data class MainData(
    val temp: Double,
    val humidity: Int,
)

data class WeatherData(
    val description: String,
    val icon: String
)

data class WindData(
    val speed: Double
)


data class WeatherDisplayData(
    val city: String,
    val temperature: Int,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val iconCode: String
)
