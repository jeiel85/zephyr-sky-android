# Play Console 최초 등록 인테이크 시트

> Play Console(https://play.google.com/console)에서 "앱 만들기 → 스토어 등록정보 → 데이터 안전 → 콘텐츠 등급 → 타겟층 → 프로덕션 출시" 순서대로 진행할 때 그대로 붙여넣을 수 있도록 정리한 문서입니다.
> 모든 값은 현재 저장소의 코드/문서에서 **검증된 값**만 반영했습니다. 추정한 값은 `⚠️ 확인 필요` 표시를 달아두었습니다.
>
> **현재 기준 버전: v2.0.3** (mock 제거, Open-Meteo 실 API 연동)

---

## 0. 사전 확인사항 (사용자 직접)

| 항목 | 상태 | 메모 |
|---|---|---|
| Google Play Developer 계정 ($25 일회) | ⚠️ 확인 필요 | 계정 미보유 시 https://play.google.com/console/signup 에서 신규 등록 |
| 개발자 표시 이름 | ⚠️ 확인 필요 | 예: `Jeiel` |
| 연락처 이메일 (공개) | `jeiel85@gmail.com` (PRIVACY_POLICY.md 기준) | Play Console 공개 표시 |
| 업로드 키스토어 (`my-upload-key.jks`) | ❌ **미존재** | 본 문서 하단 §6 참고 |
| 그래픽 자산 (아이콘 512, 피처 1024×500, 폰 스크린샷 ≥2장) | ✅ **준비 완료** | `play_store/assets/` 및 `website/assets/`에 보관 |

---

## 1. 앱 만들기 (Create app)

| 필드 | 값 |
|---|---|
| **앱 이름 (App name)** | `Zephyr Sky` |
| **기본 언어 (Default language)** | `한국어 - ko-KR` |
| **앱 또는 게임** | 앱 (App) |
| **무료 또는 유료** | 무료 (Free) |
| **개발자 프로그램 정책 동의** | 체크 |
| **미국 수출법 동의** | 체크 |

---

## 2. 스토어 등록정보 (Main store listing) — 한국어 (기본)

### 앱 이름 (30자 이내)
```
Zephyr Sky
```

### 간단한 설명 (80자 이내)
```
아름다운 그라데이션과 미니멀한 디자인의 날씨 앱
```

### 자세한 설명 (4000자 이내) — 실제 빌드 기준 보수 버전
```
Zephyr Sky — 잔잔한 다크 톤의 미니멀 날씨 앱

별이 떠 있는 듯한 차분한 그라데이션 배경 위에서, 오늘 하루 입을 옷과 우산 챙김 여부를 빠르게 판단할 수 있도록 도와드립니다.

주요 기능
- 도시 이름으로 날씨를 빠르게 검색 (서울, 부산, 제주, Tokyo, New York 등 주요 도시 추천)
- 한눈에 보이는 큰 숫자로 현재 기온 표시
- 시간대별 6칸 예보 카드
- 대기질·자외선·습도·바람을 한 화면에 정리
- 다크 / 라이트 / 시스템 자동 테마 전환
- 섭씨·화씨 단위 전환
- 상태바 알림으로 현재 기온 상시 표시 (선택)
- 강수·한파·폭염·미세먼지 안내 알림 (선택)

개인정보가 필요 없는 가벼운 앱
- 위치 권한을 요청하지 않습니다. 도시 이름은 직접 검색합니다.
- 카메라·연락처·마이크 등 민감 권한을 사용하지 않습니다.
- 알림 권한(POST_NOTIFICATIONS)만 선택적으로 요청합니다.
- 모든 설정값은 기기 내부에만 저장되며, 외부 서버로 전송되지 않습니다.

조용한 화면이 필요한 분께
Zephyr Sky는 광고와 추적이 없는 미니멀 날씨 도구입니다.
```

> ✅ **코드 기준 검증 완료** (2026-05-23):
> - 매니페스트 권한: `POST_NOTIFICATIONS` 단 1개
> - `WeatherRepository` 는 mock 데이터를 반환 (실 API 호출 없음)
> - 위치/카메라/Gemini 코드 없음
> - 데이터는 SharedPreferences("skyline_weather_prefs")로 로컬에만 저장
> - 따라서 README 의 "Gemini AI·CameraX·실시간 API" 문구는 본 빌드에 적용 불가. 위 설명은 실제 동작에 맞춰 보수적으로 다시 썼습니다.

### 앱 아이콘
- ✅ `play_store/assets/icon_512.png` (512×512, ~52 KB)

### 피처 그래픽
- ✅ `play_store/assets/feature_1024x500.png` (1024×500, ~103 KB)

### 폰 스크린샷 (1080×1920, 다크 톤 모의)
- ✅ `play_store/assets/screenshots/01-home.png` — 서울 / 22° / Clear
- ✅ `play_store/assets/screenshots/02-forecast.png` — 제주 / 26° / Sunny
- ✅ `play_store/assets/screenshots/03-detail.png` — 부산 / 24° / Clouds
- 생성 스크립트: `scripts/generate_assets.py` (Pillow 기반, 재실행 가능)

### 동영상 (선택)
- YouTube URL — 사용 안 함

---

## 3. 앱 카테고리 및 연락처

| 필드 | 값 |
|---|---|
| **앱 카테고리** | 날씨 (Weather) |
| **태그** | 날씨, 기상, 예보, 미니멀, 대기질, 위젯, 다크모드 |
| **이메일 주소 (공개)** | `jeiel85@gmail.com` |
| **전화번호** | 비공개 (선택) |
| **웹사이트** | `https://jeiel85.github.io/zephyr-sky-android/` |
| **개인정보처리방침 URL** | `https://jeiel85.github.io/zephyr-sky-android/privacy-policy.html` |

---

## 4. 앱 콘텐츠 (App content) — 필수 설문

### 4.1 개인정보처리방침
- URL: `https://jeiel85.github.io/zephyr-sky-android/privacy-policy.html`
- 백업 마크다운: `PRIVACY_POLICY.md`

### 4.2 광고 (Ads)
- **앱에 광고 있음?** → **아니오** (광고 SDK 미사용)

### 4.3 앱 액세스 권한 (App access)
- **모든 기능이 제한 없이 사용 가능** (로그인 불필요)

### 4.4 콘텐츠 등급 (Content rating) 설문 추천 답안

| 질문 | 답변 |
|---|---|
| 폭력 | 없음 |
| 성적 콘텐츠 | 없음 |
| 비속어 | 없음 |
| 통제 약물 | 없음 |
| 도박 (시뮬레이션 포함) | 없음 |
| 사용자 위치 공유 | ⚠️ **확인 필요** — 매니페스트에 위치 권한 미선언이므로 현재 빌드는 "아니오"가 정확함. 단 향후 위치 기능 추가 예정이면 "예" 표시 |
| 디지털 구매 가능성 | 없음 |
| 사용자 간 상호작용 | 없음 |

→ 예상 등급: **전체 이용가 (Everyone / 3+)**

### 4.5 타겟층 및 콘텐츠 (Target audience)
- 타겟 연령대: **13세 이상 모든 연령** (Weather 카테고리 일반적 선택)
- 어린이를 대상으로 하지 않음

### 4.6 뉴스 앱
- 뉴스 앱 → **아니오**

### 4.7 COVID-19 추적 / 접촉 추적 앱
- → **아니오**

### 4.8 데이터 안전 (Data safety) — v2.0.3 실 API 기준

**Q: 이 앱이 사용자 데이터를 수집하거나 공유하나요?** → **예** (검색기록만)

**수집·공유하는 데이터**
| 데이터 유형 | 수집 | 공유 | 필수/선택 | 용도 | 처리 |
|---|---|---|---|---|---|
| 앱 활동 → 앱 내 검색 기록 (사용자가 입력한 도시명) | 예 | **아니오** | 필수 (앱 기능) | 날씨 조회 (Geocoding → Forecast) | 전송 중 HTTPS, 영구 보관 안 함 |
| 위치 (대략적/정확한) | **아니오** | 아니오 | - | - | 매니페스트에 위치 권한 미선언, 사용자 GPS 미사용 |
| 개인 식별 정보(이름/이메일/전화 등) | 아니오 | - | - | - | - |
| 기기 또는 기타 ID | 아니오 | - | - | - | - |
| 광고 ID | 아니오 | - | - | - | - |

**근거 (코드 기준):**
- 매니페스트 권한: `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`. 위치/카메라/연락처/저장소 등은 미선언.
- 외부 호출: 사용자가 입력한 도시명(예: "서울")을 Open-Meteo Geocoding(`geocoding-api.open-meteo.com`)으로 전송하여 위경도를 얻은 뒤, Forecast / Air-Quality 엔드포인트에 그 좌표를 전달함.
- 사용자 식별 정보와 연결되지 않음. 검색어는 앱이 어떤 사용자 식별자와도 결합하지 않고 전송됨.
- 광고 SDK, 분석 SDK (Firebase Analytics, Crashlytics 등) 미통합.

**전송 중 암호화**: ✅ 예 (모든 Open-Meteo 호출은 HTTPS).
**데이터 삭제 요청**: 앱 측 보유 데이터는 SharedPreferences 로컬뿐이며, 앱 삭제로 즉시 제거됨. Open-Meteo는 서버 측 사용자 데이터 저장이 없음(공식 문서 참고).

### 4.9 정부 앱 / 금융 앱
- → **아니오**

---

## 5. 그래픽 자산 준비 가이드 (현재 ✅ 준비 완료)

### 5.1 앱 아이콘 (512×512)
준비 파일: `play_store/assets/icon_512.png`

- 규격: 512×512 PNG
- 용도: Play Console 앱 아이콘

### 5.2 피처 그래픽 (1024×500)
준비 파일: `play_store/assets/feature_1024x500.png`

- 규격: 1024×500 PNG
- 용도: Play Console 피처 그래픽 및 GitHub Pages 히어로 이미지

### 5.3 폰 스크린샷 (≥ 2장)
준비 파일:

1. `play_store/assets/screenshots/01-home.png`
2. `play_store/assets/screenshots/02-forecast.png`
3. `play_store/assets/screenshots/03-detail.png`

각 파일은 1080×1920 PNG이며, 현재 앱의 다크 톤 날씨 화면을 기준으로 한 Play Store 등록용 목업입니다.

캡처 방법:
```powershell
# 에뮬레이터 또는 실 기기 연결 후
adb shell screencap -p /sdcard/zephyr-1.png
adb pull /sdcard/zephyr-1.png play_store/assets/screenshots/
```

저장 위치: `play_store/assets/screenshots/*.png`

> 실기기 스크린샷이 필요하면 위 목업을 대체해 같은 경로에 저장하면 됩니다. 현재 작업에서는 연결된 기기를 건드리지 않고 기존 준비 에셋을 사용했습니다.

---

## 6. 업로드 키스토어 (현재 ❌ 미존재)

> **⚠️ 보안 경고**: 업로드 키스토어는 **앱 평생 동일한 키**를 사용해야 합니다. 비밀번호 분실 시 동일 앱으로 업데이트가 불가능합니다. **사용자가 직접 비밀번호를 정하고 안전한 곳에 보관**해야 합니다.

### 키스토어 생성 (사용자 직접 실행)
```powershell
# 프로젝트 루트에서 실행
$keystoreDir = "D:\Project\zephyr-sky-android"
keytool -genkeypair -v `
  -keystore "$keystoreDir\my-upload-key.jks" `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -alias upload
# Common Name, Organizational Unit 등 입력 후 비밀번호 2회 입력
```

### 빌드용 환경변수 (.env 또는 시스템 환경)
```env
RELEASE_STORE_FILE=D:\Project\zephyr-sky-android\my-upload-key.jks
RELEASE_STORE_PASSWORD=<사용자 결정>
RELEASE_KEY_ALIAS=upload
RELEASE_KEY_PASSWORD=<사용자 결정>
```

### AAB 빌드
```powershell
.\gradlew.bat bundleRelease
# 출력: app\build\outputs\bundle\release\app-release.aab
```

### Play Console 업로드용 산출물 추출
```powershell
.\scripts\export-play-store-release.ps1
# 바탕화면에 zephyr-sky-v2.0.3.aab + zephyr-sky-v2.0.3-release-notes.txt 복사
```

---

## 7. 출시 트랙 및 릴리스 노트

### 출시 트랙
**최초 등록은 ❗ 반드시 "내부 테스트(Internal testing)" 트랙 권장**
- 프로덕션 직행 시 검토 거부되면 수정 후 재제출까지 며칠 소요됩니다.
- 내부 테스트 → 비공개 테스트 → 프로덕션 단계적 확대가 안전합니다.

### 릴리스 이름
```
2.0.3 (203)
```

### 릴리스 노트 (최초 출시용 권장 텍스트)
```
Zephyr Sky 첫 정식 출시
• Open-Meteo 무료 API로 실시간 날씨, 시간별 예보, 대기질 정보 제공
• 도시 이름 검색으로 전 세계 주요 도시의 날씨 조회
• 다크/라이트/시스템 자동 테마 전환
• 상태바 알림으로 현재 기온 상시 표시 (선택)
```

> 내부 이력용 다국어 릴리스 노트는 `play_store/release_notes/v2.0.3.txt`에 있습니다.

---

## 8. 가격 및 배포 (Pricing & distribution)

| 필드 | 값 |
|---|---|
| 무료 / 유료 | **무료** |
| 배포 국가 | 전 세계 (Default: All countries) ⚠️ 사용자 결정 필요 |
| 인앱 결제 포함 | 아니오 |
| Designed for Families 프로그램 참여 | 아니오 |
| Android Wear / TV / Auto / Chromebook 호환 | 휴대전화 및 태블릿 (기본) |

---

## 9. 최초 등록 차단 요인 요약

| # | 항목 | 상태 | 책임 |
|---|---|---|---|
| 1 | Google Play Developer 계정 ($25 결제) | ⚠️ 사용자 확인 | **사용자** |
| 2 | 업로드 키스토어 생성 (비밀번호 결정 포함) | ❌ | **사용자** (보안상 자동화 부적절) |
| 3 | 앱 아이콘 512×512 PNG | ✅ | 준비 완료 |
| 4 | 피처 그래픽 1024×500 | ✅ | 준비 완료 |
| 5 | 폰 스크린샷 최소 2장 | ✅ | 준비 완료 |
| 6 | 개인정보처리방침 URL 공개 접근 가능 여부 | ✅ | GitHub Pages 연결 후 확인 대상 |
| 7 | README와 실제 빌드 기능 일치성 검증 | ✅ | 현재 v2.0.3 코드 기준으로 정렬 |
| 8 | Chrome MCP 확장 연결 (자동 폼 입력 시) | ⚠️ | 사용자 |
| 9 | 매니페스트 권한 정합성 (위치/카메라/인터넷 등) | ⚠️ | 사용자 |
| 10 | Play Console 폼 최종 제출 (계정 권한 필요) | - | **사용자** |
