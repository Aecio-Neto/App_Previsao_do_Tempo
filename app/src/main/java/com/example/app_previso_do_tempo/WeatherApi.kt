package com.example.app_previso_do_tempo

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityQuery: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pt"
    ): Response<OpenWeatherResponse>

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") cityQuery: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pt"
    ): Response<ForecastResponse>
}


data class OpenWeatherResponse(
    val name: String,
    val sys: SysData,
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData
)

data class SysData(
    val country: String
)

data class MainData(
    val temp: Double,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
)

data class WeatherData(
    val description: String,
    val icon: String
)

data class WindData(
    val speed: Double
)

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String,
    val main: MainData
)


data class WeatherDisplayData(
    val city: String,
    val country: String,
    val temperature: Int,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val iconCode: String,
    val tempMin: Int,
    val tempMax: Int,
    val dateText: String
)
