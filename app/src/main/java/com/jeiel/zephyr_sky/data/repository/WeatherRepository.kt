package com.jeiel.zephyr_sky.data.repository

import com.jeiel.zephyr_sky.BuildConfig
import com.jeiel.zephyr_sky.data.api.*
import com.jeiel.zephyr_sky.data.api.MainTemp
import com.jeiel.zephyr_sky.data.api.Wind
import com.jeiel.zephyr_sky.data.api.Clouds
import com.jeiel.zephyr_sky.data.api.SysInfo
import com.jeiel.zephyr_sky.data.api.CurrentWeatherResponse
import com.jeiel.zephyr_sky.data.api.WeatherDescription
import com.jeiel.zephyr_sky.data.api.ForecastItem
import com.jeiel.zephyr_sky.data.api.CityInfo
import com.jeiel.zephyr_sky.data.api.ForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WeatherRepository {

    // Returns true to satisfy ViewModel states without requiring API keys
    fun isApiKeyConfigured(): Boolean {
        return true
    }

    suspend fun getCurrentWeather(cityName: String): CurrentWeatherResponse = withContext(Dispatchers.IO) {
        return@withContext getMockCurrentWeather(cityName)
    }

    suspend fun getFineDust(cityName: String): FineDustInfo = withContext(Dispatchers.IO) {
        val lowerCity = cityName.lowercase(Locale.ROOT).trim()
        val pm10: Int
        val pm25: Int
        val grade: String
        val gradeDesc: String
        val statusColorHex: String

        when {
            lowerCity.contains("seoul") || lowerCity.contains("서울") -> {
                pm10 = 42
                pm25 = 21
                grade = "보통"
                gradeDesc = "공기질 무난함. 가벼운 외출 및 청소가 가능합니다."
                statusColorHex = "0xFF435E91"
            }
            lowerCity.contains("busan") || lowerCity.contains("부산") -> {
                pm10 = 28
                pm25 = 12
                grade = "좋음"
                gradeDesc = "대기 상쾌함! 야외 산책 및 외출 활동하기 완벽한 날씨입니다."
                statusColorHex = "0xFF34A853"
            }
            lowerCity.contains("jeju") || lowerCity.contains("제주") -> {
                pm10 = 12
                pm25 = 5
                grade = "좋음"
                gradeDesc = "청정 제주 한라산 무공해 공기로 숨쉬기 매우 평화롭습니다."
                statusColorHex = "0xFF34A853"
            }
            lowerCity.contains("tokyo") || lowerCity.contains("도쿄") -> {
                pm10 = 35
                pm25 = 18
                grade = "보통"
                gradeDesc = "가벼운 야외 활동을 하기에 무리 없는 일상적인 환경입니다."
                statusColorHex = "0xFF435E91"
            }
            lowerCity.contains("new york") || lowerCity.contains("뉴욕") || lowerCity.contains("york") -> {
                pm10 = 85
                pm25 = 55
                grade = "나쁨"
                gradeDesc = "도시 배기가스와 정체 공기로 공기가 탁합니다. 마스크를 추천합니다."
                statusColorHex = "0xFFFE8C00"
            }
            else -> {
                val seed = cityName.hashCode().let { if (it < 0) -it else it }
                pm10 = 20 + (seed % 90)
                pm25 = pm10 / 2 + (seed % 10)
                when {
                    pm10 <= 30 -> {
                        grade = "좋음"
                        gradeDesc = "대기질 상쾌함. 맑고 깨끗해 마음 놓고 활동할 수 있습니다."
                        statusColorHex = "0xFF34A853"
                    }
                    pm10 <= 80 -> {
                        grade = "보통"
                        gradeDesc = "보통의 마일드한 상태입니다. 실내 환기도 원활히 가능한 수준입니다."
                        statusColorHex = "0xFF435E91"
                    }
                    pm10 <= 150 -> {
                        grade = "나쁨"
                        gradeDesc = "민감군은 호흡기 질환 예방을 위해 오랜 실외 일정을 축소하세요."
                        statusColorHex = "0xFFFE8C00"
                    }
                    else -> {
                        grade = "매우나쁨"
                        gradeDesc = "황사/초미세경보 최고조! 무리한 유산소 구보나 실외 노출을 중지하세요."
                        statusColorHex = "0xFFD32F2F"
                    }
                }
            }
        }

        return@withContext FineDustInfo(
            pm10 = pm10,
            pm25 = pm25,
            grade = grade,
            gradeDesc = gradeDesc,
            statusColorHex = statusColorHex
        )
    }

    suspend fun getForecast(cityName: String): ForecastResponse = withContext(Dispatchers.IO) {
        return@withContext getMockForecast(cityName)
    }

    // High quality mock weather generators based on selected city
    private fun getMockCurrentWeather(cityName: String): CurrentWeatherResponse {
        val lowerCity = cityName.lowercase(Locale.ROOT).trim()
        val temp: Double
        val conditionHeader: String
        val conditionDesc: String
        val conditionId: Int // Storm, Rain, Snow, Clouds, Clear
        val humidity: Double
        val windSpeed: Double

        when {
            lowerCity.contains("seoul") || lowerCity.contains("서울") -> {
                temp = 22.4
                conditionHeader = "Rain"
                conditionDesc = "가벼운 비가 내리고 있습니다"
                conditionId = 500 // Rainy
                humidity = 82.0
                windSpeed = 3.6
            }
            lowerCity.contains("busan") || lowerCity.contains("부산") -> {
                temp = 24.8
                conditionHeader = "Clouds"
                conditionDesc = "흐리고 서늘한 바람이 붑니다"
                conditionId = 803 // Cloudy
                humidity = 70.0
                windSpeed = 5.2
            }
            lowerCity.contains("jeju") || lowerCity.contains("제주") -> {
                temp = 26.1
                conditionHeader = "Clear"
                conditionDesc = "맑고 아주 쾌청한 날씨입니다"
                conditionId = 800 // Sunny
                humidity = 58.0
                windSpeed = 2.1
            }
            lowerCity.contains("tokyo") || lowerCity.contains("도쿄") -> {
                temp = 18.5
                conditionHeader = "Clouds"
                conditionDesc = "구름이 많아 포근한 화창함입니다"
                conditionId = 801 // Partly Cloudy
                humidity = 65.0
                windSpeed = 1.9
            }
            lowerCity.contains("new york") || lowerCity.contains("뉴욕") || lowerCity.contains("york") -> {
                temp = 8.3
                conditionHeader = "Snow"
                conditionDesc = "낭만적인 흰 눈이 내리고 있습니다"
                conditionId = 600 // Snowing
                humidity = 90.0
                windSpeed = 4.8
            }
            else -> {
                temp = 20.0
                conditionHeader = "Clear"
                conditionDesc = "맑고 선선한 하루입니다"
                conditionId = 800
                humidity = 60.0
                windSpeed = 2.5
            }
        }

        return CurrentWeatherResponse(
            name = when {
                lowerCity.contains("seoul") || lowerCity.contains("서울") -> "서울"
                lowerCity.contains("busan") || lowerCity.contains("부산") -> "부산"
                lowerCity.contains("jeju") || lowerCity.contains("제주") -> "제주"
                lowerCity.contains("tokyo") || lowerCity.contains("도쿄") -> "Tokyo"
                lowerCity.contains("new york") || lowerCity.contains("뉴욕") -> "New York"
                else -> cityName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            },
            weather = listOf(
                WeatherDescription(
                    id = conditionId,
                    main = conditionHeader,
                    description = conditionDesc,
                    icon = when (conditionId) {
                        in 200..232 -> "11d" // storm
                        in 300..531 -> "09d" // rain
                        in 600..622 -> "13d" // snow
                        800 -> "01d" // clear
                        else -> "03d" // clouds
                    }
                )
            ),
            main = MainTemp(
                temp = temp,
                feels_like = temp - 1.2,
                temp_min = temp - 4.0,
                temp_max = temp + 3.0,
                humidity = humidity,
                pressure = 1013.0
            ),
            wind = Wind(speed = windSpeed, deg = 180.0),
            clouds = Clouds(all = if (conditionHeader == "Clear") 10 else 75),
            sys = SysInfo(
                country = if (lowerCity.contains("seoul") || lowerCity.contains("서울") || lowerCity.contains("busan") || lowerCity.contains("부산") || lowerCity.contains("jeju") || lowerCity.contains("제주")) "KR" else "US",
                sunrise = Date().time / 1000 - 18000,
                sunset = Date().time / 1000 + 18000
            ),
            dt = Date().time / 1000
        )
    }

    private fun getMockForecast(cityName: String): ForecastResponse {
        val lowerCity = cityName.lowercase(Locale.ROOT).trim()
        val currentWeather = getMockCurrentWeather(cityName)
        val baseTemp = currentWeather.main.temp
        val mainConditionId = currentWeather.weather.firstOrNull()?.id ?: 800

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val list = mutableListOf<ForecastItem>()

        val calendar = Calendar.getInstance()
        for (i in 0 until 8) { // 8 slots = 24 hours of forecast, 3-hour interval
            calendar.add(Calendar.HOUR_OF_DAY, 3)
            val forecastTime = calendar.time
            val timeString = dateFormat.format(forecastTime)

            // Make beautiful temperature variations (sine waves or daily rhythm)
            val factor = Math.sin(i * 0.8)
            val tempOffset = factor * 3.5
            val currentTemp = baseTemp + tempOffset

            val itemConditionId = when {
                i % 4 == 0 -> mainConditionId // main
                i % 3 == 0 -> 800 // clear sunny
                else -> 801 // clouds
            }

            val conditionHeader = when (itemConditionId) {
                800 -> "Clear"
                in 600..622 -> "Snow"
                in 300..531 -> "Rain"
                else -> "Clouds"
            }

            val conditionDesc = when (itemConditionId) {
                800 -> "맑음"
                in 600..622 -> "눈송이"
                in 300..531 -> "비 내림"
                else -> "구름 낌"
            }

            list.add(
                ForecastItem(
                    dt = forecastTime.time / 1000,
                    main = MainTemp(
                        temp = Math.round(currentTemp * 10).toDouble() / 10,
                        feels_like = currentTemp - 0.8,
                        temp_min = currentTemp - 2.0,
                        temp_max = currentTemp + 2.0,
                        humidity = currentWeather.main.humidity + (factor * 5).toInt(),
                        pressure = 1013.0
                    ),
                    weather = listOf(
                        WeatherDescription(
                            id = itemConditionId,
                            main = conditionHeader,
                            description = conditionDesc,
                            icon = when (itemConditionId) {
                                800 -> "01d"
                                in 600..622 -> "13d"
                                in 300..531 -> "09d"
                                else -> "03d"
                            }
                        )
                    ),
                    dt_txt = timeString,
                    wind = Wind(speed = currentWeather.wind.speed + (factor * 1.2), deg = 180.0)
                )
            )
        }

        return ForecastResponse(
            list = list,
            city = CityInfo(
                name = currentWeather.name,
                country = currentWeather.sys.country
            )
        )
    }
}
