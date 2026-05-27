# Zephyr Sky - 미니멀 날씨 앱 개발 로드맵

## 프로젝트 개요
**목표:** 도시명 검색과 선택형 현재 위치 기반의 실시간 날씨, 시간별 예보, 대기질 정보를 광고와 추적 없이 정제된 그라데이션 UI로 보여주는 미니멀 Android 날씨 앱 개발.

---

## 현재 앱 상태 (v2.0.0)

### ✅ 구현 완료 기능
| 기능 | 구현 컴포넌트 | 상태 |
|------|------|------|
| **실시간 위치 날씨 연동** | `WeatherApi`, `WeatherRepository` | 완료 |
| **시간별 예보 UI** | `WeatherScreen`, `WeatherViewModel` | 완료 |
| **대기질 리포트** | `WeatherRepository`, `WeatherScreen` | 완료 |
| **앱 설정 로컬 저장** | `SharedPreferences` | 완료 |
| **자동 스크린샷 비주얼 테스트** | `Roborazzi` & `Robolectric` 테스트 스위트 | 완료 |
| **환경 변수 구성 API 키 관리** | `Secrets Gradle Plugin` & `.env` | 완료 |

---

## 🗺️ 개발 로드맵 (5단계)

### Phase 1: 실시간 날씨 연동 및 파싱 (완료)
* **목표**: Open-Meteo Forecast / Geocoding / Air Quality API를 통해 현재 날씨, 시간별 예보, 대기질 정보를 조회하고 앱 화면에 표시합니다.
* **구현 세부**:
  - `WeatherApi.kt`, `WeatherModels.kt`로 REST 기상 정보 처리.
  - 한글 주요 도시명 검색 보정 및 좌표 기반 조회 지원.

### Phase 2: 로컬 설정 저장 및 재실행 복원 (완료)
* **목표**: 마지막 도시 또는 현재 위치 좌표, 온도 단위, 테마, 알림 설정을 기기 내부에 저장하고 재실행 시 복원합니다.
* **구현 세부**:
  - SharedPreferences 기반 설정 저장.
  - 기존 `skyline_weather_prefs` 설정값을 `zephyr_sky_prefs`로 보존 이전.

### Phase 3: 선택형 위치 기반 조회 (완료)
* **목표**: 사용자가 명시적으로 선택할 때만 대략적인 위치 권한을 요청하고, 현재 위치 기반 날씨와 대기질을 조회합니다.
* **구현 세부**:
  - Android `LocationManager`와 `Geocoder` 기반 현재 위치 해석.
  - 위치 좌표를 날씨 조회와 마지막 선택 복원 용도로만 사용.

### Phase 4: 스크린샷 기반 비주얼 회귀 테스트 구축 (완료)
* **목표**: UI 컴포넌트의 깨짐 현상이나 디자인 회귀를 방지하기 위해 로컬 단위 테스트 수준에서 UI 화면을 렌더링하고 이미지를 대조 검증하는 환경을 구축합니다.
* **구현 세부**:
  - Robolectric 기반 가상 디바이스 테스트 환경 설정.
  - Roborazzi 플러그인을 활성화하여 `app/src/test/screenshots/` 내 스크린샷 생성 및 대조 테스트 실행 가능 확보.

### Phase 5: 다국어 고도화 및 위젯 지원 (예정)
* **목표**: 앱 문자열의 다국어 구조를 정교화하고, 안드로이드 홈 스크린에 현재 날씨 요약을 보여주는 위젯을 개발합니다.
* **구현 세부**:
  - 다국어 번들 리소스 리팩토링.
  - Android Glance 또는 표준 AppWidget 라이브러리를 활용한 홈 위젯 연동.

---

## 📋 완료 조건 체크리스트

### Phase 1~4 완료 조건
- [x] Open-Meteo 실시간 날씨 연동 성공
- [x] 시간별 예보와 대기질 리포트 표시 성공
- [x] 설정 저장 및 기존 설정값 이전 성공
- [x] Robolectric 및 Roborazzi 테스트 전체 성공 (`./gradlew test`)
- [x] 디버그 APK 빌드 성공 (`./gradlew assembleDebug`)

---

*로드맵 최종 업데이트: 2026-05-27*
