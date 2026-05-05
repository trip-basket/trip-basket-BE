# Trip Basket Changelog

API 및 서비스 변경 사항을 빠르게 확인할 수 있도록 관리하는 문서입니다.
- API 계약에 영향을 주는 변경이 생기면 이 문서와 Swagger를 함께 확인해주세요.
- 각 변경 항목에는 `프론트 영향: 낮음 | 보통 | 높음` 형식으로 영향도를 함께 적습니다.

## 2026-05-05

### 1. Block reaction 하위 API 추가

##### 프론트 영향: `보통`
- 블록 리액션도 블록 수정 API가 아니라 하위 API로 분리했습니다.
- 추가된 API:
  - `POST /api/rooms/{roomId}/blocks/{blockId}/reactions`
  - `DELETE /api/rooms/{roomId}/blocks/{blockId}/reactions/{reactionId}`
- 리액션 타입은 현재 `like`만 지원합니다.
- 동일한 블록에 대해 같은 멤버가 같은 타입의 리액션을 중복 생성할 수 없습니다.
- `GET /api/rooms/{roomId}/blocks/{blockId}` 응답의 `reactions`는 실제 저장된 값을 반환합니다.
- 블록이 삭제되면 연결된 `reaction`들도 서비스 레이어에서 먼저 hard delete 됩니다.

## 2026-04-14

### 1. Block todo 하위 API 추가

###### 프론트 영향: `높음`
- 블록 투두는 블록 수정 API가 아니라 하위 API로 분리했습니다.
- 추가된 API:
  - `POST /api/rooms/{roomId}/blocks/{blockId}/todos`
  - `PATCH /api/rooms/{roomId}/blocks/{blockId}/todos/{todoId}`
  - `DELETE /api/rooms/{roomId}/blocks/{blockId}/todos/{todoId}`
- `GET /api/rooms/{roomId}/blocks/{blockId}` 응답의 `todos`는 실제 저장된 값을 반환합니다.
- `todos`는 생성 순서(`createdAt asc`)로 정렬됩니다.
- 블록이 삭제되면 연결된 `todo`들도 함께 soft delete 됩니다.

## 2026-04-13

### 1. Block memo 구현

##### 프론트 영향: `보통`
- `PATCH /api/rooms/{roomId}/blocks/{blockId}` 에서 `memo`를 수정할 수 있습니다.
- `memo`를 요청 본문에서 보내지 않으면 기존 값을 유지합니다.
- `memo`를 명시적으로 `null`로 보내면 기존 메모가 삭제됩니다.
- `GET /api/rooms/{roomId}/blocks/{blockId}` 응답의 `memo`는 저장된 값을 반환합니다.

## 2026-04-12

### 1. Block 목록 조회 place 요약 필드 조정

##### 프론트 영향: `보통`
- `GET /api/rooms/{roomId}/blocks` 응답의 `place` 요약 정보에서 `lat`, `lng`를 제거했습니다.
- 대신 `category`를 추가했습니다.
- 목록 응답의 `place`는 이제 `placeId`, `placeName`, `category`를 포함합니다.
- `category`는 분류 정보가 없으면 `null`일 수 있습니다.

## 2026-04-09

### 1. Place 스키마 마이그레이션 체계 도입

#### 프론트 영향: `낮음`
- Flyway 기반 DB 마이그레이션을 도입했습니다.
- 초기 스키마를 `db/migration`으로 관리하도록 정리했습니다.
- 운영 환경의 JPA 스키마 변경 방식은 `ddl-auto: update`에서 검증 중심으로 전환했습니다.

### 2. Place 주소/사진 URL 컬럼 타입 확장

#### 프론트 영향: `낮음`
- `places.formatted_address`, `places.photo_url` 컬럼 타입을 `TEXT`로 변경했습니다.
- Google Place 연동 시 더 긴 주소 또는 사진 URL이 들어와도 DB 길이 제한에 걸릴 가능성을 줄였습니다.
- API 응답 필드 이름이나 응답 구조 자체는 변경되지 않습니다.

## 2026-04-08

### 1. Block 목록 조회 API 추가

###### 프론트 영향: `높음`
- `GET /api/rooms/{roomId}/blocks` API를 추가했습니다.
- 응답은 배열이 아니라 `{ "blocks": [...] }` 형태로 반환합니다.
- `status` 쿼리파라미터를 지원합니다.
  - 예: `?status=scheduled`, `?status=bucket`
- 필터가 없으면 전체 블록을 조회합니다.
- 각 항목은 목록 전용 응답 구조를 사용합니다.
- 목록 응답의 `place`는 최소 정보만 포함합니다.
  - `placeId` (`googlePlaceId`)
  - `placeName`
  - `lat`
  - `lng`
- 목록 응답에서는 `memo`, `todos`를 포함하지 않습니다.
- `reactions`는 현재 단계에서 빈 배열로 포함됩니다.
- 정렬 규칙은 다음과 같습니다.
  - `scheduled` 블록이 먼저 옵니다.
  - `scheduled` 내부는 `startTime asc`, 동률이면 `addedAt asc` 입니다.
  - `bucket` 블록은 뒤에 오며 `addedAt desc` 입니다.

### 2. Block 상세 조회 API 추가

###### 프론트 영향: `높음`
- `GET /api/rooms/{roomId}/blocks/{blockId}` API를 추가했습니다.
- 상세 조회 응답은 Block 생성 응답과 동일한 구조를 사용합니다.
- `place`에는 상세 정보가 포함됩니다.
  - `googlePlaceId`, `placeName`, `position`, `category`, `formattedAddress`, `rating`, `reviewCount`, `openingHours`, `priceLevel`, `photoUrl`
- `memo`는 현재 단계에서 `null`, `todos`, `reactions`는 빈 배열로 반환됩니다.

### 3. Block 수정 API 추가

###### 프론트 영향: `높음`
- `PATCH /api/rooms/{roomId}/blocks/{blockId}` API를 추가했습니다.
- 수정 가능한 필드는 `name`, `status`, `startTime`, `endTime` 입니다.
- 전송하지 않은 필드는 기존 값을 유지합니다.
- `bucket -> scheduled`, `scheduled -> bucket` 전환을 지원합니다.
- `place`는 수정할 수 없습니다.
- `bucket` 상태에서는 시간 값을 보낼 수 없습니다.

### 4. Block 삭제 API 추가

###### 프론트 영향: `높음`
- `DELETE /api/rooms/{roomId}/blocks/{blockId}` API를 추가했습니다.
- 삭제는 hard delete가 아니라 soft delete 입니다.
- 성공 시 `204 No Content`를 반환합니다.

## 2026-04-07

### 1. Block 생성 응답의 장소 좌표 구조 정리

##### 프론트 영향: `보통`
- `POST /api/rooms/{roomId}/blocks` 응답의 `place` 객체에서 좌표 필드를 `lat`, `lng` 평면 구조 대신 `position { lat, lng }` 구조로 변경했습니다.
- 이제 장소 상세 조회 응답과 동일한 좌표 표현을 사용합니다.

### 2. Place 관련 Swagger null 가능성 설명 보강

#### 프론트 영향: `낮음`
- `GET /api/places/{placeId}` 및 Block 생성 응답의 장소 정보 Swagger 설명을 보강했습니다.
- `priceLevel`, `rating`, `reviewCount`, `photoUrl`, `category`는 제공되지 않으면 `null`일 수 있음을 명시했습니다.
- `openingHours`는 `null`이 아니라, 정보가 없을 때 빈 배열(`[]`)로 반환된다는 점을 명시했습니다.

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
