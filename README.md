# trip-basket-new

Trip Basket 백엔드 서버 프로젝트입니다.

## Git hooks 설정

로컬에서 아래 명령어를 한 번 실행해 `.githooks`를 Git hooks 경로로 설정해주세요.

```bash
git config core.hooksPath .githooks
```

## 문서

프로젝트의 동작 방식과 API 규약은 아래 문서에서 관리합니다.

- [docs/error-handling.md](docs/error-handling.md): 예외 처리 아키텍처, 상태 코드 매핑, 로깅 정책
- [docs/api-conventions.md](docs/api-conventions.md): API 응답 규약, 에러 응답 구조(RFC 9457), 인증/경로 컨벤션