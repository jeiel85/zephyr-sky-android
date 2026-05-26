package com.jeiel.zephyr_sky.data.repository

import com.jeiel.zephyr_sky.data.api.AirQualityApi
import com.jeiel.zephyr_sky.data.api.CityInfo
import com.jeiel.zephyr_sky.data.api.Clouds
import com.jeiel.zephyr_sky.data.api.CurrentWeatherResponse
import com.jeiel.zephyr_sky.data.api.FineDustInfo
import com.jeiel.zephyr_sky.data.api.ForecastApi
import com.jeiel.zephyr_sky.data.api.ForecastItem
import com.jeiel.zephyr_sky.data.api.ForecastResponse
import com.jeiel.zephyr_sky.data.api.GeocodingApi
import com.jeiel.zephyr_sky.data.api.GeocodingResult
import com.jeiel.zephyr_sky.data.api.MainTemp
import com.jeiel.zephyr_sky.data.api.SysInfo
import com.jeiel.zephyr_sky.data.api.Wind
import com.jeiel.zephyr_sky.data.api.WeatherDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class WeatherRepository(
    geocodingApi: GeocodingApi? = null,
    forecastApi: ForecastApi? = null,
    airQualityApi: AirQualityApi? = null
) {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private val geocoding: GeocodingApi = geocodingApi ?: buildApi(
        baseUrl = "https://geocoding-api.open-meteo.com/",
        clazz = GeocodingApi::class.java
    )

    private val forecast: ForecastApi = forecastApi ?: buildApi(
        baseUrl = "https://api.open-meteo.com/",
        clazz = ForecastApi::class.java
    )

    private val airQuality: AirQualityApi = airQualityApi ?: buildApi(
        baseUrl = "https://air-quality-api.open-meteo.com/",
        clazz = AirQualityApi::class.java
    )

    fun isApiKeyConfigured(): Boolean = true

    suspend fun getCurrentWeather(cityName: String): CurrentWeatherResponse = withContext(Dispatchers.IO) {
        val place = resolvePlace(cityName)
        getCurrentWeatherForPlace(place)
    }

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        displayName: String,
        countryCode: String? = null
    ): CurrentWeatherResponse = withContext(Dispatchers.IO) {
        getCurrentWeatherForPlace(
            WeatherPlace(
                name = displayName.ifBlank { "현재 위치" },
                latitude = latitude,
                longitude = longitude,
                country = countryCode.orEmpty(),
                countryCode = countryCode
            )
        )
    }

    private suspend fun getCurrentWeatherForPlace(place: WeatherPlace): CurrentWeatherResponse {
        val resp = forecast.forecast(latitude = place.latitude, longitude = place.longitude)
        val cur = resp.current
            ?: throw IllegalStateException("Open-Meteo 응답에 'current'가 없습니다: ${place.name}")
        val tz = resolveTimeZone(resp.timezone)

        val conditionId = wmoToConditionId(cur.weatherCode)
        val (conditionMain, conditionDesc) = wmoToMainAndDesc(cur.weatherCode)
        val icon = wmoToIcon(cur.weatherCode, cur.isDay == 1)
        val firstDailyDate = resp.daily?.time?.firstOrNull()
        val sunriseEpoch = parseLocalDateTimeToEpoch(resp.daily?.sunrise?.firstOrNull(), tz)
            ?: epochAtLocalTime(firstDailyDate, 6, 0, tz)
        val sunsetEpoch = parseLocalDateTimeToEpoch(resp.daily?.sunset?.firstOrNull(), tz)
            ?: epochAtLocalTime(firstDailyDate, 18, 0, tz)

        return CurrentWeatherResponse(
            name = place.name,
            weather = listOf(
                WeatherDescription(
                    id = conditionId,
                    main = conditionMain,
                    description = conditionDesc,
                    icon = icon
                )
            ),
            main = MainTemp(
                temp = cur.temperature2m,
                feels_like = cur.apparentTemperature,
                temp_min = cur.temperature2m - 2.0,
                temp_max = cur.temperature2m + 2.0,
                humidity = cur.relativeHumidity2m,
                pressure = cur.surfacePressure
            ),
            wind = Wind(speed = cur.windSpeed10m, deg = cur.windDirection10m),
            clouds = Clouds(all = cur.cloudCover),
            sys = SysInfo(
                country = place.countryCode ?: place.country.orEmpty(),
                sunrise = sunriseEpoch,
                sunset = sunsetEpoch
            ),
            dt = parseLocalDateTimeToEpoch(cur.time, tz) ?: nowEpochSeconds()
        )
    }

    suspend fun getFineDust(cityName: String): FineDustInfo = withContext(Dispatchers.IO) {
        val place = resolvePlace(cityName)
        getFineDustForPlace(place)
    }

    suspend fun getFineDust(latitude: Double, longitude: Double): FineDustInfo = withContext(Dispatchers.IO) {
        getFineDustForPlace(
            WeatherPlace(
                name = "현재 위치",
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    private suspend fun getFineDustForPlace(place: WeatherPlace): FineDustInfo {
        val resp = airQuality.current(latitude = place.latitude, longitude = place.longitude)
        val cur = resp.current
        val pm10 = cur?.pm10?.toInt() ?: 0
        val pm25 = cur?.pm25?.toInt() ?: 0
        val aqi = cur?.europeanAqi ?: pm10.toDouble()

        val (grade, gradeDesc, colorHex) = when {
            aqi <= 20.0 -> Triple(
                "좋음",
                "대기질 상쾌함. 맑고 깨끗해 마음 놓고 활동할 수 있습니다.",
                "0xFF34A853"
            )
            aqi <= 40.0 -> Triple(
                "보통",
                "보통의 마일드한 상태입니다. 실내 환기도 원활히 가능한 수준입니다.",
                "0xFF435E91"
            )
            aqi <= 60.0 -> Triple(
                "나쁨",
                "민감군은 호흡기 질환 예방을 위해 오랜 실외 일정을 축소하세요.",
                "0xFFFE8C00"
            )
            else -> Triple(
                "매우나쁨",
                "황사/초미세경보 최고조! 무리한 유산소 구보나 실외 노출을 중지하세요.",
                "0xFFD32F2F"
            )
        }

        return FineDustInfo(
            pm10 = pm10,
            pm25 = pm25,
            grade = grade,
            gradeDesc = gradeDesc,
            statusColorHex = colorHex
        )
    }

    suspend fun getForecast(cityName: String): ForecastResponse = withContext(Dispatchers.IO) {
        val place = resolvePlace(cityName)
        getForecastForPlace(place)
    }

    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        displayName: String,
        countryCode: String? = null
    ): ForecastResponse = withContext(Dispatchers.IO) {
        getForecastForPlace(
            WeatherPlace(
                name = displayName.ifBlank { "현재 위치" },
                latitude = latitude,
                longitude = longitude,
                country = countryCode.orEmpty(),
                countryCode = countryCode
            )
        )
    }

    private suspend fun getForecastForPlace(place: WeatherPlace): ForecastResponse {
        val resp = forecast.forecast(latitude = place.latitude, longitude = place.longitude)
        val hourly = resp.hourly
            ?: throw IllegalStateException("Open-Meteo 응답에 'hourly'가 없습니다: ${place.name}")
        val tz = resolveTimeZone(resp.timezone)

        val nowEpoch = nowEpochSeconds()
        val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = tz
        }

        // Find the first slot at or near "now", then stride 3 hours, up to 8 slots total.
        val firstFutureIndex = hourly.time.indexOfFirst { iso ->
            val epoch = parseLocalDateTimeToEpoch(iso, tz)
            epoch != null && epoch >= nowEpoch - 1800
        }.coerceAtLeast(0)

        val stride = 3
        val selected = mutableListOf<Int>()
        var cursor = firstFutureIndex
        while (selected.size < 8 && cursor < hourly.time.size) {
            selected.add(cursor)
            cursor += stride
        }

        val items = selected.map { idx ->
            val code = hourly.weatherCode.getOrElse(idx) { 0 }
            val temp = hourly.temperature2m.getOrElse(idx) { 0.0 }
            val feels = hourly.apparentTemperature.getOrElse(idx) { temp }
            val humidity = hourly.relativeHumidity2m?.getOrElse(idx) { 60.0 } ?: 60.0
            val wind = hourly.windSpeed10m?.getOrElse(idx) { 2.0 } ?: 2.0
            val iso = hourly.time[idx]
            val epoch = parseLocalDateTimeToEpoch(iso, tz) ?: nowEpoch
            val (mainText, descText) = wmoToMainAndDesc(code)

            ForecastItem(
                dt = epoch,
                main = MainTemp(
                    temp = temp,
                    feels_like = feels,
                    temp_min = temp - 1.5,
                    temp_max = temp + 1.5,
                    humidity = humidity,
                    pressure = 1013.0
                ),
                weather = listOf(
                    WeatherDescription(
                        id = wmoToConditionId(code),
                        main = mainText,
                        description = descText,
                        icon = wmoToIcon(code, isDayHour(epoch, tz))
                    )
                ),
                dt_txt = displayFormat.format(Date(epoch * 1000)),
                wind = Wind(speed = wind, deg = 180.0)
            )
        }

        return ForecastResponse(
            list = items,
            city = CityInfo(
                name = place.name,
                country = place.countryCode ?: place.country.orEmpty()
            )
        )
    }

    // --- helpers ---

    private suspend fun resolvePlace(cityName: String): WeatherPlace {
        val trimmed = cityName.trim()
        val normalized = normalizeCitySearchQuery(trimmed)
        val response = geocoding.search(name = normalized)
        val result = response.results?.firstOrNull()
            ?: if (normalized != trimmed) {
                geocoding.search(name = trimmed).results?.firstOrNull()
            } else {
                null
            }
            ?: throw IllegalArgumentException("도시를 찾을 수 없습니다: $cityName")
        return result.toWeatherPlace()
    }

    private fun <T> buildApi(baseUrl: String, clazz: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(clazz)
    }

    private fun resolveTimeZone(zone: String?): TimeZone {
        if (zone.isNullOrBlank()) return TimeZone.getTimeZone("UTC")
        val resolved = TimeZone.getTimeZone(zone)
        // TimeZone.getTimeZone falls back to GMT for unknown ids — accept either GMT or matching id.
        return resolved ?: TimeZone.getTimeZone("UTC")
    }

    private fun parseLocalDateTimeToEpoch(time: String?, tz: TimeZone): Long? {
        if (time.isNullOrBlank()) return null
        // Open-Meteo sends local times like "2026-05-23T10:00" (no seconds) or with seconds.
        val patterns = arrayOf("yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss")
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.ROOT).apply { timeZone = tz }
                return fmt.parse(time)?.time?.div(1000)
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return null
    }

    private fun epochAtLocalTime(dateOnly: String?, hour: Int, minute: Int, tz: TimeZone): Long {
        if (dateOnly.isNullOrBlank()) return nowEpochSeconds()
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = tz }
            val date = fmt.parse(dateOnly) ?: return nowEpochSeconds()
            val cal = Calendar.getInstance(tz).apply {
                time = date
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis / 1000
        } catch (_: Exception) {
            nowEpochSeconds()
        }
    }

    private fun isDayHour(epochSec: Long, tz: TimeZone): Boolean {
        val cal = Calendar.getInstance(tz).apply { timeInMillis = epochSec * 1000 }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour in 6..18
    }

    private fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1000
}

private data class WeatherPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val countryCode: String? = null
)

private fun GeocodingResult.toWeatherPlace(): WeatherPlace {
    return WeatherPlace(
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        countryCode = countryCode
    )
}

internal fun normalizeCitySearchQuery(query: String): String {
    val compact = query.trim()
    return koreanCityAliases[compact] ?: compact
}

private val koreanCityAliases = mapOf(
    "서울" to "Seoul",
    "서울시" to "Seoul",
    "서울특별시" to "Seoul",
    "부산" to "Busan",
    "부산시" to "Busan",
    "부산광역시" to "Busan",
    "제주" to "Jeju",
    "제주시" to "Jeju",
    "제주도" to "Jeju",
    "인천" to "Incheon",
    "인천광역시" to "Incheon",
    "대구" to "Daegu",
    "대구광역시" to "Daegu",
    "대전" to "Daejeon",
    "대전광역시" to "Daejeon",
    "광주" to "Gwangju",
    "광주광역시" to "Gwangju",
    "울산" to "Ulsan",
    "울산광역시" to "Ulsan",
    "세종" to "Sejong",
    "세종시" to "Sejong",
    "수원" to "Suwon",
    "수원시" to "Suwon"
)

// --- WMO weather-code mapping (keeps existing UI conditionId semantics) ---

internal fun wmoToConditionId(code: Int): Int = when (code) {
    0 -> 800
    1, 2 -> 801
    3 -> 803
    45, 48 -> 701
    51, 53, 55 -> 300
    56, 57 -> 511
    61, 63, 65, 80, 81, 82 -> 500
    66, 67 -> 511
    71, 73, 75, 77, 85, 86 -> 600
    95 -> 200
    96, 99 -> 232
    else -> 800
}

internal fun wmoToMainAndDesc(code: Int): Pair<String, String> = when (code) {
    0 -> "Clear" to "맑음"
    1 -> "Clouds" to "대체로 맑음"
    2 -> "Clouds" to "부분적으로 흐림"
    3 -> "Clouds" to "흐림"
    45, 48 -> "Atmosphere" to "안개"
    51, 53, 55 -> "Rain" to "이슬비"
    56, 57 -> "Rain" to "어는 이슬비"
    61, 63, 65 -> "Rain" to "비"
    66, 67 -> "Rain" to "어는 비"
    71, 73, 75 -> "Snow" to "눈"
    77 -> "Snow" to "싸락눈"
    80, 81, 82 -> "Rain" to "소나기"
    85, 86 -> "Snow" to "소낙눈"
    95 -> "Thunderstorm" to "뇌우"
    96, 99 -> "Thunderstorm" to "우박을 동반한 뇌우"
    else -> "Clear" to "맑음"
}

internal fun wmoToIcon(code: Int, isDay: Boolean): String {
    val suffix = if (isDay) "d" else "n"
    return when (code) {
        0 -> "01$suffix"
        1, 2 -> "02$suffix"
        3 -> "03$suffix"
        45, 48 -> "50$suffix"
        51, 53, 55, 56, 57 -> "09$suffix"
        61, 63, 65, 66, 67 -> "10$suffix"
        80, 81, 82 -> "09$suffix"
        71, 73, 75, 77, 85, 86 -> "13$suffix"
        95, 96, 99 -> "11$suffix"
        else -> "01$suffix"
    }
}
