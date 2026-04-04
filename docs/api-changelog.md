# Trip Basket Changelog

API 및 서비스 변경 사항을 빠르게 확인할 수 있도록 관리하는 문서입니다.

## 2026-04-03

### Added

- `/changelog` 페이지를 추가했습니다.
- Markdown 원본을 서버에서 읽어 렌더링하는 changelog 문서 구성을 도입했습니다.

### Changed

- REST API 스펙을 각 `*Api` 인터페이스로 분리했습니다.
- 컨트롤러는 인터페이스 구현체로 동작하도록 구조를 정리했습니다.

### Frontend Notes

- API 계약에 영향을 주는 변경이 생기면 이 문서와 Swagger를 함께 확인해주세요.
