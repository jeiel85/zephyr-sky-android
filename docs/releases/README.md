# Release notes

GitHub Release 본문을 손으로 작성해 보관하는 위치입니다.

## 흐름

1. 릴리즈 전에 `app/build.gradle.kts`의 `versionName`과 `versionCode`를 갱신합니다.
2. `CHANGELOG.md`의 최신 변경 사항을 `vX.Y.Z - YYYY-MM-DD` 섹션으로 승격합니다.
3. `docs/releases/vX.Y.Z.md` 파일을 작성합니다.
4. Play Store 업로드가 필요한 경우 `play_store/release_notes/vX.Y.Z.txt`도 함께 작성합니다.
5. 변경 사항을 커밋하고 `main`에 푸시합니다.
6. 태그를 생성하고 푸시합니다.

```bash
git tag -a vX.Y.Z -m "vX.Y.Z"
git push origin vX.Y.Z
```

태그 푸시 후 GitHub Actions가 Android 릴리즈 APK/AAB를 빌드하고, 태그명과 같은 이 파일을 Release 본문으로 사용합니다.

## 파일명 규칙

- 태그명과 정확히 일치해야 합니다.
- 예: `v2.0.1.md`, `v2.1.0.md`

## 형식

```markdown
## vX.Y.Z — 한 줄 부제

### 주요 변경 사항
- 

### 수정 사항
- 

### 문서 / 빌드 / 배포
- 

### 검증
- 로컬:
- CI:
- 산출물:

### 설치 또는 업데이트 참고 사항
- 
```

## 정책

- 빈 본문 또는 자동 생성 changelog만 있는 릴리즈를 만들지 않습니다.
- 사용자에게 의미 있는 변경 사항을 먼저 쓰고, 내부 빌드/문서 변경은 별도 섹션에 둡니다.
- 실제로 실행하지 않은 검증을 성공으로 기록하지 않습니다.
