# Play Console release notes

Google Play Console의 "What's new in this release" 입력란에 붙여넣을 릴리즈 노트를 보관합니다.

GitHub Release 본문(`docs/releases/`)과는 형식 제약이 다르므로 별도 파일로 관리합니다.

## 파일명

- 태그명과 정확히 일치: `vX.Y.Z.txt`
- 예: `v2.0.1.txt`, `v2.1.0.txt`

## 형식

BCP-47 언어 태그로 언어별 블록을 감쌉니다.

```text
<ko-KR>
vX.Y.Z 한 줄 요약

새로 추가
• 변경 1
• 변경 2
</ko-KR>
<en-US>
vX.Y.Z short summary

What's new
• Change 1
• Change 2
</en-US>
```

## 제약

- 언어당 최대 500자 이내로 작성합니다.
- 마크다운과 HTML은 사용하지 않습니다.
- BCP-47 언어 태그(`<ko-KR>`, `<en-US>`)는 유지합니다.

## 업로드 방법

1. GitHub Release의 AAB 파일을 Play Console 릴리즈에 업로드합니다.
2. 이 파일 내용을 통째로 "What's new in this release"에 붙여넣습니다.
3. Play Console이 언어 태그를 기준으로 각 언어 노트를 분리하는지 확인합니다.

## 바탕화면 내보내기

로컬에 release AAB가 생성되어 있으면 다음 명령으로 AAB와 릴리즈 노트 txt를 바탕화면에 복사합니다.

```powershell
.\scripts\export-play-store-release.ps1 -Version X.Y.Z
```

결과 파일명:

```text
zephyr-sky-vX.Y.Z.aab
zephyr-sky-vX.Y.Z-release-notes.txt
```
