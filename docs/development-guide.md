# Development Guide

이 문서는 이 저장소를 처음 클론한 사람이 가장 먼저 봐야 하는 개발 안내서입니다.

프로젝트 실행 준비, 빌드/포맷 규칙, 현재 코드베이스에서 지키고 있는 구조 규칙을 한곳에 정리합니다.

## 1. 시작하기

### 1.1 필수 환경

- Java 21
- Gradle Wrapper 사용 (`./gradlew`)
- Git

이 프로젝트는 [`build.gradle`](../build.gradle)에서 Java toolchain을 `21`로 고정하고 있습니다.

### 1.2 저장소 클론 후 바로 할 일

1. Git hooks 경로 설정

```bash
git config core.hooksPath .githooks
```

2. 테스트 실행으로 기본 상태 확인

```bash
./gradlew test
```

3. API 문서/정책 문서 확인

- [error-handling.md](./error-handling.md)
- [api-conventions.md](./api-conventions.md)

## 2. 빌드와 포맷 규칙

[`build.gradle`](../build.gradle) 기준으로 현재 프로젝트는 아래 규칙을 따릅니다.

### 2.1 핵심 플러그인

- `org.springframework.boot`
- `io.spring.dependency-management`
- `com.diffplug.spotless`

### 2.2 포맷터

Java 코드는 Spotless로 정리합니다.

- formatter: `palantirJavaFormat()`
- 사용하지 않는 import 제거: `removeUnusedImports()`
- import 순서:
  - `java`
  - `javax`
  - `org`
  - `com`
  - 빈 줄
  - 그 외 프로젝트 패키지
- line ending: `UNIX`
- 파일 끝 newline 유지

즉, 수동 스타일링보다 `spotless` 결과를 기준으로 맞추는 것이 우선입니다.

### 2.3 pre-commit hook

[`.githooks/pre-commit`](../.githooks/pre-commit) 에서 staged Java 파일이 있으면 아래를 자동 실행합니다.

1. `./gradlew spotlessApply`
2. 수정된 Java 파일 재-stage
3. `./gradlew spotlessCheck`

그래서 Java 코드를 수정할 때는 hook 설정을 해두는 것이 사실상 기본 전제입니다.

## 3. 주요 실행 명령

자주 쓰는 명령은 아래 정도로 정리하면 됩니다.

```bash
./gradlew test
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew bootRun
```

## 4. 현재 기술 스택

`build.gradle` 기준 주요 스택은 아래와 같습니다.

- Spring Boot 3.5.x
- Spring Web
- Spring Security
- Spring Validation
- Spring Data JPA
- OAuth2 Client
- JWT (`jjwt`)
- springdoc OpenAPI
- H2 / PostgreSQL
- Lombok

## 5. 코드 구조에서 지키는 규칙

이 섹션은 "코드가 돌아가면 된다" 수준이 아니라, 팀이 **읽기 쉬운 구조를 유지하기 위해** 따르는 규칙입니다.

### 5.1 메서드 배치 순서

기본 원칙:

- 필드
- public 진입점
- private helper

즉, 클래스의 핵심 역할을 먼저 읽고, 세부 구현은 아래에서 따라가는 흐름을 선호합니다.

예:

- [`SecurityConfig.java`](../src/main/java/dev/jino/tripbasketnew/security/SecurityConfig.java)
  - `securityFilterChain()`이 먼저 오고
  - `logUnauthorized()`, `logForbidden()`은 아래에 둡니다.

이 규칙은 절대 법칙은 아니지만, 현재 코드베이스에서는 기본값으로 생각합니다.

### 5.2 책임 분리

- Controller: HTTP 입출력 경계
- Service: 비즈니스 흐름/도메인 로직
- Client/Adapter: 외부 시스템 연동
- Config: 프레임워크/보안/문서 설정
- Exception layer: 에러 정책 중앙화

새 코드도 이 책임선을 유지하는 방향을 우선합니다.

### 5.3 예외 처리 규칙

상세 정책은 [error-handling.md](./error-handling.md)를 따르지만, 실무 규칙만 요약하면 아래와 같습니다.

- 서비스 레이어의 도메인성 오류는 `BusinessException` 사용
- 응답은 `ProblemDetail`로 표준화
- `debugInfo`, `cause`, stacktrace는 응답에 노출하지 않음
- `4xx`: 요약 로그
- `5xx`: stacktrace 포함 error 로그
- 비즈니스 오류 로그 prefix: `BIZ-*`
- 시스템/보안/프레임워크 오류 로그 prefix: `SYS-*`

### 5.4 Security 계층 규칙

- JWT 인증 실패/인가 실패도 가능한 한 API 에러 포맷과 일관되게 응답
- 필터 단계 예외는 `GlobalExceptionHandler`에 도달하지 않을 수 있으므로 Security 설정에서 별도 처리
- `401/403`은 `ProblemDetail` JSON으로 응답

### 5.5 문서화 규칙

- API 계약/응답 규약: [api-conventions.md](./api-conventions.md)
- 에러 정책/로깅 전략: [error-handling.md](./error-handling.md)
- 개발/구조/스타일 규칙: 이 문서

즉, 문서도 역할별로 나눠 유지합니다.

## 6. 새 코드를 추가할 때 체크할 것

1. 이 클래스의 책임이 분명한가
2. public 진입점이 먼저 보이도록 메서드 순서를 잡았는가
3. 예외가 `ProblemDetail` 정책과 충돌하지 않는가
4. 로그 prefix와 심각도가 기존 정책과 맞는가
5. Spotless 규칙을 통과하는가
6. OpenAPI 또는 관련 문서를 같이 갱신해야 하는가

## 7. 권장 작업 흐름

실무적으로는 아래 흐름을 권장합니다.

1. 관련 문서와 기존 코드를 먼저 확인
2. 책임 분리와 예외 처리 정책에 맞춰 구현
3. `./gradlew test` 실행
4. 필요 시 `./gradlew spotlessApply`
5. API 계약이 바뀌면 문서도 함께 수정

이 프로젝트는 기능 구현 자체만큼, **일관된 구조와 운영 가능한 로그/에러 정책**을 중요하게 봅니다.
