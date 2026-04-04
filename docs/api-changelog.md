# Trip Basket Changelog

API 및 서비스 변경 사항을 빠르게 확인할 수 있도록 관리하는 문서입니다.
- API 계약에 영향을 주는 변경이 생기면 이 문서와 Swagger를 함께 확인해주세요.
- 각 변경 항목에는 `프론트 영향: 낮음 | 보통 | 높음` 형식으로 영향도를 함께 적습니다.

## 2026-04-04

### 1. Place 저장 모델 추가

- 프론트 영향: `낮음`
- Google Place 상세 정보를 서버 DB에 저장할 수 있도록 `Place` 엔티티와 `PlaceOpeningHour` 값을 추가했습니다.
- `googlePlaceId`를 기준으로 장소를 식별하고, 이후 Block 생성 시 재사용할 수 있는 저장 구조를 마련했습니다.

### 2. Place 상세 응답 필드 확장

- 프론트 영향: `보통`
- `GET /api/places/{placeId}` 응답에 장소 상세 필드를 확장했습니다.
- 기존 `name`은 `placeName`으로, `address`는 `formattedAddress`로 정리했습니다.
- `rating`, `reviewCount`, `priceLevel`, `photoUrl`, `openingHours`를 함께 내려주도록 변경했습니다.

### 3. Google Place 동기화 로직 추가

- 프론트 영향: `낮음`
- `googlePlaceId`로 Google Places 상세를 조회한 뒤 서버 `Place` 데이터를 upsert하는 내부 서비스를 추가했습니다.
- 이 변경은 다음 단계의 `POST /api/rooms/{roomId}/blocks` 구현을 위한 선행 작업입니다.

### 4. Place 외부 연동 예외 처리 통일

- 프론트 영향: `보통`
- Google Places 연동 실패 시 `ResponseStatusException` 대신 프로젝트 공통 예외 체계(`BusinessException + ErrorCode`)를 사용하도록 변경했습니다.
- 이에 따라 Place 관련 오류도 다른 도메인 API와 동일한 ProblemDetail 응답 정책을 따릅니다.

## 2026-04-03

### 1. Changelog 페이지 추가

- 프론트 영향: `낮음`
- `/changelog` 페이지를 추가했습니다.
- Markdown 원본을 서버에서 읽어 렌더링하는 changelog 문서 구성을 도입했습니다.

### 2. REST API 인터페이스 분리

- 프론트 영향: `낮음`
- REST API 스펙을 각 `*Api` 인터페이스로 분리했습니다.
- 컨트롤러는 인터페이스 구현체로 동작하도록 구조를 정리했습니다.
