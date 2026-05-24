package com.jeiel.zephyr_sky.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Geocoding (https://geocoding-api.open-meteo.com/v1/search) ---

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    @Json(name = "country_code") val countryCode: String?,
    val timezone: String?,
    val admin1: String?
)

// --- Forecast (https://api.open-meteo.com/v1/forecast) ---

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val current: OpenMeteoCurrent?,
    val hourly: OpenMeteoHourly?,
    val daily: OpenMeteoDaily?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    val time: String,
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "wind_direction_10m") val windDirection10m: Double,
    @Json(name = "surface_pressure") val surfacePressure: Double,
    @Json(name = "cloud_cover") val cloudCover: Int,
    @Json(name = "is_day") val isDay: Int
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature2m: List<Double>,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Double>?,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    val time: List<String>,
    val sunrise: List<String>?,
    val sunset: List<String>?
)

// --- Air Quality (https://air-quality-api.open-meteo.com/v1/air-quality) ---

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    val current: OpenMeteoAirQualityCurrent?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityCurrent(
    val time: String,
    val pm10: Double?,
    @Json(name = "pm2_5") val pm25: Double?,
    @Json(name = "european_aqi") val europeanAqi: Double?
)
