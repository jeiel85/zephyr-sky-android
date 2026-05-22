# CHANGELOG.md

## v2.0.1 - 2026-05-22

### Fixed
- Android 앱 런처 및 Activity 표시명이 템플릿 값 `Weather`로 보이던 문제를 `Zephyr Sky`로 정렬했습니다.

### Build / CI
- GitHub Actions CI를 Flutter 명령에서 Gradle Kotlin DSL 기반 `./gradlew test`, `./gradlew assembleDebug` 흐름으로 전환했습니다.
- 태그 릴리즈 워크플로우를 Gradle 기반 `assembleRelease` 및 `bundleRelease` 산출물 업로드 방식으로 전환했습니다.
- Gradle 프로젝트명을 `Zephyr Sky`로 변경하고, 릴리즈 서명 환경변수 처리를 GitHub Secrets 기반으로 정렬했습니다.
- `debug.keystore`가 없는 로컬/CI 환경에서도 Android 기본 debug signing으로 디버그 APK를 빌드할 수 있도록 보완했습니다.
- GitHub Actions `Android CI`에서 테스트, 디버그 빌드, 에뮬레이터 앱 실행 검증 성공을 확인했습니다.

### Documentation
- `DEPLOYMENT.md`를 Kotlin/Compose Android Native 빌드, 서명, 버전, 산출물 경로 기준으로 갱신했습니다.
- `README.md`의 로컬 빌드 안내를 Android SDK 경로 설정 기준으로 갱신했습니다.
- Nightseed Survivor 방식과 동일하게 GitHub Release용 수기 릴리즈 노트와 Play Console용 다국어 릴리즈 노트 파일을 분리 관리하도록 추가했습니다.
- 리뉴얼 이전 GitHub Issues 백로그를 모두 닫고, 리뉴얼 이후 기준으로 새 이슈를 시작할 수 있도록 정리했습니다.

### Verification
- 로컬 `test`는 Android SDK 경로를 명령 환경변수로 지정해 성공했습니다.
- 로컬 `assembleDebug --no-configuration-cache`는 Android SDK 경로를 명령 환경변수로 지정해 성공했습니다.
- GitHub Actions `Android CI` 런 `26274383792`에서 테스트, 디버그 빌드, 에뮬레이터 앱 실행 스모크 테스트 성공을 확인했습니다.

## v2.0.0 - 2026-05-22

### Changed
- **프레임워크 전면 개편**: 기존 Flutter 기반의 앱 구조를 완전히 갈아엎고 **Kotlin & Jetpack Compose 기반 Android 네이티브 에디토리얼 날씨 저널 앱**으로 아키텍처 및 코드를 완전 전환.
- **아키텍처 및 디자인 패턴**: Clean Architecture 및 Jetpack MVVM (ViewModel, StateFlow) 패턴을 채택하여 비즈니스 로직과 UI 분리.

### Added
- **Gemini AI 에디토리얼 저널 생성**: Google Firebase AI SDK를 연동하여 실시간 기상 상태에 기반한 고유의 에디토리얼 기사와 일기 본문을 생성하는 AI 저널 비서 구축.
- **Room Database 로컬 저장소**: 작성한 날씨 저널 및 사진 메타데이터를 기기에 영속화하여 오프라인에서도 조회할 수 있는 영속성 레이어 확보.
- **CameraX 미디어 통합**: 날씨를 직접 포착해 저널 기록에 시각적으로 풍부한 사진을 촬영 및 저장하는 기능을 내장.
- **Roborazzi 비주얼 회귀 테스트**: JUnit 4, Robolectric 및 Roborazzi를 통한 UI 스크린샷 렌더링 검증 테스트 자동화 마련.
- **Gradle 버전 카탈로그 & Gradle Wrapper**: Gradle 8.10.2 및 `libs.versions.toml` 카탈로그를 활용한 현대적인 빌드 의존성 체계 확보.

### Removed
- **Flutter 프레임워크 리소스 제거**: `lib/`, `ios/`, `web/`, `windows/`, `macos/`, `linux/`, `pubspec.yaml` 등 기존 하이브리드 리소스 전면 폐기.

---

## v1.3.1 - 2026-05-05

### Build / CI
- GitHub Actions Android 에뮬레이터 스모크 테스트의 실행 대상을 디버그 빌드 패키지(`com.jeiel.zephyr_sky.debug`)와 실제 `MainActivity` 컴포넌트 기준으로 보정해 액티비티 탐색 실패를 방지.

## v1.3.0 - 2026-05-04

### Added
- **다국어 지원 (한국어/영어):** 앱 전체를 한국어와 영어로 사용할 수 있도록 지원
- 언어 설정 기능 추가 (설정 화면에서 한국어/영어 전환 가능)
- ARB 파일 기반 지역화 시스템 구축 (100+ 개 문자열 키)
- 모든 UI 문자열을 지역화 키로 교체 (하드코딩 제거)
- **오프라인 모드 지원:** 네트워크 불가 시에도 캐시된 날씨 정보 표시
- 네트워크 상태 감지 및 오프라인 배너 UI 추가
- 캐시 만료 정책 (30분) 및 Stale-while-revalidate 패턴 적용
- API 실패 시 캐시 데이터 폴리백
- 검색 화면 오프라인 상태 대응

### Changed
- SettingsScreen에 언어 선택 메뉴 추가
- main.dart에 locale 초기화 및 AppLocalizations 통합
- WeatherRepository에 캐시 타임스탬프 및 만료 체크 추가

---

## v1.2.2 - 2026-05-04

### Fixed
- Dart 예약어(`Importance.default`, `Priority.default`) 충돌로 인한 빌드 오류 수정
- 상태바 알림 표시 안 됨 및 권한 처리 로직 수정
- `settingsProvider` import 누락 및 `geocoding` 3.0.0 호환성 수정
- AndroidX 호환성 및 Java 21 빌드 환경 최적화
- Proguard 규칙 보완으로 릴리즈 빌드 실패 해결

### Changed
- 주간 예보 접기/펼치기 기능 추가
- UV 지수 표시 복구 및 역지오코딩 기반 위치명 표시
- 내비게이션 뎁스 유지 및 상태바 알림 설정 로직 보완

### Build / CI
- AAB 빌드 추가 및 GitHub Release에 APK/AAB 동시 업로드
- 태그 푸시 전 버전 검증 스크립트 추가
- `DEPLOYMENT.md` 배포 절차 보완

### Documentation
- Play Store 출시 가이드(`PLAY_STORE.md`) 추가
- 키스토어 생성 스크립트 및 빌드 최적화 문서 작성

---

## v1.2.0 - 2026-04-22

### Changed
- 모든 텍스트에 그림자(Shadow) 적용 및 반투명 배경 카드 농도 조절로 그라데이션 위 시인성 대폭 강화
- 상세 기상 정보(AQI, 기압, 시정 등)를 그리드 형태로 변경하여 시각적 혼잡도 감소
- 하단 액션 버튼을 상단 우측 햄버거 메뉴(Drawer)로 통합하여 메인 화면 공간 확보
- 스크롤 다운 새로고침(Pull-to-Refresh) 전면 적용
- 시간별 예보 영역에 스크롤 가능 여부 시각적 힌트 추가
- 주간 예보 데이터를 핵심 정보 위주로 컴팩트하게 구성
- 자외선(UV), 대기질(AQI) 등 색상 지표 위젯의 가용성 및 대비 보완
- 차트 내 텍스트 시인성 및 가독성 개선

### Fixed
- 설정 화면의 버전 표기 오류 수정 (1.1.0 → 1.2.0)

---

## v1.1.0 - 2026-04-22

### Added
- AQI(대기질 지수) 실시간 표시 및 등급 안내
- UV Index(자외선 지수) 및 위험 등급 안내
- 48시간 시간별 예보 (기존 24시간에서 확장)
- 16일 일별 예보 (기존 7일에서 확장)
- 강수 확률 시간별/일별 표시
- 일출/일몰 시간 정보
- 상세 기상 정보 (기압, 시정, 이슬점, 구름량)
- 날씨 경고 시스템 (강한 바람, 한파, 고온, 자외선, 대기질)
- 설정 화면 (다크 모드, °C/°F 단위 토글, 알림, 즐겨찾기)
- fl_chart 기반 기온 추이 선 그래프 및 강수 확률 막대 그래프
- 안드로이드 홈 스크린 위젯 (home_widget)
- 야외 활동 지수 (0-100 점수, 진행 바, 권장 메시지)
- 단위 테스트 10개 케이스 추가

### Documentation
- 상위 날씨 앱 분석 기반 개발 로드맵(ROADMAP.md) 작성
- README.md 전체 한국어 정리 및 기술 스택 대폭 강화

---

## v1.0.8 - 2026-04-21

### Build / CI
- 릴리즈 키스토어 서명 설정 추가 (GitHub Secrets 기반)
- AAB 빌드 제거, APK 중심 릴리즈로 단순화

### Documentation
- 배포 가이드(DEPLOYMENT.md) 업데이트 (릴리즈 서명, 태그 빌드 흐름)
- 버전 관리 체계 및 빌드번호 덮어쓰기 동작 명시

---

## v1.0.6 - 2026-04-21

### Fixed
- SDK 환경 최적화 및 pubspec.lock 초기화

### Build / CI
- AAB 빌드 제거, APK로 릴리즈 변경

---

## v1.0.5 - 2026-04-21

### Fixed
- R8(난독화) 빌드 에러 해결 — `proguard-rules.pro`에 보존 규칙 추가

### Build / CI
- GitHub Actions 릴리즈 테스트 자동화 추가

---

## v1.0.4 - 2026-04-21

### Build / CI
- GitHub Actions 빌드 결과물 경로 인식 오류 수정
- 빌드 결과물 확인(Check Build Artifacts) 단계 추가

---

## v1.0.3 - 2026-04-21

### Fixed
- Android 패키지명 불일치(`com.example.open_weather` vs `com.jeiel.zephyr_sky`)로 인한 런타임 크래시 해결
- `MainActivity.kt`를 올바른 경로로 이동 및 패키지 선언 수정
- `AndroidManifest.xml`에 `POST_NOTIFICATIONS`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED` 권한 추가
- R8(난독화) 설정 추가 — Flutter 프레임워크 및 플러그인 클래스 보존

---

## v1.0.2 - 2026-04-21

### Changed
- Open Weather → Zephyr Sky 브랜드 리브랜딩 (앱 이름, 패키지명, 라벨 전면 교체)
- 브랜드 소개 랜딩 페이지(website/) 리뉴얼

### Fixed
- 앱 실행 프리즈(Freeze) 현상 수정 — `main.dart` 초기화 로직에 `try-catch` 안전 장치 추가
- `NotificationService` 초기화 시 안드로이드 알림 권한 요청 로직 보완

---

## v1.0.1 - 2026-04-21

### Security
- 보안 감사 결과 반영

---

## v1.0.0 - 2026-04-20

### Added
- Flutter 프로젝트 초기화 및 Clean Architecture 구조 수립
- 현재 위치 기반 날씨 정보 조회
- 전 세계 도시 검색
- 시간별 예보 (24시간) 및 일별 예보 (7일)
- 날씨 아이콘/설명 동적 표시
- 동적 그라데이션 UI
- 마지막 위치 저장 및 복원
- 날씨 캐싱
- 상태바 알림
- `LocaleDataException` 해결 — `initializeDateFormatting('ko_KR', null)` 추가
