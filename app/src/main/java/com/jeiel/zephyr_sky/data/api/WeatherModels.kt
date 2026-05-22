package com.jeiel.zephyr_sky.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class MainTemp(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Double,
    val pressure: Double
)

@JsonClass(generateAdapter = true)
data class Wind(
    val speed: Double,
    val deg: Double
)

@JsonClass(generateAdapter = true)
data class Clouds(
    val all: Int
)

@JsonClass(generateAdapter = true)
data class SysInfo(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherResponse(
    val name: String,
    val weather: List<WeatherDescription>,
    val main: MainTemp,
    val wind: Wind,
    val clouds: Clouds,
    val sys: SysInfo,
    val dt: Long
)

@JsonClass(generateAdapter = true)
data class ForecastItem(
    val dt: Long,
    val main: MainTemp,
    val weather: List<WeatherDescription>,
    val dt_txt: String,
    val wind: Wind
)

@JsonClass(generateAdapter = true)
data class CityInfo(
    val name: String,
    val country: String
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: CityInfo
)

@JsonClass(generateAdapter = true)
data class FineDustInfo(
    val pm10: Int,
    val pm25: Int,
    val grade: String,
    val gradeDesc: String,
    val statusColorHex: String // e.g. Color Hex strings for custom themed indicators
)

