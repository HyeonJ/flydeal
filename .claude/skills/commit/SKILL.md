---
name: commit
description: 변경사항 분석 → 커밋 → CHANGELOG.md 자동 업데이트
---

# Commit + CHANGELOG 업데이트

사용자가 `/commit` 또는 `/commit 메시지` 로 호출하면:

## 절차

1. `git status`와 `git diff --staged`로 변경사항 확인
2. 변경사항이 없으면 알림 후 종료
3. 커밋 메시지 생성:
   - 인자가 있으면 (`$ARGUMENTS`) 그대로 사용
   - 없으면 변경사항 분석해서 자동 생성
   - 형식: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:` 접두사
4. 관련 파일 스테이징 (git add)
5. 커밋 실행 (Co-Authored-By 포함)
6. **CHANGELOG.md 업데이트**:
   - `## [Unreleased]` 섹션에 변경사항 추가
   - 카테고리: Added, Changed, Fixed, Removed
   - Keep a Changelog 형식 준수
7. CHANGELOG 변경분도 별도 커밋: `docs: CHANGELOG.md 업데이트`

## CHANGELOG 형식

```markdown
## [Unreleased]

### Added
- 새로운 기능 설명

### Changed
- 변경된 기능 설명

### Fixed
- 수정된 버그 설명
```

## 주의사항
- .env, credentials 등 민감 파일은 절대 스테이징하지 말 것
- amend 금지, 항상 새 커밋
- Co-Authored-By: Claude <noreply@anthropic.com> 포함
