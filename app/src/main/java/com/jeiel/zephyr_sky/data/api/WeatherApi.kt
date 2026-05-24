package com.jeiel.zephyr_sky.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Geocoding API. No API key required.
 * Base URL: https://geocoding-api.open-meteo.com/
 */
interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "ko",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}

/**
 * Open-Meteo Forecast API. No API key required.
 * Base URL: https://api.open-meteo.com/
 */
interface ForecastApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code," +
            "wind_speed_10m,wind_direction_10m,surface_pressure,cloud_cover,is_day",
        @Query("hourly") hourly: String =
            "temperature_2m,apparent_temperature,weather_code,relative_humidity_2m,wind_speed_10m",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 2,
        @Query("wind_speed_unit") windSpeedUnit: String = "ms"
    ): OpenMeteoForecastResponse
}

/**
 * Open-Meteo Air Quality API. No API key required.
 * Base URL: https://air-quality-api.open-meteo.com/
 */
interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun current(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "pm10,pm2_5,european_aqi",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoAirQualityResponse
}
