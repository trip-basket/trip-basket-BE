# Trip Basket Changelog

API 및 서비스 변경 사항을 빠르게 확인할 수 있도록 관리하는 문서입니다.
- API 계약에 영향을 주는 변경이 생기면 이 문서와 Swagger를 함께 확인해주세요.
- 각 변경 항목에는 `프론트 영향: 낮음 | 보통 | 높음` 형식으로 영향도를 함께 적습니다.

## 2026-04-05

### 1. Block 생성 API 추가

###### 프론트 영향: `높음`
- `POST /api/rooms/{roomId}/blocks` API를 추가했습니다.
- 요청 본문은 `status`, `googlePlaceId`, `name`, `startTime`, `endTime`을 사용합니다.
- `status`는 소문자 `bucket | scheduled`로 고정됩니다.
- `scheduled`일 때만 `startTime`, `endTime`이 필요하고, `bucket`일 때는 두 값이 없어야 합니다.

### 2. Block 응답 구조 추가

###### 프론트 영향: `높음`
- Block 생성 응답에 `id`, `roomId`, `status`, `place`, `name`, `startTime`, `endTime`, `timezoneId`, `startUtcOffsetMinutes`, `endUtcOffsetMinutes`, `addedBy`, `addedAt`를 포함하도록 했습니다.
- `memo`, `cost`는 현재 단계에서는 `null`로, `reactions`, `todos`는 빈 배열로 내려갑니다.
- `place` 객체에는 `googlePlaceId`, `placeName`, `lat`, `lng`, `category`, `formattedAddress`, `rating`, `reviewCount`, `openingHours`, `priceLevel`, `photoUrl`가 포함됩니다.

### 3. Block 시간 저장/응답 정책 변경

###### 프론트 영향: `높음`
- Block 요청의 `startTime`, `endTime`은 이제 offset 포함 시간이 아니라 장소 현지 기준 `LocalDateTime`으로 받습니다.
- 서버는 장소 시간대 기준 로컬 시간을 UTC로 변환해 저장하고, 응답에서는 다시 블록의 `timezoneId` 기준 로컬 시간으로 변환해 내려줍니다.
- 응답에 `startUtcOffsetMinutes`, `endUtcOffsetMinutes`를 추가했습니다. 써머타임 전환일에는 두 값이 다를 수 있습니다.

### 4. Place 시간대 저장 로직 추가

##### 프론트 영향: `보통`
- `Place`에 `timezoneId` 저장 필드를 추가했습니다.
- Block 생성 시 `googlePlaceId -> lat/lng -> Google Timezone API -> timezoneId` 흐름으로 장소 시간대를 식별합니다.
- 이미 저장된 `Place`에 `timezoneId`가 있으면 재호출하지 않고 재사용합니다.

### 5. Block 도메인 및 입력 검증 추가

##### 프론트 영향: `보통`
- `Block` 엔티티와 `BlockStatus`를 추가했습니다.
- `scheduled`면 시간 필수, `bucket`이면 시간 금지, `endTime > startTime` 규칙을 적용했습니다.
- DTO 레벨에서도 동일한 조합 검증이 동작하도록 요청 검증을 추가했습니다.

### 6. Google 연동 테스트 보강

#### 프론트 영향: `낮음`
- `PlaceClient`의 mock 기반 HTTP 테스트를 추가해 Place Detail 및 Timezone API 응답 파싱/예외 매핑을 검증합니다.
- 실제 Google API를 호출하는 라이브 통합 테스트도 추가했습니다.
- 라이브 테스트는 `./gradlew placeClientLiveTest`로 별도 실행하며, 현재는 외부 환경변수로 주입된 값만 사용합니다.

## 2026-04-04

### 1. Place 저장 모델 추가

#### 프론트 영향: `낮음`
- Google Place 상세 정보를 서버 DB에 저장할 수 있도록 `Place` 엔티티와 `PlaceOpeningHour` 값을 추가했습니다.
- `googlePlaceId`를 기준으로 장소를 식별하고, 이후 Block 생성 시 재사용할 수 있는 저장 구조를 마련했습니다.

### 2. Place 상세 응답 필드 확장

##### 프론트 영향: `보통`
- `GET /api/places/{placeId}` 응답에 장소 상세 필드를 확장했습니다.
- 기존 `name`은 `placeName`으로, `address`는 `formattedAddress`로 정리했습니다.
- `rating`, `reviewCount`, `priceLevel`, `photoUrl`, `openingHours`를 함께 내려주도록 변경했습니다.

### 3. Google Place 동기화 로직 추가

#### 프론트 영향: `낮음`
- `googlePlaceId`로 Google Places 상세를 조회한 뒤 서버 `Place` 데이터를 upsert하는 내부 서비스를 추가했습니다.
- 이 변경은 다음 단계의 `POST /api/rooms/{roomId}/blocks` 구현을 위한 선행 작업입니다.

### 4. Place 외부 연동 예외 처리 통일

##### 프론트 영향: `보통`
- Google Places 연동 실패 시 `ResponseStatusException` 대신 프로젝트 공통 예외 체계(`BusinessException + ErrorCode`)를 사용하도록 변경했습니다.
- 이에 따라 Place 관련 오류도 다른 도메인 API와 동일한 ProblemDetail 응답 정책을 따릅니다.

## 2026-04-03

### 1. Changelog 페이지 추가

#### 프론트 영향: `낮음`
- `/changelog` 페이지를 추가했습니다.
- Markdown 원본을 서버에서 읽어 렌더링하는 changelog 문서 구성을 도입했습니다.

### 2. REST API 인터페이스 분리

#### 프론트 영향: `낮음`
- REST API 스펙을 각 `*Api` 인터페이스로 분리했습니다.
- 컨트롤러는 인터페이스 구현체로 동작하도록 구조를 정리했습니다.
