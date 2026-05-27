# 📱 Play Store 출시 가이드

## 📋 앱 기본 정보

| 항목 | 내용 |
|------|------|
| **앱 이름** | 제퍼 스카이 (Zephyr Sky) |
| **패키지명** | com.jeiel.zephyr_sky |
| **현재 버전** | 1.2.2 (12) |
| **카테고리** | 날씨 |
| **콘텐츠 등급** | 전체 이용가 (3+) |
| **가격** | 무료 |

---

## 📝 스토어 리스팅 콘텐츠

### 🇰🇷 한국어 (기본)

#### 짧은 설명 (80자 이내)
```
아름다운 그라데이션과 미니멀한 디자인의 날씨 앱
```

#### 전체 설명
```
제퍼 스카이 - 당신의 하루를 밝게 비추는 날씨 앱

🌤️ 주요 기능:

✨ 실시간 날씨 정보
- 현재 위치 기반 정확한 날씨 정보
- 48시간 시간별 예보 (온도, 날씨, 강수확률)
- 16일 일별 예보 (최고/최저 기온, UV 지수)

🌡️ 상세 기상 정보
- AQI (대기질 지수) - 실시간 대기질 상태
- UV Index (자외선 지수) - 위험 등급 안내
- 기압, 시정, 이슬점, 구름량 등 상세 정보
- 일출/일몰 시간 정보

🎨 아름다운 UI/UX
- 날씨 상태에 따라 변하는 동적 그라데이션 배경
- 다크 모드 / 라이트 모드 지원
- fl_chart 기반 기온 추이 및 강수확률 그래프
- 날씨 기반 야외 활동 지수 (0-100)

⚙️ 맞춤 설정
- ℃ / ℉ 단위 전환
- 즐겨찾기 위치 관리 (다중 지역 저장)
- 날씨 경고 (강풍, 한파, 폭염, 자외선, 대기질)
- 상태바 알림 (앱 미실행 시에도 확인)

📱 시스템 통합
- 홈 스크린 위젯 지원 (Android)
- GitHub Actions 자동 빌드 및 배포

🌍 데이터 출처
- Open-Meteo API (무료, API 키 불필요)
- 전 세계 도시 검색 지원

특별한 날씨도 제퍼 스카이와 함께 확인하세요 ☀️
```

---

### 🇺🇸 English

#### Short Description (80 chars max)
```
Beautiful gradient weather app with minimalist design
```

#### Full Description
```
Zephyr Sky - Weather app that brightens your day

🌤️ Key Features:

✨ Real-time Weather Information
- Accurate weather based on current location
- 48-hour hourly forecast (temperature, conditions, precipitation)
- 16-day daily forecast (high/low temp, UV index)

🌡️ Detailed Meteorological Data
- AQI (Air Quality Index) - Real-time air quality status
- UV Index - Risk level alerts
- Detailed info: pressure, visibility, dew point, cloud cover
- Sunrise/sunset times

🎨 Beautiful UI/UX
- Dynamic gradient backgrounds that change with weather
- Dark mode / Light mode support
- Temperature trends & precipitation charts (fl_chart)
- Outdoor activity index (0-100) based on weather

⚙️ Customization
- ℃ / ℉ unit toggle
- Saved locations management (multiple cities)
- Weather alerts (high winds, cold waves, heat waves, UV, air quality)
- Status bar notifications (even when app is closed)

📱 System Integration
- Home screen widget support (Android)
- Automated CI/CD with GitHub Actions

🌍 Data Source
- Open-Meteo API (Free, no API key required)
- Global city search support

Experience beautiful weather forecasting with Zephyr Sky ☀️
```

---

## 🏷️ 키워드 (태그)

### 한국어
```
날씨, 기상, 예보, 미니멀, 그라데이션, 대기질, UV지수, 위젯, 다크모드
```

### English
```
weather, forecast, minimalist, gradient, air quality, UV index, widget, dark mode, hourly, daily
```

---

## 🖼️ 스토어 에셋 준비 체크리스트

### 아이콘 및 그래픽
- [ ] **앱 아이콘**: 512 x 512 px (PNG, 32-bit)
  - 현재 위치: `assets/icon/app_icon.png`
  - 생성 명령어: `flutter pub run flutter_launcher_icons`

- [ ] **기능 아이콘**: 512 x 512 px (PNG, 32-bit)
  - 다크 모드 버전도 준비

- [ ] **스크린샷** (필수)
  - **전화**: 2-8장 (16:9 또는 9:16 비율)
    - 해상도: 1080 x 1920 px (최소)
    - 최대: 3840 x 3840 px
  - **태블릿**: 7-8장 (권장)

### 스크린샷 촬영 가이드
1. 홈 화면 (현재 날씨 + 그래프)
2. 48시간 예보 화면
3. 16일 예보 화면
4. 검색 화면
5. 설정 화면 (다크 모드)
6. 위젯 화면
7. 날씨 경고 알림
8. 상세 기상 정보

---

## 🔐 서명 및 빌드 준비

### 1. 키스토어 생성 (최초 1회)
```bash
# 키스토어 생성 (비밀번호는 안전하게 보관하세요!)
keytool -genkey -v -keystore release.keystore \
  -alias zephyr_sky \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 생성된 키스토어를 android/app/ 로 이동
mv release.keystore android/app/
```

### 2. 환경 변수 설정 (GitHub Secrets)
```
RELEASE_STORE_FILE=release.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=zephyr_sky
RELEASE_KEY_PASSWORD=your_key_password
```

### 3. 릴리스 APK/AAB 빌드
```bash
# APK 빌드 (일반 배포)
flutter build apk --release

# AAB 빌드 (Play Store 권장)
flutter build appbundle --release

# 출력 위치
# APK: build/app/outputs/flutter-apk/app-release.apk
# AAB: build/app/outputs/bundle/release/app-release.aab
```

---

## 📄 개인정보 처리방침

### 필수 포함 항목
- [ ] 수집하는 데이터 종류 (위치 정보)
- [ ] 데이터 사용 목적 (날씨 정보 제공)
- [ ] 데이터 보관 기간 (로컬 저장, 서버 저장 안 함)
- [ ] 제3자 제공 (Open-Meteo API)
- [ ] 사용자 권리 (데이터 삭제 요청 등)

### 개인정보 처리방침 URL
```
https://jeiel85.github.io/zephyr-sky-android/privacy-policy.html
```
- ✅ HTML 버전: `website/privacy-policy.html`
- ✅ Markdown 버전: `PRIVACY_POLICY.md`

---

## 🎯 스토어 최적화 (ASO)

### 제목 최적화
- 한국어: 제퍼 스카이 - 미니멀 날씨 (30자)
- 영어: Zephyr Sky - Minimal Weather (30자)

### 설명 최적화 팁
1. 주요 키워드를 앞부분에 배치
2. 이모지 적절히 사용 (가독성 향상)
3. 기능별로 섹션 나누기
4. 혜택과 특징 강조

---

## ✅ 출시 전 체크리스트

### 기술적 검토
- [ ] `flutter analyze` 통과
- [ ] 릴리스 모드에서 테스트 완료
- [ ] 난독화 (R8/ProGuard) 적용 확인
- [ ] 인터넷 권한 확인
- [ ] 위치 권한 확인
- [ ] 알림 권한 확인
- [ ] 위젯 동작 확인

### 스토어 등록
- [ ] 개발자 계정 생성 ($25 일회 결제)
- [ ] 앱 등록 정보 입력
- [ ] 스크린샷 업로드
- [ ] 아이콘 및 배너 업로드
- [ ] 개인정보 처리방침 URL 입력
- [ ] 콘텐츠 등급 설문 완료
- [ ] 가격 및 배포 지역 설정

### 배포 설정
- [ ] **프로덕션 트랙** (전체 사용자)
- [ ] **베타 트랙** (테스터 그룹) - 권장
- [ ] **내부 테스트 트랙** (QA 팀) - 권장

---

## 📊 출시 후 관리

### 모니터링
- [ ] 크래시 리포트 확인 (Firebase Crashlytics 권장)
- [ ] 사용자 리뷰 모니터링
- [ ] 성능 지표 추적 (ANR, 시작 시간 등)

### 업데이트 주기
- 버그 수정: 즉시
- 기능 추가: 월 1-2회
- 주요 업데이트: 분기별

---

## 🔗 유용한 링크

- [Play Console](https://play.google.com/console)
- [Android Developer Guide](https://developer.android.com/distribute)
- [App Bundle Guide](https://developer.android.com/guide/app-bundle)
- [Store Listing Guide](https://developer.android.com/distribute/best-practices/store-presence)

---

**준비 완료! 🚀 이제 Play Console에서 앱을 등록하세요.**
