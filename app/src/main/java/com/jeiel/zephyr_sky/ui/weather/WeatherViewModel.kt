package com.jeiel.zephyr_sky.ui.weather

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeiel.zephyr_sky.data.api.CurrentWeatherResponse
import com.jeiel.zephyr_sky.data.api.ForecastResponse
import com.jeiel.zephyr_sky.data.api.FineDustInfo
import com.jeiel.zephyr_sky.data.repository.WeatherRepository
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

private const val PREFS_NAME = "zephyr_sky_prefs"
private const val LEGACY_PREFS_NAME = "skyline_weather_prefs"
private const val PREF_KEY_LAST_CITY = "pref_key_last_city"
private const val PREF_KEY_LAST_LATITUDE = "pref_key_last_latitude"
private const val PREF_KEY_LAST_LONGITUDE = "pref_key_last_longitude"
private const val PREF_KEY_DARK_THEME = "pref_key_dark_theme"
private const val PREF_KEY_FAHRENHEIT = "pref_key_fahrenheit"
private const val PREF_KEY_TEMP_NOTIFICATION_ENABLED = "pref_key_temp_notification_enabled"
private const val PREF_KEY_AUTO_ALERTS_ENABLED = "pref_key_auto_alerts_enabled"

private fun Context.zephyrSkyPreferences(): SharedPreferences {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val legacyPrefs = getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    if (!prefs.contains(PREF_KEY_LAST_CITY) && legacyPrefs.all.isNotEmpty()) {
        val editor = prefs.edit()
        legacyPrefs.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    editor.putStringSet(key, value as Set<String>)
                }
            }
        }
        editor.apply()
    }
    return prefs
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

    private var currentCoordinates: SelectedCoordinates? = null

    private val _tempNotificationEnabled = MutableStateFlow(true)
    val tempNotificationEnabled: StateFlow<Boolean> = _tempNotificationEnabled.asStateFlow()

    private val _autoAlertsEnabled = MutableStateFlow(true)
    val autoAlertsEnabled: StateFlow<Boolean> = _autoAlertsEnabled.asStateFlow()

    private val _darkThemeSetting = MutableStateFlow(DarkThemeSetting.SYSTEM)
    val darkThemeSetting: StateFlow<DarkThemeSetting> = _darkThemeSetting.asStateFlow()

    init {
        fetchWeather(_currentCity.value)
    }

    fun loadPreferences(context: Context) {
        val prefs = context.zephyrSkyPreferences()
        
        val lastCity = prefs.getString(PREF_KEY_LAST_CITY, "서울") ?: "서울"
        val lastLatitude = prefs.getString(PREF_KEY_LAST_LATITUDE, null)?.toDoubleOrNull()
        val lastLongitude = prefs.getString(PREF_KEY_LAST_LONGITUDE, null)?.toDoubleOrNull()
        val themeName = prefs.getString(PREF_KEY_DARK_THEME, DarkThemeSetting.SYSTEM.name) ?: DarkThemeSetting.SYSTEM.name
        
        _darkThemeSetting.value = try {
            DarkThemeSetting.valueOf(themeName)
        } catch (e: Exception) {
            DarkThemeSetting.SYSTEM
        }
        
        _isFahrenheit.value = prefs.getBoolean(PREF_KEY_FAHRENHEIT, false)
        _tempNotificationEnabled.value = prefs.getBoolean(PREF_KEY_TEMP_NOTIFICATION_ENABLED, true)
        _autoAlertsEnabled.value = prefs.getBoolean(PREF_KEY_AUTO_ALERTS_ENABLED, true)
        
        if (lastLatitude != null && lastLongitude != null) {
            currentCoordinates = SelectedCoordinates(lastLatitude, lastLongitude)
            _currentCity.value = lastCity
            fetchWeather(lastLatitude, lastLongitude, lastCity)
        } else if (lastCity != _currentCity.value) {
            currentCoordinates = null
            _currentCity.value = lastCity
            fetchWeather(lastCity)
        }
    }

    fun selectDarkThemeSetting(setting: DarkThemeSetting, context: Context) {
        _darkThemeSetting.value = setting
        context.zephyrSkyPreferences()
            .edit()
            .putString(PREF_KEY_DARK_THEME, setting.name)
            .apply()
    }

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTempUnit(context: Context? = null) {
        _isFahrenheit.value = !_isFahrenheit.value
        context?.zephyrSkyPreferences()
            ?.edit()
            ?.putBoolean(PREF_KEY_FAHRENHEIT, _isFahrenheit.value)
            ?.apply()
    }

    fun searchCity(context: Context? = null) {
        val city = _searchQuery.value.trim()
        if (city.isNotEmpty()) {
            currentCoordinates = null
            _currentCity.value = city
            saveCityPreference(context, city)
            fetchWeather(city)
        }
    }

    fun selectCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val selection = DeviceLocationResolver.resolve(context.applicationContext)
                currentCoordinates = SelectedCoordinates(selection.latitude, selection.longitude)
                _currentCity.value = selection.displayName
                _searchQuery.value = ""
                saveLocationPreference(
                    context = context,
                    displayName = selection.displayName,
                    latitude = selection.latitude,
                    longitude = selection.longitude
                )
                fetchWeather(selection.latitude, selection.longitude, selection.displayName)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "현재 위치를 확인할 수 없습니다."
                )
            }
        }
    }

    private fun saveCityPreference(context: Context?, city: String) {
        context?.zephyrSkyPreferences()
            ?.edit()
            ?.putString(PREF_KEY_LAST_CITY, city)
            ?.remove(PREF_KEY_LAST_LATITUDE)
            ?.remove(PREF_KEY_LAST_LONGITUDE)
            ?.apply()
    }

    private fun saveLocationPreference(
        context: Context?,
        displayName: String,
        latitude: Double,
        longitude: Double
    ) {
        context?.zephyrSkyPreferences()
            ?.edit()
            ?.putString(PREF_KEY_LAST_CITY, displayName)
            ?.putString(PREF_KEY_LAST_LATITUDE, latitude.toString())
            ?.putString(PREF_KEY_LAST_LONGITUDE, longitude.toString())
            ?.apply()
    }

    fun refresh() {
        val coordinates = currentCoordinates
        if (coordinates != null) {
            fetchWeather(coordinates.latitude, coordinates.longitude, _currentCity.value)
        } else {
            fetchWeather(_currentCity.value)
        }
    }

    fun setTempNotificationEnabled(enabled: Boolean, context: Context, state: WeatherUiState.Success?) {
        _tempNotificationEnabled.value = enabled
        context.zephyrSkyPreferences()
            .edit()
            .putBoolean(PREF_KEY_TEMP_NOTIFICATION_ENABLED, enabled)
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
        val title = "Zephyr Sky 기상 특보 안내"
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
        context?.zephyrSkyPreferences()
            ?.edit()
            ?.putBoolean(PREF_KEY_AUTO_ALERTS_ENABLED, enabled)
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

    private fun fetchWeather(latitude: Double, longitude: Double, displayName: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val currentWeather = repository.getCurrentWeather(latitude, longitude, displayName)
                val forecast = repository.getForecast(latitude, longitude, displayName)
                val fineDust = repository.getFineDust(latitude, longitude)
                val isConfigured = repository.isApiKeyConfigured()

                _uiState.value = WeatherUiState.Success(
                    currentWeather = currentWeather,
                    forecast = forecast,
                    fineDust = fineDust,
                    isApiKeyConfigured = isConfigured
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    e.localizedMessage ?: "현재 위치의 날씨 정보 수집 중 에러가 발생했습니다."
                )
            }
        }
    }
}

private data class SelectedCoordinates(
    val latitude: Double,
    val longitude: Double
)
