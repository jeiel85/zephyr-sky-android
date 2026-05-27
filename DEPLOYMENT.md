# Zephyr Sky 앱 배포 가이드 (DEPLOYMENT.md)

이 문서는 Kotlin/Jetpack Compose Android 네이티브 전환 이후의 빌드 및 배포 절차를 설명합니다.

---

## 1. 빌드 자동화 구조

GitHub Actions는 Gradle Kotlin DSL 기반 Android 빌드를 실행합니다.

### 트리거 조건
- **일반 푸시 또는 PR**: `./gradlew test`, `./gradlew assembleDebug`, 에뮬레이터 앱 실행 스모크 테스트
- **태그 푸시** (`vX.Y.Z`): `./gradlew test`, `./gradlew assembleRelease`, `./gradlew bundleRelease`, GitHub Release 생성

### 배포 흐름

```bash
git push origin main
git tag -a vX.Y.Z -m "vX.Y.Z"
git push origin vX.Y.Z
```

태그 푸시 후 GitHub Actions가 릴리즈 APK/AAB를 빌드하고 GitHub Releases에 업로드합니다. 태그명과 같은 `docs/releases/vX.Y.Z.md` 파일이 있으면 해당 파일을 GitHub Release 본문으로 사용합니다.

---

## 2. 로컬 빌드 및 테스트

Android SDK 경로가 설정되어 있어야 합니다. 로컬 환경에서는 `ANDROID_HOME`을 지정하거나 프로젝트 루트의 `local.properties`에 `sdk.dir`을 설정합니다.

```properties
sdk.dir=C:\\Users\\<USER>\\AppData\\Local\\Android\\Sdk
```

검증 명령:

```bash
./gradlew test
./gradlew assembleDebug
```

Windows PowerShell에서는 다음과 같이 실행할 수 있습니다.

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

---

## 3. 릴리즈 서명 설정

릴리즈 APK/AAB는 GitHub Secrets에 등록된 키스토어로 서명합니다. 키스토어 파일과 비밀번호는 저장소에 커밋하지 않습니다.

### GitHub Secrets 등록 항목

| Secret 이름 | 설명 |
|-------------|------|
| `RELEASE_KEYSTORE_BASE64` | 릴리즈 키스토어 파일 Base64 인코딩 값 |
| `RELEASE_STORE_PASSWORD` | 키스토어 비밀번호 |
| `RELEASE_KEY_ALIAS` | 키 별칭 |
| `RELEASE_KEY_PASSWORD` | 키 비밀번호 |

릴리즈 워크플로우는 `RELEASE_KEYSTORE_BASE64`를 `release.keystore`로 복원한 뒤, 다음 환경변수를 Gradle에 전달합니다.

| 환경변수 | 용도 |
|----------|------|
| `KEYSTORE_PATH` | 복원된 키스토어 파일 경로 |
| `RELEASE_STORE_PASSWORD` | 키스토어 비밀번호 |
| `RELEASE_KEY_ALIAS` | 키 별칭 |
| `RELEASE_KEY_PASSWORD` | 키 비밀번호 |

`app/build.gradle.kts`의 `release` signingConfig는 위 환경변수를 우선 사용하며, 하위 호환을 위해 `RELEASE_STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`도 인식합니다.

---

## 4. 버전 관리 체계

현재 앱 내부 버전은 `app/build.gradle.kts`에서 관리합니다.

```kotlin
versionCode = (findProperty("VERSION_CODE") as String?)?.toIntOrNull() ?: 201
versionName = (findProperty("VERSION_NAME") as String?) ?: "2.0.1"
```

릴리즈 워크플로우는 태그명에서 `VERSION_NAME`을 계산하고 `app/build.gradle.kts`의 `versionName`과 일치하는지 확인합니다. 불일치하면 릴리즈 빌드를 중단합니다.

예:

```bash
git tag v2.0.1
git push origin v2.0.1
```

위 태그는 CI에서 `VERSION_NAME=2.0.1`로 빌드됩니다.

---

## 5. 릴리즈 노트

릴리즈 전에 Nightseed Survivor와 같은 방식으로 수기 릴리즈 노트를 작성합니다.

| 파일 | 용도 |
|------|------|
| `docs/releases/vX.Y.Z.md` | GitHub Release 본문 |
| `play_store/release_notes/vX.Y.Z.txt` | Play Console 입력용 다국어 릴리즈 노트 |

`play_store/release_notes/` 파일은 `<ko-KR>`, `<en-US>` 언어 태그를 유지하고, 언어당 500자 이내로 작성합니다.

Nightseed Survivor와 같은 방식으로 Play Console 업로드용 파일을 바탕화면에 준비할 수 있습니다.

```powershell
.\scripts\export-play-store-release.ps1 -Version 2.0.1
```

이 스크립트는 최신 release AAB와 `play_store/release_notes/vX.Y.Z.txt`를 찾아 다음 이름으로 바탕화면에 복사합니다.

```text
zephyr-sky-vX.Y.Z.aab
zephyr-sky-vX.Y.Z-release-notes.txt
```

release AAB가 아직 없다면 먼저 릴리즈 빌드를 실행해야 합니다.

```powershell
.\gradlew.bat bundleRelease
```

---

## 6. 산출물 위치

로컬 및 CI 빌드 산출물은 Gradle Android 표준 경로에 생성됩니다.

| 산출물 | 경로 |
|--------|------|
| Debug APK | `app/build/outputs/apk/debug/*.apk` |
| Release APK | `app/build/outputs/apk/release/*.apk` |
| Release AAB | `app/build/outputs/bundle/release/*.aab` |

---

## 7. 현재 배포 상태

- **GitHub Actions**: Android Gradle 기반 CI/Release 워크플로우 사용
- **GitHub Release**: 태그 푸시로 APK/AAB 자동 업로드
- **Brand Page**: 운영 중 ([소개 페이지](https://jeiel85.github.io/zephyr-sky-android/))

---

## 8. 유지보수

- 배포 또는 CI 변경 후 `HISTORY.md`와 `CHANGELOG.md`를 갱신합니다.
- 실제로 실행하지 않은 로컬/CI 검증은 성공으로 기록하지 않습니다.
- 릴리즈 키스토어, 비밀번호, API 키 등 비밀정보는 GitHub Secrets 또는 로컬 비공개 설정으로만 관리합니다.
