package com.example.ui.weather

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.CurrentWeatherResponse
import com.example.data.api.ForecastResponse
import com.example.data.api.FineDustInfo
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val currentWeather: CurrentWeatherResponse,
        val forecast: ForecastResponse,
        val fineDust: FineDustInfo,
        val isApiKeyConfigured: Boolean
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

enum class DarkThemeSetting {
    SYSTEM, LIGHT, DARK
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isFahrenheit = MutableStateFlow(false)
    val isFahrenheit: StateFlow<Boolean> = _isFahrenheit.asStateFlow()

    private val _currentCity = MutableStateFlow("서울")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _tempNotificationEnabled = MutableStateFlow(true)
    val tempNotificationEnabled: StateFlow<Boolean> = _tempNotificationEnabled.asStateFlow()

    private val _autoAlertsEnabled = MutableStateFlow(true)
    val autoAlertsEnabled: StateFlow<Boolean> = _autoAlertsEnabled.asStateFlow()

    private val _darkThemeSetting = MutableStateFlow(DarkThemeSetting.SYSTEM)
    val darkThemeSetting: StateFlow<DarkThemeSetting> = _darkThemeSetting.asStateFlow()

    val suggestions = listOf("서울", "부산", "제주", "Tokyo", "New York")

    init {
        fetchWeather(_currentCity.value)
    }

    fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
        
        val lastCity = prefs.getString("pref_key_last_city", "서울") ?: "서울"
        val themeName = prefs.getString("pref_key_dark_theme", DarkThemeSetting.SYSTEM.name) ?: DarkThemeSetting.SYSTEM.name
        
        _darkThemeSetting.value = try {
            DarkThemeSetting.valueOf(themeName)
        } catch (e: Exception) {
            DarkThemeSetting.SYSTEM
        }
        
        _isFahrenheit.value = prefs.getBoolean("pref_key_fahrenheit", false)
        _tempNotificationEnabled.value = prefs.getBoolean("pref_key_temp_notification_enabled", true)
        _autoAlertsEnabled.value = prefs.getBoolean("pref_key_auto_alerts_enabled", true)
        
        if (lastCity != _currentCity.value) {
            _currentCity.value = lastCity
            fetchWeather(lastCity)
        }
    }

    fun selectDarkThemeSetting(setting: DarkThemeSetting, context: Context) {
        _darkThemeSetting.value = setting
        context.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("pref_key_dark_theme", setting.name)
            .apply()
    }

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTempUnit(context: Context? = null) {
        _isFahrenheit.value = !_isFahrenheit.value
        context?.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("pref_key_fahrenheit", _isFahrenheit.value)
            ?.apply()
    }

    fun searchCity(context: Context? = null) {
        val city = _searchQuery.value.trim()
        if (city.isNotEmpty()) {
            _currentCity.value = city
            context?.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
                ?.edit()
                ?.putString("pref_key_last_city", city)
                ?.apply()
            fetchWeather(city)
        }
    }

    fun selectSuggestedCity(city: String, context: Context? = null) {
        _currentCity.value = city
        _searchQuery.value = ""
        context?.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("pref_key_last_city", city)
            ?.apply()
        fetchWeather(city)
    }

    fun refresh() {
        fetchWeather(_currentCity.value)
    }

    fun setTempNotificationEnabled(enabled: Boolean, context: Context, state: WeatherUiState.Success?) {
        _tempNotificationEnabled.value = enabled
        context.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("pref_key_temp_notification_enabled", enabled)
            .apply()
            
        if (enabled && state != null) {
            updateCurrentTempNotification(context, state)
        } else {
            WeatherNotificationHelper.clearCurrentTempNotification(context)
        }
    }

    fun updateCurrentTempNotification(context: Context, state: WeatherUiState.Success) {
        if (!_tempNotificationEnabled.value) return
        val temp = state.currentWeather.main.temp
        val formattedText = if (_isFahrenheit.value) {
            "${(temp * 9 / 5 + 32).roundToInt()}°F"
        } else {
            "${temp.roundToInt()}°C"
        }
        val conditionText = state.currentWeather.weather.firstOrNull()?.description ?: "맑음"
        
        WeatherNotificationHelper.showCurrentTempNotification(
            context = context,
            cityName = state.currentWeather.name,
            tempText = formattedText,
            conditionText = conditionText
        )
    }

    fun triggerMockWeatherPush(context: Context, condition: String) {
        val title = "⚡ skyline 기상 특보 안내"
        val desc = when (condition) {
            "Rain" -> "오후부터 돌풍을 동반한 강한 비가 내리겠습니다. 침수 피해 및 빗길 운전에 필히 유의하십시오."
            "Snow" -> "한파 및 눈발이 거세 기포 주의보가 추가 발효되었습니다. 보행 안전 및 폭설 빙판길 감속 운전 하시기 바랍니다."
            "DustAlert" -> "미세먼지 농도 [매우 나쁨/주의] 경보! 호흡기 환자 및 임산부는 무리한 외출을 금하고 실외 마스크를 상시 착용해 주세요."
            else -> "기압 변화로 날씨가 급변합니다. 가벼운 안개 및 돌풍에 대비하고 저녁 외출 시 보온에 유의해 주세요."
        }
        WeatherNotificationHelper.showWeatherAlertNotification(context, title, desc)
    }

    fun triggerMockFineDustPush(context: Context, fineDust: FineDustInfo) {
        val title = "💨 미세먼지 수치 경보알림"
        val desc = "현재 대기 상태가 [${fineDust.grade}] 등급입니다. PM10: ${fineDust.pm10}㎍/㎥, PM2.5: ${fineDust.pm25}㎍/㎥. ${fineDust.gradeDesc}"
        WeatherNotificationHelper.showFineDustAlertNotification(context, title, desc)
    }

    fun setAutoAlertsEnabled(enabled: Boolean, context: Context? = null) {
        _autoAlertsEnabled.value = enabled
        context?.getSharedPreferences("skyline_weather_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("pref_key_auto_alerts_enabled", enabled)
            ?.apply()
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                // Fetch both weather, forecast, and fine dust in parallel using coroutines
                val currentWeather = repository.getCurrentWeather(city)
                val forecast = repository.getForecast(city)
                val fineDust = repository.getFineDust(city)
                val isConfigured = repository.isApiKeyConfigured()
                
                _uiState.value = WeatherUiState.Success(
                    currentWeather = currentWeather,
                    forecast = forecast,
                    fineDust = fineDust,
                    isApiKeyConfigured = isConfigured
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "날씨 정보 수집 중 에러가 발생했습니다."
                )
            }
        }
    }
}
