package com.jeiel.zephyr_sky.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jeiel.zephyr_sky.data.api.CurrentWeatherResponse
import com.jeiel.zephyr_sky.data.api.ForecastItem
import com.jeiel.zephyr_sky.data.api.ForecastResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Theme Colors for Modern Minimalist Aesthetic (Editorial Aesthetic Edition)
object WeatherThemes {
    val ClearSkyGrad = listOf(Color(0xFFFDFBFF), Color(0xFFFFF2E2)) // Soft warm golden off-white
    val RainyGrad = listOf(Color(0xFFFDFBFF), Color(0xFFE5EEF8))    // Cool ambient mist blue
    val CloudyGrad = listOf(Color(0xFFFDFBFF), Color(0xFFECEFF4))   // Creamy elegant cloud grey
    val SnowyGrad = listOf(Color(0xFFFDFBFF), Color(0xFFE9F5F7))    // Frosty crisp white-pearl
}

object DarkWeatherThemes {
    val ClearSkyGrad = listOf(Color(0xFF0F111E), Color(0xFF1E2235)) // Dark starry slate midnight
    val RainyGrad = listOf(Color(0xFF101323), Color(0xFF1A2132))    // Stormy dark navy
    val CloudyGrad = listOf(Color(0xFF12141F), Color(0xFF212534))   // Dark overcast charcoal
    val SnowyGrad = listOf(Color(0xFF0F141B), Color(0xFF1B2836))    // Frosty deep navy
}

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isFahrenheit by viewModel.isFahrenheit.collectAsState()

    val darkThemeSetting by viewModel.darkThemeSetting.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (darkThemeSetting) {
        DarkThemeSetting.SYSTEM -> isSystemDark
        DarkThemeSetting.LIGHT -> false
        DarkThemeSetting.DARK -> true
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(DeviceLocationResolver.hasLocationPermission(context))
    }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.selectCurrentLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        WeatherNotificationHelper.createNotificationChannels(context)
    }

    val successState = uiState as? WeatherUiState.Success
    LaunchedEffect(successState, isFahrenheit) {
        if (successState != null && hasNotificationPermission) {
            viewModel.updateCurrentTempNotification(context, successState)
        }
    }

    // Determine color coordinates and animation based on active state
    val weatherId = when (val state = uiState) {
        is WeatherUiState.Success -> state.currentWeather.weather.firstOrNull()?.id ?: 800
        else -> 800
    }

    val backgroundGradient = remember(weatherId, isDarkTheme) {
        if (isDarkTheme) {
            when (weatherId) {
                800 -> DarkWeatherThemes.ClearSkyGrad
                in 200..232, in 300..531 -> DarkWeatherThemes.RainyGrad
                in 600..622 -> DarkWeatherThemes.SnowyGrad
                else -> DarkWeatherThemes.CloudyGrad
            }
        } else {
            when (weatherId) {
                800 -> WeatherThemes.ClearSkyGrad
                in 200..232, in 300..531 -> WeatherThemes.RainyGrad
                in 600..622 -> WeatherThemes.SnowyGrad
                else -> WeatherThemes.CloudyGrad
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = backgroundGradient)
            )
    ) {
        // Dynamic Canvas-Based Animations running infinitely in the background (Editorial styled)
        WeatherAnimator(weatherId = weatherId)

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Editorial Brand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Skyline Weather",
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                            fontSize = 22.sp,
                            modifier = Modifier.testTag("app_brand_logo")
                        )
                        Text(
                            text = "EDITORIAL WEATHER JOURNAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Compact Celsius/Fahrenheit Unit Switcher
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF))
                            .clickable { viewModel.toggleTempUnit(context) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isFahrenheit) "°F" else "°C",
                                color = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Toggle Units",
                                tint = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar with software keyboard action bindings
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setQuery(it) },
                    placeholder = { Text("도시 이름 검색...", color = (if (isDarkTheme) Color(0xFFE1E2EC) else Color(0xFF1A1C1E)).copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1A1C1E),
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1A1C1E),
                        focusedBorderColor = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                        unfocusedBorderColor = (if (isDarkTheme) Color(0xFFE1E2EC) else Color(0xFF1A1C1E)).copy(alpha = 0.15f),
                        cursorColor = if (isDarkTheme) Color.White else Color(0xFF435E91),
                        focusedContainerColor = if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7),
                        unfocusedContainerColor = if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    shape = RoundedCornerShape(32.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.searchCity(context)
                                keyboardController?.hide()
                            },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.searchCity(context)
                        keyboardController?.hide()
                    })
                )

                Spacer(modifier = Modifier.height(14.dp))

                FilledTonalButton(
                    onClick = {
                        if (DeviceLocationResolver.hasLocationPermission(context)) {
                            hasLocationPermission = true
                            viewModel.selectCurrentLocation(context)
                        } else {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("current_location_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7),
                        contentColor = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38)
                    ),
                    border = BorderStroke(
                        1.dp,
                        (if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91)).copy(alpha = 0.28f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use current location",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasLocationPermission) "현재 위치 날씨 보기" else "위치 기반으로 선택",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Render Content base on State Machine
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    label = "WeatherStateAnim"
                ) { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = if (isDarkTheme) Color.White else Color(0xFF435E91), strokeWidth = 3.dp)
                            }
                        }
                        is WeatherUiState.Success -> {
                            WeatherContentBlock(
                                current = state.currentWeather,
                                forecast = state.forecast,
                                fineDust = state.fineDust,
                                isFahrenheit = isFahrenheit,
                                isApiKeyConfigured = state.isApiKeyConfigured,
                                weatherId = weatherId,
                                onRefresh = { viewModel.refresh() },
                                viewModel = viewModel,
                                stateSuccess = state,
                                isDarkTheme = isDarkTheme
                            )
                        }
                        is WeatherUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
                                    .border(1.dp, if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC), RoundedCornerShape(32.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = "Error icon",
                                    tint = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("재시도", color = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Renders the main dashboard layout
@Composable
fun WeatherContentBlock(
    current: CurrentWeatherResponse,
    forecast: ForecastResponse,
    fineDust: com.jeiel.zephyr_sky.data.api.FineDustInfo,
    isFahrenheit: Boolean,
    isApiKeyConfigured: Boolean,
    weatherId: Int,
    onRefresh: () -> Unit,
    viewModel: WeatherViewModel,
    stateSuccess: WeatherUiState.Success,
    isDarkTheme: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Temperature Conversion Logic Helper
        fun formatTemp(celsius: Double): String {
            val temp = if (isFahrenheit) (celsius * 1.8 + 32) else celsius
            return "${temp.roundToInt()}°"
        }

        // Current Location & Timestamp Panel
        val timeFormat = SimpleDateFormat("EE, MMM dd | HH:mm", Locale.getDefault())
        val formattedTime = remember(current.dt) {
            timeFormat.format(Date(current.dt * 1000))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = current.name,
                style = MaterialTheme.typography.displaySmall,
                color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91))
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { onRefresh() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = "${current.sys.country} • $formattedTime",
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Centerpiece Big Temperature Block with Floating Animation (Editorial Layout)
        val infiniteTransition = rememberInfiniteTransition(label = "TempFloat")
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FloatAnim"
        )

        // Elegant Vector Icon centered
        val iconImage = remember(weatherId) {
            when (weatherId) {
                800 -> Icons.Outlined.WbSunny
                in 200..232 -> Icons.Outlined.Thunderstorm
                in 300..531 -> Icons.Outlined.WaterDrop
                in 600..622 -> Icons.Outlined.AcUnit
                else -> Icons.Outlined.Cloud
            }
        }

        Row(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = formatTemp(current.main.temp).replace("°", ""),
                        color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                        fontWeight = FontWeight.Light,
                        fontSize = 110.sp,
                        lineHeight = 110.sp,
                        letterSpacing = (-6).sp
                    )
                    Text(
                        text = "°",
                        color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                        fontWeight = FontWeight.ExtraLight,
                        fontSize = 80.sp,
                        lineHeight = 80.sp
                    )
                }

                Text(
                    text = current.weather.firstOrNull()?.description ?: "",
                    color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "최고: ${formatTemp(current.main.temp_max)}",
                            color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "최저: ${formatTemp(current.main.temp_min)}",
                            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Elegant, floating weather symbol frame on the right
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background((if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)).copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconImage,
                    contentDescription = "Weather Icon",
                    tint = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38),
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // High-Density Metrics: Material 3 Cards (Editorial Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditorialGridItem(
                icon = Icons.Default.WaterDrop,
                label = "HUMIDITY",
                value = "${current.main.humidity.roundToInt()}%",
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            EditorialGridItem(
                icon = Icons.Default.Air,
                label = "WIND SPEED",
                value = "${current.wind.speed} m/s",
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditorialGridItem(
                icon = Icons.Default.Thermostat,
                label = "FEELS LIKE",
                value = formatTemp(current.main.feels_like),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
            EditorialGridItem(
                icon = Icons.Default.Compress,
                label = "PRESSURE",
                value = "${current.main.pressure.roundToInt()} hPa",
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fine Dust (PM10 / PM2.5) Display Card
        FineDustSection(fineDust = fineDust, isDarkTheme = isDarkTheme)

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly/3-Hour Forecast Line
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "시간대별 날씨 예보",
                color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(forecast.list) { item ->
                    HourlyForecastItem(item = item, isFahrenheit = isFahrenheit, isDarkTheme = isDarkTheme)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trigger Control & Settings Section for local system dispatches
        NotificationControlSection(viewModel = viewModel, successState = stateSuccess, isDarkTheme = isDarkTheme)
    }
}

// Compact Subcomponent for Parameter Grid Item - Editorial Style
@Composable
fun EditorialGridItem(
    icon: ImageVector,
    label: String,
    value: String,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Glassmorphic vertical capsule for hourly forecast block
@Composable
fun HourlyForecastItem(
    item: ForecastItem,
    isFahrenheit: Boolean,
    isDarkTheme: Boolean = false
) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeLabel = remember(item.dt_txt) {
        try {
            val date = inputFormat.parse(item.dt_txt)
            if (date != null) outputFormat.format(date) else ""
          } catch (e: Exception) {
            ""
        }
    }

    val tempVal = if (isFahrenheit) (item.main.temp * 1.8 + 32) else item.main.temp
    val climate = item.weather.firstOrNull()?.id ?: 800

    val climateIcon = remember(climate) {
        when (climate) {
            800 -> Icons.Default.WbSunny
            in 200..232 -> Icons.Default.Thunderstorm
            in 300..531 -> Icons.Default.WaterDrop
            in 600..622 -> Icons.Default.AcUnit
            else -> Icons.Default.Cloud
        }
    }

    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
            .border(1.dp, if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC), RoundedCornerShape(24.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = timeLabel,
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = climateIcon,
            contentDescription = "Forecast visual",
            tint = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "${tempVal.roundToInt()}°",
            color = if (isDarkTheme) Color(0xFFF1F0F7) else Color(0xFF1A1C1E),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// CANVAS WEATHER BACKGROUNDS: Elegant light-mode matching floating particle simulations
@Composable
fun WeatherAnimator(weatherId: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherEngine")

    when (weatherId) {
        800 -> { // Sun pulsations and rays
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "SunPulse"
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(28000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "RaySpin"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseRadius = 80.dp.toPx()
                val center = Offset(size.width * 0.85f, size.height * 0.15f)

                // Sun Halo drawing (Elegant transparent gold layers)
                drawCircle(
                    color = Color(0xFFFFE0B2).copy(alpha = 0.15f),
                    radius = baseRadius * 1.8f * scale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.22f),
                    radius = baseRadius * 1.3f * scale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                    radius = baseRadius * scale,
                    center = center
                )

                // Light Ray projections
                val lineLength = 22.dp.toPx()
                val lineOffset = baseRadius * scale + 8.dp.toPx()
                for (i in 0 until 8) {
                    val angle = rotation + (i * 45f)
                    val radians = Math.toRadians(angle.toDouble())
                    val startX = center.x + (lineOffset * cos(radians)).toFloat()
                    val startY = center.y + (lineOffset * sin(radians)).toFloat()
                    val endX = center.x + ((lineOffset + lineLength) * cos(radians)).toFloat()
                    val endY = center.y + ((lineOffset + lineLength) * sin(radians)).toFloat()

                    drawLine(
                        color = Color(0xFFFE8C00).copy(alpha = 0.22f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        in 200..232, in 300..531 -> { // Rainy animation
            val rainProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "RainDropProgress"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val dropCount = 35
                val random = java.util.Random(12345) // Fixed seed so lines don't jitter randomly across recompositions

                for (i in 0 until dropCount) {
                    val xPos = random.nextFloat() * size.width
                    val ySpeedFactor = 0.7f + random.nextFloat() * 0.6f
                    val dropLength = 15.dp.toPx() + random.nextFloat() * 10.dp.toPx()
                    
                    // Cascade calculation
                    val initialY = random.nextFloat() * size.height
                    val currentY = (initialY + (rainProgress * size.height * ySpeedFactor)) % size.height

                    drawLine(
                        color = Color(0xFF435E91).copy(alpha = 0.25f),
                        start = Offset(xPos - 5.dp.toPx() * (rainProgress * ySpeedFactor), currentY),
                        end = Offset(xPos - 5.dp.toPx() * (rainProgress * ySpeedFactor) - 2.dp.toPx(), currentY + dropLength),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        in 600..622 -> { // Snowy animation
            val snowProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "SnowDriftProgress"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val flakeCount = 30
                val random = java.util.Random(98765)

                for (i in 0 until flakeCount) {
                    val baseScaleX = random.nextFloat() * size.width
                    val driftFactor = sin((snowProgress * 2 * Math.PI) + (random.nextFloat() * 5)).toFloat() * 12.dp.toPx()
                    val finalX = baseScaleX + driftFactor
                    val currentSpeedFactor = 0.5f + random.nextFloat() * 0.5f
                    val flakeRadius = 3.dp.toPx() + random.nextFloat() * 3.dp.toPx()

                    val initialY = random.nextFloat() * size.height
                    val finalY = (initialY + (snowProgress * size.height * currentSpeedFactor)) % size.height

                    drawCircle(
                        color = Color(0xFF435E91).copy(alpha = 0.2f),
                        radius = flakeRadius,
                        center = Offset(finalX, finalY)
                    )
                }
            }
        }
        else -> { // Overlapping clouds swelling and drifting
            val cloudOffset by infiniteTransition.animateFloat(
                initialValue = -12f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(5000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "CloudDrift"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draws minimal shapes mirroring foggy clouds
                drawCircle(
                    color = Color(0xFFD3E4FF).copy(alpha = 0.35f),
                    radius = 210.dp.toPx(),
                    center = Offset(size.width * 0.25f + cloudOffset.dp.toPx(), size.height * 0.05f)
                )
                drawCircle(
                    color = Color(0xFFD3E4FF).copy(alpha = 0.25f),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width * 0.7f - cloudOffset.dp.toPx(), size.height * 0.08f)
                )
                drawCircle(
                    color = Color(0xFFD3E4FF).copy(alpha = 0.15f),
                    radius = 250.dp.toPx(),
                    center = Offset(size.width * 0.45f + (cloudOffset / 1.5f).dp.toPx(), size.height * 0.12f)
                )
            }
        }
    }
}

@Composable
fun FineDustSection(
    fineDust: com.jeiel.zephyr_sky.data.api.FineDustInfo,
    isDarkTheme: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
            .border(1.dp, if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "대기 오염 정보 뉴스레터",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AIR QUALITY REPORT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E)
                )
            }
            
            // Convert statusColorHex to native color beautifully
            val gradeColor = remember(fineDust.statusColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(fineDust.statusColorHex))
                } catch (e: Exception) {
                    Color(0xFF435E91)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(gradeColor.copy(alpha = 0.12f))
                    .border(1.dp, gradeColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = fineDust.grade,
                    color = gradeColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // PM10
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDarkTheme) Color(0xFF2B2D35) else Color.White)
                    .border(1.dp, (if (isDarkTheme) Color(0xFF3B3D45) else Color(0xFFE1E2EC)).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "미세먼지 (PM10)",
                        color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${fineDust.pm10}",
                            color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = " ㎍/㎥",
                            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // PM2.5
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDarkTheme) Color(0xFF2B2D35) else Color.White)
                    .border(1.dp, (if (isDarkTheme) Color(0xFF3B3D45) else Color(0xFFE1E2EC)).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "초미세먼지 (PM2.5)",
                        color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${fineDust.pm25}",
                            color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = " ㎍/㎥",
                            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = fineDust.gradeDesc,
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun NotificationControlSection(
    viewModel: WeatherViewModel,
    successState: WeatherUiState.Success,
    isDarkTheme: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tempNotifEnabled by viewModel.tempNotificationEnabled.collectAsState()
    val autoAlertsEnabled by viewModel.autoAlertsEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDarkTheme) Color(0xFF1E2025) else Color(0xFFF1F0F7))
            .border(1.dp, if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        // Theme Selection Panel
        Text(
            text = "화면 테마 모드 설정",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val selectedTheme by viewModel.darkThemeSetting.collectAsState()
            listOf(
                DarkThemeSetting.SYSTEM to "🌓 자동",
                DarkThemeSetting.LIGHT to "☀️ 라이트",
                DarkThemeSetting.DARK to "🌙 다크"
            ).forEach { (setting, label) ->
                val isSelected = selectedTheme == setting
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)
                            } else {
                                if (isDarkTheme) Color(0xFF2B2D35) else Color.White
                            }
                        )
                        .clickable { viewModel.selectDarkThemeSetting(setting, context) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) {
                            if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38)
                        } else {
                            if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background((if (isDarkTheme) Color(0xFF3B3D45) else Color(0xFFE1E2EC)).copy(alpha = 0.5f)))
        Spacer(modifier = Modifier.height(16.dp))

        // Label Header
        Text(
            text = "기보 알림 설정 및 발송 테스트",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "NOTIFICATIONS MANAGER",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Switch 1: Current temp status bar notification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "알림창 현재 기온 상시 고정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E)
                )
                Text(
                    text = "단말 상단 알림 바에 현재 위치 기온 정보를 실시간 상시 고정 표시합니다.",
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                    lineHeight = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = tempNotifEnabled,
                onCheckedChange = { viewModel.setTempNotificationEnabled(it, context, successState) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isDarkTheme) Color(0xFF12141F) else Color.White,
                    checkedTrackColor = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    uncheckedThumbColor = if (isDarkTheme) Color(0xFF5E6066) else Color(0xFF74777F),
                    uncheckedTrackColor = if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC)))
        Spacer(modifier = Modifier.height(8.dp))

        // Toggle Switch 2: Weather change warnings & alert notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "실시간 날씨 속보/경보 수신",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1A1C1E)
                )
                Text(
                    text = "폭우, 한파 폭설, 미세먼지 수치 주의 경보 상황에 대한 알림을 수집합니다.",
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF74777F),
                    lineHeight = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = autoAlertsEnabled,
                onCheckedChange = { viewModel.setAutoAlertsEnabled(it, context) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isDarkTheme) Color(0xFF12141F) else Color.White,
                    checkedTrackColor = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91),
                    uncheckedThumbColor = if (isDarkTheme) Color(0xFF5E6066) else Color(0xFF74777F),
                    uncheckedTrackColor = if (isDarkTheme) Color(0xFF2E3038) else Color(0xFFE1E2EC)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "실시간 날씨 푸시 알림 속보 즉시 발송 테스트",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color(0xFFBAC3D4) else Color(0xFF435E91)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Clickable Trigger Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.triggerMockWeatherPush(context, "Rain") },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("🌧️ 폭우 알림", color = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.triggerMockWeatherPush(context, "Snow") },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF3B485A) else Color(0xFFD3E4FF)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("❄️ 폭설 알림", color = if (isDarkTheme) Color(0xFFD3E4FF) else Color(0xFF001C38), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.triggerMockFineDustPush(context, successState.fineDust) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color(0xFF8C1D18).copy(alpha = 0.2f) else Color(0xFFF9DEDC)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("💨 미세먼지 알림", color = if (isDarkTheme) Color(0xFFF9DEDC) else Color(0xFF370B0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
