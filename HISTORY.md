# 프로젝트 이력 관리 (HISTORY.md)

## [2026-05-22] Nightseed Survivor 방식의 새 버전 릴리즈 절차 도입

### 작업
- `D:\Project\nightseed-survivor`의 새 버전 생성 절차를 확인.
- GitHub Release 본문용 수기 릴리즈 노트를 `docs/releases/vX.Y.Z.md`에 보관하는 방식을 Zephyr Sky에 도입.
- Play Console 입력용 다국어 릴리즈 노트를 `play_store/release_notes/vX.Y.Z.txt`에 별도 보관하는 방식을 도입.
- 태그 릴리즈 워크플로우가 `docs/releases/<tag>.md`를 GitHub Release 본문으로 사용하도록 변경.
- 앱 내부 기본 버전을 `2.0.1` / `versionCode 201`로 승격하고, 태그명과 `versionName` 불일치 시 릴리즈 빌드를 중단하도록 보완.
- `CHANGELOG.md`의 Unreleased 항목을 `v2.0.1 - 2026-05-22` 릴리즈 섹션으로 승격.
- Play Console 업로드용 AAB와 릴리즈 노트 txt를 바탕화면으로 복사하는 `scripts/export-play-store-release.ps1`를 추가.

### 변경 파일
- `.github/workflows/release.yml`
- `app/build.gradle.kts`
- `CHANGELOG.md`
- `DEPLOYMENT.md`
- `docs/releases/README.md`
- `docs/releases/v2.0.1.md`
- `play_store/release_notes/README.md`
- `play_store/release_notes/v2.0.1.txt`
- `scripts/export-play-store-release.ps1`
- `HISTORY.md`

### 검증
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat test` 성공.
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat assembleDebug --no-configuration-cache` 성공.
- 로컬: `app/build/outputs/apk/debug/output-metadata.json`에서 `versionCode 201`, `versionName 2.0.1` 확인.
- 로컬: `scripts/export-play-store-release.ps1` PowerShell 구문 검사 성공.
- 로컬: release AAB가 없는 상태에서 `scripts/export-play-store-release.ps1 -Version 2.0.1` 실행 시 `Release AAB not found. Build it first: .\gradlew.bat bundleRelease`로 안전하게 중단되는 것 확인.
- CI: GitHub Actions `Android CI` 런 `26276209625` 성공 확인.
- CI: GitHub Actions `Android CI` 런 `26276759639` 성공 확인.
- 릴리즈: 태그 `v2.0.1` 푸시 후 GitHub Actions `Android Build & Release` 런 `26277342080` 성공 확인.
- 릴리즈: GitHub Release `v2.0.1` 생성 및 수기 릴리즈 노트 본문 적용 확인.
- 릴리즈 산출물:
  - `app-release.aab` 11,916,967 bytes, SHA-256 `6ea8c78ce79033f4c0976c0783d2fb32b686cdf3c71ddec03451e660f6ae3d2e`
  - `app-release.apk` 12,243,793 bytes, SHA-256 `0e68e525a5134f1cbc2b45034302f7f2cfb96f2318ec416289a31b9309abb1db`
- 바탕화면 산출물:
  - `C:\Users\jeiel\OneDrive\바탕 화면\zephyr-sky-v2.0.1.aab`
  - `C:\Users\jeiel\OneDrive\바탕 화면\zephyr-sky-v2.0.1-release-notes.txt`

### 결과
- 새 버전 생성 시 수기 릴리즈 노트, Play Console 노트, 버전 파일, 태그 푸시, 바탕화면 업로드 파일 준비를 한 흐름으로 관리할 수 있게 정렬함.
- `v2.0.1` 릴리즈와 Play Console 업로드용 로컬 산출물 준비를 완료함.

## [2026-05-22] 리뉴얼 이전 GitHub Issues 백로그 정리

### 작업
- GitHub 열린 이슈 29건(`#19`~`#57` 중 열린 항목)을 확인.
- 모든 열린 이슈가 2026-05-04 생성된 Kotlin/Compose Android Native 리뉴얼 이전 백로그임을 확인.
- `#37 [Performance] Flutter 엔진 및 종속성 최신화` 등 현재 코드베이스와 직접 맞지 않는 Flutter 시절 항목이 포함되어 있어 전체 열린 이슈를 닫음.
- 각 이슈에는 리뉴얼 이전 백로그 정리 사유와 신규 작업은 리뉴얼 이후 기준으로 새 이슈를 생성한다는 코멘트를 남김.

### 변경 파일
- `HISTORY.md`

### 검증
- GitHub: `gh issue list --state open --limit 50` 결과 열린 이슈 없음 확인.
- 로컬: 문서 이력 변경만 수행하여 빌드/테스트는 생략.
- CI: GitHub Actions `Android CI` 런 `26275335018` 성공 확인.

### 결과
- GitHub Issues 백로그를 리뉴얼 이후 기준으로 새로 시작할 수 있도록 초기화함.

## [2026-05-22] Kotlin/Compose Android Native 빌드 및 릴리즈 파이프라인 정렬

### 작업
- `.github/workflows/ci.yml`에서 Flutter 설정을 제거하고 Gradle 기반 `./gradlew test`, `./gradlew assembleDebug` 및 Android 에뮬레이터 스모크 테스트 흐름으로 전환.
- `.github/workflows/release.yml`에서 Flutter 릴리즈 빌드를 제거하고 Gradle 기반 `test`, `assembleRelease`, `bundleRelease`, GitHub Release 업로드 흐름으로 전환.
- `settings.gradle.kts`의 `rootProject.name`을 `Zephyr Sky`로 변경.
- `app/build.gradle.kts`의 릴리즈 signingConfig를 GitHub Secrets 환경변수(`KEYSTORE_PATH`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`)와 호환되도록 점검 및 보완.
- `debug.keystore`가 없을 때도 Android 기본 debug signing으로 `assembleDebug`가 진행되도록 debug signingConfig 적용 조건을 보완.
- 릴리즈 워크플로우에서 태그명 기반 `VERSION_NAME`과 GitHub Actions 실행 번호 기반 `VERSION_CODE`를 Gradle property로 전달하도록 구성.
- `DEPLOYMENT.md`를 Kotlin/Compose Android Native 빌드 및 배포 절차에 맞게 전면 업데이트.

### 변경 파일
- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`
- `settings.gradle.kts`
- `app/build.gradle.kts`
- `README.md`
- `DEPLOYMENT.md`
- `HISTORY.md`
- `CHANGELOG.md`

### 검증
- 로컬: `.\gradlew.bat test` 실행 시 Android SDK 경로 미설정으로 구성 단계에서 실패.
- 로컬: `.\gradlew.bat assembleDebug` 최초 실행 시 Android SDK 경로 미설정으로 구성 단계에서 실패.
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat test` 성공.
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat assembleDebug --no-configuration-cache` 성공.
- 원인: 초기 실패는 `ANDROID_HOME` 환경변수 또는 `local.properties`의 `sdk.dir` 미설정, 이후 debug signingConfig의 `debug.keystore` 강제 참조 확인.
- CI: GitHub Actions `Android CI` 런 `26273486101` 성공 확인.

### 결과
- Flutter 기반 CI/CD 잔여 구성을 Android Gradle 빌드 기준으로 정렬함.
- 로컬 검증은 SDK 경로를 명령 환경변수로 지정하여 완료함.
- 원격 `main` 푸시 후 GitHub Actions 기준 테스트, 디버그 빌드, 에뮬레이터 앱 실행 검증까지 성공함.

## [2026-05-22] Android 앱 표시명 브랜드 정렬

### 작업
- Kotlin/Compose 전환 후 템플릿 값으로 남아 있던 Android 앱 표시명을 `Weather`에서 `Zephyr Sky`로 변경.

### 변경 파일
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`
- `HISTORY.md`
- `CHANGELOG.md`

### 검증
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat test` 최초 실행 시 기존 테스트 기대값(`Weather`) 불일치로 실패.
- 로컬: 앱 표시명 테스트 기대값을 `Zephyr Sky`로 정렬한 뒤 `.\gradlew.bat test` 성공.
- 로컬: `ANDROID_HOME=C:\Users\jeiel\AppData\Local\Android\Sdk` 지정 후 `.\gradlew.bat assembleDebug --no-configuration-cache` 성공.
- CI: GitHub Actions `Android CI` 런 `26274383792` 성공 확인.

### 결과
- 런처 및 Activity 라벨이 프로젝트 브랜드와 일치하도록 정렬함.

## [2026-05-22] Flutter 프로젝트에서 Kotlin/Compose Android 네이티브 Weather Journal 프로젝트로의 대규모 프레임워크 전환

### 작업 내용
- **기존 Flutter 관련 리소스 완전 삭제**: `lib/`, `ios/`, Flutter용 `android/` 래퍼, `web/`, `windows/`, `macos/`, `linux/`, `pubspec.yaml`, `pubspec.lock`, `analysis_options.yaml`, `l10n.yaml` 등 Flutter 설정 및 소스 일체 제거.
- **Android Kotlin/Compose 네이티브 소스 이식**: `D:\Project\editorial-weather-journal` 로부터 `app/` 소스 코드(Compose UI, Retrofit, Room DB, Gemini SDK, CameraX 연동 등)와 `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `.env.example`, `debug.keystore.base64`, `metadata.json` 등을 `zephyr-sky` 프로젝트로 완벽 복사 이식.
- **Gradle Wrapper 환경 구축**: 형제 프로젝트(`markscene-android`)로부터 `gradlew`, `gradlew.bat`, `gradle/wrapper/` 디렉토리를 복사해와 독립적 빌드 환경 확보.
- **Gradle 버전 업그레이드**: AGP 9.1.1 및 Kotlin 2.2.10 호환성을 고려하여 `gradle-wrapper.properties` 내 Gradle 버전값을 `8.10.2`로 고도화.
- **프로젝트 설정 및 명세 전면 개정**:
  - `.gitignore`를 Android 네이티브 개발 환경에 최적화하여 갱신.
  - `AGENTS.md`를 Android Kotlin Native 빌드/테스트 체계, 아키텍처, 기술 스택 위주로 개정.
  - `README.md`를 Weather Journal 브랜드에 맞춰 템플릿 정보 및 동작 방법 업데이트.
  - `ROADMAP.md`를 AI 저널 및 로컬 미디어 통합 개발 방향에 맞게 개편.

### 변경 파일
- **[DELETE]** `lib/`, `ios/`, `android/`, `web/`, `windows/`, `macos/`, `linux/`, `pubspec.yaml` 등 Flutter 관련 일체
- **[NEW]** `app/` (전체), `gradle/` (wrapper 및 catalog), `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `.env.example`, `debug.keystore.base64`, `metadata.json`, `gradlew`, `gradlew.bat`
- **[MODIFY]** `AGENTS.md`, `README.md`, `ROADMAP.md`, `HISTORY.md`

### 검증
- **로컬 빌드**: Gradle 빌드를 통해 전체 복사 파일들의 정렬 및 컴파일 설정 검사 진행 예정.
- **테스트**: 로컬 JUnit 및 Robolectric, Roborazzi UI 스크린샷 단위 테스트 수행 예정.

## [2026-05-05] CI 디버그 패키지 실행 경로 보정 및 버전 업데이트

### 작업
- 최신 실패 런(`25383172063`) 로그를 재분석해 디버그 빌드의 `applicationIdSuffix = ".debug"`와 CI 실행 대상 패키지 불일치를 확인.
- `.github/workflows/ci.yml`에서 앱 실행/프로세스 확인 대상을 `com.jeiel.zephyr_sky.debug` 기준으로 수정.
- 앱 버전을 `1.3.1+14`로 업데이트.

### 변경 파일
- `.github/workflows/ci.yml`
- `pubspec.yaml`

### 검증
- CI 재실행 후 성공 여부 모니터링 예정

### 결과
- 디버그 APK 설치 후 실제 설치 패키지 기준으로 앱 실행 검증이 가능하도록 정렬함.
- 후속 재시도에서 `adb install`의 streamed install 단계가 `Broken pipe (32)`로 실패해, CI 설치 방식을 `--no-streaming`으로 추가 보정함.

## [2026-05-05] GitHub Actions 에뮬레이터 앱 실행 실패 수정

### 작업
- `gh run view --log-failed`로 최신 실패 런(`25381071461`) 로그를 분석.
- `.github/workflows/ci.yml`의 에뮬레이터 앱 실행 명령을 `monkey` 방식에서 명시적 Activity 실행 방식(`adb shell am start -W -n com.jeiel.zephyr_sky/.MainActivity`)으로 변경.

### 변경 파일
- `.github/workflows/ci.yml`

### 검증
- 로컬: `flutter analyze` 실행 (기존 warning/info 다수 존재, 신규 오류 추가 없음 확인)
- 로컬: `flutter test` 실행 (기존 테스트 3건 실패 확인: `test/domain/entities/weather_test.dart`의 outdoorActivityLevel 기대값 불일치)
- CI: 커밋/푸시 후 GitHub Actions 재실행 성공 여부 모니터링 예정

### 결과
- CI 앱 실행 단계의 액티비티 탐색 실패 가능성을 제거하는 방향으로 워크플로를 보정함.

## [2026-05-05] GitHub CI 앱 실행 검증 단계 추가

### 작업 내용
- `.github/workflows/ci.yml` 신규 추가.
- `push(main)`/`pull_request` 기준으로 `flutter test`, `flutter build apk --debug`를 수행하는 기본 CI 파이프라인 구성.
- `reactivecircus/android-emulator-runner@v2` 기반 Android 에뮬레이터 스모크 테스트 잡을 추가해, 디버그 APK 설치 후 `MainActivity` 실행(`adb shell am start -W ...`) 및 프로세스 기동(`adb shell pidof ...`)까지 검증하도록 구성.

### 현재 상태
- GitHub Actions에서 테스트 + 빌드 + 앱 실행(에뮬레이터)까지 자동 검증 가능.

## [2026-05-04] Play Store 출시 준비 및 핵심 기능 개선 - Issues #4, #5, #8, #11, #13, #14

### 주요 변경 사항

#### Play Store 필수 항목
- **개인정보 처리방침 페이지:** GitHub Pages 호스팅을 위한 HTML 페이지 및 Markdown 버전 생성
  - 수집 데이터, 사용 목적, 제3자 제공, 사용자 권리 명시
  - URL: https://jeiel85.github.io/zephyr-sky/privacy-policy.html
- **PLAY_STORE.md 업데이트:** 개인정보 처리방침 체크리스트 완료 표시

#### 핵심 기능 추가
- **온볼딩 화면 구현 (Issue #8):**
  - 4페이지 구조: 환영 → 위치 권한 → 알림 권한 → 완료
  - 그라데이션 배경 및 애니메이션
  - SharedPreferences에 'seen_onboarding' 플래그 저장
  - 첫 실행 시에만 표시, 이후 홈 화면으로 바로 이동
- **날씨 공유 기능 (Issue #13):**
  - share_plus 패키지 추가
  - Drawer 메뉴에 공유 버튼 추가
  - 공유 텍스트: 도시명, 온도, 날씨 상태, 야외활동지수 포함

#### UI/UX 개선
- **Android Adaptive Icon (Issue #11):**
  - mipmap-anydpi-v26/ic_launcher.xml 및 ic_launcher_round.xml 생성
  - API 26+ 기기에서 적응형 아이콘 지원
- **지역화 문자열 추가:** 온볼딩 및 공유 관련 한국어/영어 문자열 추가

#### 안정성
- **API 클라이언트 개선 (Issue #14):**
  - ApiClient 클래스 구현
  - Rate Limiting: 최소 5초 호출 간격
  - Exponential Backoff: 최대 3회 재시도 (1s, 2s, 4s)
  - Timeout 처리 (10초)

### 변경된 파일
- 신규: website/privacy-policy.html, PRIVACY_POLICY.md
- 신규: lib/presentation/screens/onboarding_screen.dart
- 신규: lib/core/utils/api_client.dart
- 신규: android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml, ic_launcher_round.xml
- 수정: lib/main.dart (온볼딩 라우팅 로직)
- 수정: lib/presentation/screens/home_screen.dart (공유 기능 추가)
- 수정: lib/l10n/app_ko.arb, lib/l10n/app_en.arb (신규 문자열)
- 수정: pubspec.yaml (share_plus 추가)
- 수정: PLAY_STORE.md

---

## [2026-05-04] 오프라인 모드 최적화 구현 완료 - Issue #2

### 주요 변경 사항
- **네트워크 상태 감지:** `connectivity_plus` 패키지 추가 및 `ConnectivityService` 구현.
- **캐시 타임스탬프:** 날씨 데이터 캐시 시 저장 시간 기록, 30분 만료 정책 적용.
- **Stale-while-revalidate 패턴:** 캐시된 데이터를 먼저 표시한 후 백그라운드에서 갱신.
- **오프라인 폴리백:** API 실패 시 캐시된 데이터로 폴리백.
- **오프라인 UI:** `OfflineBanner` 위젯 구현 - 오프라인 상태, 캐시 만료, 마지막 업데이트 시간 표시.
- **검색 화면 대응:** 오프라인 상태에서 검색 입력 비활성화 및 안내 메시지 표시.

### 변경된 파일
- `pubspec.yaml`: `connectivity_plus` 패키지 추가
- `lib/core/utils/connectivity_service.dart`: 신규 생성 - 네트워크 상태 관리
- `lib/core/repositories/weather_repository_impl.dart`: 캐시 타임스탬프 및 만료 체크 추가
- `lib/domain/repositories/weather_repository.dart`: `CachedWeatherInfo` 클래스 및 `getCachedWeatherWithInfo()` 추가
- `lib/presentation/providers/weather_provider.dart`: `OfflineStatus` 클래스, 오프라인 폴리시 적용
- `lib/presentation/widgets/offline_banner.dart`: 신규 생성 - 오프라인 상태 배너
- `lib/presentation/screens/home_screen.dart`: 오프라인 배너 통합, 연결 상태 리스닝
- `lib/presentation/screens/search_screen.dart`: 오프라인 상태 검색 비활성화
- `lib/l10n/app_ko.arb`, `lib/l10n/app_en.arb`: 오프라인 관련 문자열 추가

### 검증
- 오프라인 상태에서 캐시된 데이터 정상 표시
- 캐시 만료 시 경고 표시
- 검색 화면 오프라인 시 입력 비활성화

---

## [2026-05-04] 다국어 지원(한국어/영어) 구현 완료 - Issue #1

### 주요 변경 사항
- **다국어 지원 시스템 구축:** Flutter의 `flutter_localizations`와 ARB 파일을 사용하여 한국어(ko)와 영어(en) 지원을 추가함.
- **l10n 설정:** `l10n.yaml` 설정 파일 및 `lib/l10n/` 디렉토리에 ARB 파일(`app_ko.arb`, `app_en.arb`) 생성 (총 100+ 개의 문자열 키).
- **main.dart 지역화:** `MaterialApp`에 `localizationsDelegates` 및 `supportedLocales` 추가, `AppLocalizations` 통합.
- **SettingsProvider 확장:** 언어 설정(`languageCode`) 저장 및 로드 기능 추가 (`SharedPreferences` 연동).
- **SettingsScreen 언어 선택 UI:** 설정 화면에 언어 선택 드롭다운 메뉴 추가 (한국어/영어 전환 가능).
- **전체 문자열 교체:** 하드코딩된 한국어 문자열을 모두 `AppLocalizations` 키로 교체 (Screens, Widgets, Services, Entities, API Sources 등).

### 변경된 파일
- `pubspec.yaml`: `flutter_localizations` 및 `intl` 패키지 추가
- `l10n.yaml`: 신규 생성
- `lib/l10n/app_ko.arb`, `lib/l10n/app_en.arb`: 신규 생성
- `lib/l10n/app_localizations.dart`, `lib/l10n/app_localizations_en.dart`, `lib/l10n/app_localizations_ko.dart`: 자동 생성
- `lib/main.dart`: Locale 설정 및 AppLocalizations 통합
- `lib/presentation/providers/settings_provider.dart`: 언어 설정 저장/로드 로직 추가
- `lib/presentation/screens/settings_screen.dart`: 언어 선택 UI 추가
- `lib/presentation/screens/home_screen.dart`, `search_screen.dart`: 문자열 지역화
- `lib/presentation/widgets/weather_chart.dart`: 문자열 지역화
- `lib/core/utils/weather_helper.dart`, `notification_service.dart`, `location_service.dart`, `home_widget_service.dart`: 문자열 지역화
- `lib/data/sources/weather_api_source.dart`: 문자열 지역화
- `lib/domain/entities/weather.dart`: 지역화 메서드 추가

### 검증
- 모든 하드코딩 문자열이 ARB 키로 대첸됨
- SettingsProvider에서 언어 설정이 정상적으로 저장/로드됨
- SettingsScreen에서 언어 전환이 UI에 반영됨

---

## [2026-05-04] 도메인 엔티티 지역화 지원 추가
### 주요 변경 사항
- **Weather 엔티티 지역화 메서드 추가:** `Weather` 클래스 내 하드코딩된 한국어 문자열을 대체하기 위해 `AppLocalizations`를 사용하는 지역화 메서드들을 추가함.
    - `airQualityLevelLocalized(AppLocalizations l10n)`
    - `uvRiskLevelLocalized(AppLocalizations l10n)`
    - `outdoorActivityLevelLocalized(AppLocalizations l10n)`
    - `outdoorActivityMessageLocalized(AppLocalizations l10n)`
- **하위 호환성 유지:** 기존의 getter 메서드(`airQualityLevel`, `uvRiskLevel` 등)는 그대로 유지하여 기존 코드와의 호환성을 보장함.

---

## [2026-05-04] 플레이 스토어 Top 10 진입을 위한 앱 개선 전략 수립 및 이슈 등록 완료
### 주요 변경 사항
- **Top 10 진입 전략 수립:** UI/UX, Performance, Security & Privacy, Reliability & Stability, Engagement & Retention, ASO & Marketing 분야의 50개 개선안 도출.
- **GitHub 이슈 등록:** 해당 50개의 개선 사항을 프로젝트 파이프라인으로 관리할 수 있도록 GitHub 이슈에 일괄 등록 완료.

---

## [2026-04-22] 가독성 및 UI/UX 레이아웃 대폭 개선 (v1.2.0)

### 주요 개선 사항
- **가독성 강화:** 모든 텍스트에 그림자(Shadow) 적용 및 반투명 배경 카드 농도 조절로 그라데이션 위 시인성 확보
- **레이아웃 재배치:** 
    - 상세 기상 정보(AQI, 기압, 시정 등)를 그리드 형태로 변경하여 시각적 혼잡도 감소
    - 하단 액션 버튼을 상단 우측 햄버거 메뉴(Drawer)로 통합하여 메인 화면 공간 확보
- **UX 최적화:**
    - 스크롤 다운 새로고침(Pull-to-Refresh) 전면 적용
    - 시간별 예보 영역에 스크롤 가능 여부를 보여주는 시각적 힌트 추가
    - 주간 예보 데이터를 핵심 정보 위주로 컴팩트하게 구성
- **디자인 세밀화:** 자외선(UV), 대기질(AQI) 등 색상 지표가 포함된 위젯의 가용성 및 대비 보완

---

## [2026-04-22] 상위 날씨 앱 분석 기반 전체 구현 완료 (v1.1.0)

### Phase 1: 확장 날씨 데이터 표시
- **Weather 엔티티 확장:** AQI, UV Index, 강수 확률, 일출/일몰, 기압, 시정, 이슬점, 구름량 필드 추가
- **WeatherModel 확장:** 48시간 시간별 예보, 16일 일별 예보 파싱 지원
- **WeatherApiSource 확장:** Open-Meteo 확장 파라미터 및 Air Quality API 연동
- **HomeScreen UI 개선:** 확장 날씨 정보 표시 (일출/일몰, UV, 강수 확률, 상세 기상 정보)
- **ROADMAP.md 생성:** 상위 날씨 앱 분석 기반 5단계 개발 로드맵 수립

### Phase 2: 날씨 알림 및 설정 기능
- **NotificationService 확장:** 날씨 경고 로직 추가 (강한 바람, 한파, 고온, 강수 확률, 대기질, 자외선)
- **SettingsProvider 생성:** 다크 모드, °C/°F 단위 토글, 알림 설정, 즐겨찾기 위치 관리
- **SettingsScreen 추가:** 외관, 단위, 알림, 즐겨찾기, 앱 정보 설정 화면
- **HomeScreen 개선:** 설정 버튼 추가

### Phase 3: 날씨 그래프 (fl_chart)
- **fl_chart 패키지 추가:** 기온 추이 선 그래프, 강수 확률 막대 그래프
- **WeatherChart, PrecipitationChart 위젯 구현**
- **HomeScreen에 그래프 표시**

### Phase 4: 안드로이드 홈 스크린 위젯
- **home_widget 패키지 추가**
- **AndroidManifest.xml:** widget receiver 등록
- **위젯 레이아웃:** home_widget_layout.xml, widget_background.xml
- **HomeWidgetReceiver (Kotlin):** 위젯 데이터 표시
- **HomeWidgetService (Dart):** 날씨 업데이트 시 위젯 자동 갱신

### Phase 5: 야외 활동 지수 및 테스트
- **Weather 엔티티:** outdoorActivityScore, outdoorActivityLevel, outdoorActivityMessage 추가
- **HomeScreen:** 야외 활동 지수 UI 표시 (점수, 진행 바, 권장 메시지)
- **단위 테스트 추가:** Weather, AQI, UV, 야외 활동 등 10개 테스트 케이스

### 커밋 목록
- `02a8a7f` feat: 확장 날씨 데이터 추가 (AQI, UV, 48시간/16일 예보, 상세 기상 정보)
- `6c41067` docs: 상위 날씨 앱 분석 기반 개발 로드맵 작성
- `e2d69b7` feat: Phase 2 - 날씨 경고 및 설정 기능 추가
- `1df59ae` docs: Phase 1-2 구현 이력 추가
- `6766fc7` feat: Phase 3-5 전체 기능 구현 완료

### 현재 상태
- 상위 날씨 앱의 대부분의 기능을 구현한 종합 날씨 앱
- AQI, UV Index, 강수 확률, 일출/일몰 등 다양한 날씨 정보 제공
- 사용자 설정 (다크 모드, 단위, 알림, 즐겨찾기) 지원
- 날씨 경고 시스템 및 야외 활동 지수
- 안드로이드 홈 스크린 위젯 지원
- 단위 테스트 커버리지 확대

## [2026-04-20] 프로젝트 시작 및 초기 설정

### 작업 내용
- Flutter (Dart)를 프레임워크로 선정하고 프로젝트 `open_weather` 초기화.
- Open-Meteo API를 날씨 데이터 소스로 결정.
- 미니멀리즘 디자인 철학 및 상태바 알림 기능 명세 수립.
- Git 저장소 설정 및 초기 파일 구조 생성 완료.
- `HISTORY.md`를 통한 이력 관리 체계 구축.

### 현재 상태
- Flutter 프로젝트 초기화 및 Clean Architecture 구조 수립 완료.
- `shared_preferences`를 이용한 안정적인 로컬 저장소 체계 구축.
- 안드로이드 빌드 환경 최적화 (SDK 36 강제 적용 및 Desugaring 활성화).
- 실기기(SM-S921N) 설치 및 GitHub 저장소 동기화 완료.
- 앱 실행 시 `LocaleDataException` 발생 확인 (다음 세션 수정 예정).

## [2026-04-20] 회색 화면 해결 및 핵심 기능 강화

### 작업 내용
- `lib/main.dart`에 `initializeDateFormatting('ko_KR', null)`을 추가하여 `LocaleDataException` 및 회색 화면 오류 해결.
- `Weather` 엔티티에 `weatherIcon` 로직을 추가하여 날씨 상태(맑음, 비, 눈 등)에 맞는 동적 아이콘 표시 구현.
- `HomeScreen`의 현재 날씨, 시간별 예보, 주간 예보 영역에 동적 아이콘 적용.
- `SearchScreen`의 검색 방식을 `onChanged`에서 `onSubmitted`로 변경하여 불필요한 API 호출 방지 및 UX 개선.
- 검색 결과 선택 시 `HomeScreen`의 날씨 데이터가 즉각적으로 반영되도록 연동 완료.

### 현재 상태
- 앱 실행 시 초기 화면이 정상적으로 렌더링되며, 현재 위치 기반 날씨 정보를 성공적으로 가져옴.
- 상태바 알림(Notification)이 날씨 업데이트 시마다 정상적으로 갱신됨.
- 검색 기능을 통해 전 세계 도시의 날씨를 조회할 수 있음.
- **예정:** 조회된 위치 정보를 로컬에 저장하여 앱 재실행 시 마지막으로 본 위치를 표시하는 기능 추가.

## [2026-04-21] 에이전트 작업 지침(AGENTS.md) 수립 및 문서 최적화

### 작업 내용
- **에이전트 전용 가이드 생성(`AGENTS.md`):**
    - AI 에이전트가 프로젝트 접속 시 즉시 규칙을 파악할 수 있도록 최적화된 지침서 작성.
    - 기존 `DEVELOPMENT_GUIDE.md`를 `AGENTS.md`로 승격 및 보완하여 AI 협업 효율성 극대화.
- **문서 체계 확립:** `HISTORY.md` (이력), `DEPLOYMENT.md` (배포), `AGENTS.md` (규칙) 3대 관리 체계로 정리 완료.

### 현재 상태
- 프로젝트의 모든 기술적 맥락과 작업 규칙이 에이전트 친화적으로 명문화됨.
- 차후 세션에서도 동일한 퀄리티와 규칙으로 작업할 수 있는 환경 구축.

## [2026-04-21] GitHub Pages 배포 설정 심화 보완

### 작업 내용
- **워크플로우 강화:**
    - `actions/configure-pages@v5` 단계를 추가하여 배포 환경을 자동으로 최적화.
    - `concurrency` 설정을 통해 중복 빌드 충돌 방지.
    - `environment` 명시적 선언을 제거하여 자동 생성된 `github-pages` 환경과의 권한 충돌 가능성 제거.
- **Git 커밋:** 변경 사항 즉시 반영 및 푸시 준비 완료.

### 현재 상태
- GitHub Pages 설정을 "GitHub Actions"로 변경한 후 발생하는 권한 문제를 해결하기 위한 스크립트 고도화 완료.

## [2026-04-21] R8 빌드 오류 해결 및 v1.0.5 출시

### 작업 내용
- `android/app/proguard-rules.pro`에 `com.google.android.play.core` 관련 경고 무시(-dontwarn) 규칙 추가하여 R8 빌드 에러 해결.
- `pubspec.yaml` 버전을 1.0.5로 업데이트하여 신규 릴리즈 시도.
- **최종 검증 완료**: GitHub Actions 빌드 성공 및 릴리즈 APK 실기기 동작 확인 완료.
- **저장소 정리**: 중복된 Draft 릴리즈 삭제 및 최신 버전(v1.0.5) 중심 릴리즈 페이지 최적화.

## [2026-04-21] GitHub Actions 빌드 최적화 및 v1.0.4 출시

### 작업 내용
- `release.yml` 워크플로우의 빌드 결과물 경로 인식 로직 개선 (*.apk, *.aab 와일드카드 적용).
- 빌드 결과물 확인(Check Build Artifacts) 단계 추가하여 CI/CD 투명성 확보.
- `pubspec.yaml` 버전을 1.0.4로 업데이트하여 신규 릴리즈 트리거.

## [2026-04-21] GitHub 동기화 및 기능 개선

### 작업 내용
- 로컬 변경 사항(intl 초기화) 커밋 및 GitHub 최신 소스 동기화 시도.
- `HISTORY.md`를 통한 세션 이력 관리 시작.
- **마지막 조회 위치 정보 저장 및 복원 기능 구현:**
    - `WeatherRepository`에 `saveLastLocation`, `getLastLocation` 메서드 추가.
    - `SharedPreferences`를 이용해 마지막으로 날씨를 조회한 위도, 경도, 도시명을 로컬에 저장.
    - 앱 재실행 시 현재 위치 정보 획득 전, 마지막으로 본 위치의 날씨를 먼저 동기화하도록 `HomeScreen` 로직 개선.

## [2026-04-21] 앱 실행 불가(Crash) 문제 긴급 해결 및 빌드 최적화 (v1.0.3)

### 작업 내용
- **Android 패키지 구조 동기화:**
    - `build.gradle.kts`와 `MainActivity.kt`의 패키지명 불일치(`com.example.open_weather` vs `com.jeiel.zephyr_sky`)로 인한 런타임 크래시 해결.
    - `MainActivity.kt`를 올바른 경로(`android/app/src/main/kotlin/com/jeiel/zephyr_sky/`)로 이동 및 패키지 선언 수정.
- **Android 권한 보완:**
    - `AndroidManifest.xml`에 `POST_NOTIFICATIONS`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED` 권한 추가하여 알림 및 백그라운드 작업 안정화.
- **R8(난독화) 설정 추가:**
    - `proguard-rules.pro` 파일을 생성하여 릴리스 빌드 시 Flutter 프레임워크 및 플러그인 클래스가 삭제되지 않도록 보존 설정 적용.

### 현재 상태
- 앱 실행 즉시 크래시가 발생하는 패키지명 오류가 완전히 해결됨.
- 릴리스 모드 빌드 시 발생할 수 있는 클래스 누락 방지 설정 완료.
- **사용자 가이드:** 수정된 소스로 새 APK를 빌드하여 재실행 권장.
## [2026-04-21] 앱 실행 안정성 강화 및 오류 수정 (v1.0.2)

### 작업 내용
- **앱 실행 프리즈(Freeze) 현상 수정:**
    - `main.dart` 초기화 로직에 `try-catch` 안전 장치를 추가하여 특정 서비스 실패 시에도 앱 실행 보장.
    - `NotificationService` 초기화 시 안드로이드 알림 권한 요청 로직 보완 및 에러 핸들링 추가.
- 모든 기능 정상 동작 확인 및 최종 안정화 버전 v1.0.2 배포.

### 현재 상태
- **브랜드 리브랜딩 완료 (Open Weather → Zephyr Sky).**
    - 모든 앱 이름, 패키지명(`com.jeiel.zephyr_sky`), 앱 라벨 업데이트 완료.
    - 브랜드 소개 랜딩 페이지(`website/`) 리뉴얼 완료.
- **GitHub Actions CI/CD 최적화:**
    - `release.yml` 빌드 결과물 경로 인식 오류 수정 및 빌드 검증 단계 추가.
    - `v*` 태그 기반 자동 릴리즈 프로세스 안정화.
- **안정성이 대폭 강화된 v1.0.0 정식 출시 준비 완료.**
- 초기화 중단 문제 및 보안 감사 결과가 모두 반영된 완벽한 상태.
