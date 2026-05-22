# Zephyr Sky - Editorial Weather Journal

<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

**Zephyr Sky**는 Google AI Studio 및 Gemini SDK를 탑재한 세련된 모바일 에디토리얼 날씨 저널 앱입니다. Kotlin과 Jetpack Compose로 작성되었으며, 사용자의 위치 데이터를 기반으로 기후 정보를 가져오고 이에 대한 지능형이고 정형화된 저널 기록을 생성합니다.

View this app in AI Studio: [AI Studio App](https://ai.studio/apps/965f2ec2-c074-4444-8ce9-1f3231025d4c)

---

## 🌟 Key Features

* **에디토리얼 날씨 저널**: 매일의 날씨 상태와 사용자의 기분을 기반으로 AI 저널 텍스트를 자동으로 구성합니다.
* **현대적인 Jetpack Compose UI**: Vibrant 그라데이션 테마, Glassmorphism 스타일 요소, 유려한 로딩/상태 전환 애니메이션.
* **Gemini SDK 연동**: Google Firebase AI SDK를 사용하여 온디바이스 및 클라우드 AI 요약 기능을 완벽 지원합니다.
* **로컬 오프라인 데이터 스토리지**: Room Database와 DataStore Preferences를 사용하여 날씨 기록 및 저널 로그를 로컬에 오프라인 우선으로 저장합니다.
* **CameraX 연동**: 오늘의 하늘 사진이나 날씨 기록과 어울리는 순간을 카메라로 촬영해 저널에 첨부할 수 있습니다.
* **단위 테스트 및 스크린샷 검증**: JUnit 4, Robolectric, Roborazzi를 통한 강력한 자동화 테스트 및 Screenshot visual regression 테스트 세트 제공.

---

## 🛠 Prerequisites & Tech Stack

* **OS**: Android (Min SDK 24, Target SDK 35/36)
* **IDE**: [Android Studio](https://developer.android.com/studio) (Koala 이상 권장)
* **Build Tool**: Gradle 8.10.2 + Kotlin DSL
* **Languages**: Kotlin 2.2.10
* **Framework**: Jetpack Compose (Material 3)
* **Libraries**:
  * **Firebase AI SDK**: Gemini 모델 통신
  * **Room DB**: 로컬 데이터 영속화
  * **Retrofit & Moshi**: 기상 정보 API 클라이언트
  * **CameraX**: 카메라 사진 촬영
  * **Play Services Location**: 위치 기반 기상 쿼리

---

## 🚀 How to Run Locally

### 1. 프로젝트 열기
Android Studio를 실행한 뒤, **Open**을 눌러 이 프로젝트 디렉토리(`D:\Project\zephyr-sky`)를 선택하여 오픈합니다. Android Studio가 프로젝트를 빌드하고 인덱싱할 때까지 대기합니다.

### 2. API 키 설정
프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 다음과 같이 Gemini API Key를 지정합니다.

```env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
```

*참고: `.env.example` 파일을 복사하여 `.env`로 리네임한 후 값을 변경하시면 편리합니다.*

### 3. 로컬 Android SDK 확인
로컬 빌드가 정상 작동하지 않을 경우 `ANDROID_HOME` 환경변수를 지정하거나, 프로젝트 루트의 `local.properties`에 Android SDK 경로를 설정하십시오.

### 4. 실행 및 배포
에뮬레이터 또는 물리적 Android 기기를 연결하고, 상단의 **Run** 버튼을 눌러 앱을 실행시킵니다.

---

## 🧪 Running Tests

로컬에서 단위 테스트 및 Robolectric, Roborazzi 비주얼 테스트 세트를 실행할 수 있습니다.

```bash
# 단위 테스트 및 비주얼 스크린샷 테스트 전체 실행
./gradlew test

# 디버그 APK 빌드
./gradlew assembleDebug
```
