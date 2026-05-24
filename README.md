# Zephyr Sky

<p align="center">
  <img src="website/assets/feature_1024x500.png" alt="Zephyr Sky feature graphic" width="900">
</p>

**Zephyr Sky**는 도시명 검색만으로 실시간 날씨, 시간별 예보, 대기질 정보를 정제된 그라데이션 UI에 담아 보여주는 Android 날씨 앱입니다. Kotlin, Jetpack Compose, Material 3 기반으로 작성되었고, Open-Meteo의 무료 API를 사용합니다.

공개 페이지: <https://jeiel85.github.io/zephyr-sky-android/>
개인정보 처리방침: <https://jeiel85.github.io/zephyr-sky-android/privacy-policy.html>
최신 릴리즈: <https://github.com/jeiel85/zephyr-sky-android/releases/latest>

## 앱 정체성

Zephyr Sky는 과도한 정보와 광고를 걷어낸 **미니멀 날씨 도구**를 지향합니다. 다크 톤의 별빛 배경, 부드러운 날씨 애니메이션, 큰 현재 기온, 짧은 시간별 예보, 대기질 리포트를 중심에 두어 오늘의 외출 판단을 빠르게 돕습니다.

## 주요 기능

- 도시명 검색 기반 현재 날씨 조회
- Open-Meteo Forecast / Geocoding / Air Quality API 연동
- 현재 기온, 체감 온도, 습도, 바람, 구름량, 일출/일몰 표시
- 3시간 간격 시간별 예보 카드
- PM10, PM2.5, AQI 기반 대기질 리포트
- 섭씨/화씨 전환
- 라이트, 다크, 시스템 자동 테마
- 선택형 현재 기온 알림과 날씨 경보 알림
- 마지막 도시와 앱 설정의 로컬 저장

## 개인정보와 권한

현재 빌드는 GPS 위치 권한, 카메라, 연락처, 마이크, 광고 ID를 사용하지 않습니다.

앱 권한:

- `INTERNET`: Open-Meteo API 조회
- `ACCESS_NETWORK_STATE`: 네트워크 상태 확인
- `POST_NOTIFICATIONS`: 선택형 날씨 알림 표시

사용자가 입력한 도시명은 날씨 조회를 위해 Open-Meteo Geocoding API로 전송됩니다. 마지막 도시, 테마, 단위, 알림 설정은 기기 내부 SharedPreferences에 저장됩니다.

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Platform | Android Native |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM |
| Network | Retrofit, OkHttp, Moshi |
| Weather Data | Open-Meteo Forecast, Geocoding, Air Quality |
| Local Settings | SharedPreferences |
| Test | JUnit 4, Robolectric, Roborazzi |
| Build | Gradle Kotlin DSL, Version Catalogs |
| CI/CD | GitHub Actions |

## 저장소 구조

```text
app/                         Android 앱 소스
app/src/main/java/...        Kotlin + Compose UI / Repository / API 모델
app/src/main/res/            Android 리소스와 런처 아이콘
website/                     GitHub Pages 랜딩 페이지와 개인정보 처리방침
play_store/assets/           Play Store 아이콘, 피처 그래픽, 스크린샷
play_store/listing/          Play Console 등록용 입력 문서
docs/releases/               GitHub Release 본문용 릴리즈 노트
scripts/                     릴리즈/스토어 보조 스크립트
```

## 로컬 실행

Android Studio에서 이 저장소를 열고 Android SDK 경로가 잡혀 있는지 확인합니다. 환경변수가 없다면 `local.properties`에 SDK 경로를 지정할 수 있습니다.

```properties
sdk.dir=C:\\Users\\<USER>\\AppData\\Local\\Android\\Sdk
```

명령줄에서 실행할 수 있는 기본 검증:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

릴리즈 빌드에는 업로드 키스토어와 관련 환경변수가 필요합니다. 자세한 절차는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고하세요.

## GitHub Pages

랜딩 페이지는 `website/` 디렉터리에서 관리하며, `.github/workflows/pages.yml`이 GitHub Pages로 배포합니다.

```text
https://jeiel85.github.io/zephyr-sky-android/
```

Play Store 개인정보 처리방침 URL:

```text
https://jeiel85.github.io/zephyr-sky-android/privacy-policy.html
```

## Play Store 준비물

Play Console 등록용 초안과 그래픽 에셋은 아래 위치에 있습니다.

- 앱 아이콘: `play_store/assets/icon_512.png` (512 x 512)
- 피처 그래픽: `play_store/assets/feature_1024x500.png` (1024 x 500)
- 스크린샷: `play_store/assets/screenshots/*.png` (1080 x 1920)
- 등록 입력 문서: `play_store/listing/PLAY_CONSOLE_INTAKE.md`
- 개인정보 처리방침: `PRIVACY_POLICY.md`, `website/privacy-policy.html`

그래픽 에셋은 필요할 때 아래 스크립트로 재생성할 수 있습니다.

```powershell
python scripts/generate_assets.py
```
