# Dizzycode

Discord를 참고하여 만든 실시간 채팅 서비스입니다.  
WebSocket 기반 실시간 메시징, 음성/화상 통화, 채널/룸 관리 기능을 제공합니다.

- **Backend**: https://github.com/Jiwoo-Hwang/dizzycode
- **Frontend**: https://github.com/DizzyCode2024/client
- **AI 추천 서버**: https://github.com/hbam1/Dizzycode_RecommendationServer

---

## 목차

1. [기술 스택](#기술-스택)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [주요 기능](#주요-기능)
4. [기술적 의사결정](#기술적-의사결정)
5. [성능 테스트](#성능-테스트)
6. [리팩토링](#리팩토링)
7. [실행 방법](#실행-방법)

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.3.0 |
| 인증 | Spring Security, JWT |
| 실시간 통신 | WebSocket, STOMP, RabbitMQ |
| 음성/화상 | OpenVidu 2.30.0 |
| Database | MySQL, MongoDB, Redis |
| 인프라 | Nginx, Docker, Docker Compose |
| API 문서화 | Swagger (springdoc-openapi) |
| AI 추천 | Flask (별도 서버) |

---

## 시스템 아키텍처

![아키텍처](img.png)

### 패키지 구조

기존 MVC Layered Architecture에서 Hexagonal Architecture 기반으로 개선했습니다.  
각 도메인은 독립적인 패키지로 분리되어 있으며, 서비스 계층은 인프라에 직접 의존하지 않습니다.

```
<domain>/
├── controller/       # REST 엔드포인트
├── domain/           # 순수 도메인 모델 + DTO
├── exception/        # 도메인별 예외
├── infrastructure/   # JPA/MongoDB 엔티티 + 리포지토리 구현체
└── service/
    ├── <Domain>Service.java
    └── port/         # 리포지토리 인터페이스 (infrastructure가 구현)
```

서비스 계층은 `port` 인터페이스에만 의존하기 때문에 DB 교체나 단위 테스트 시 인프라 구현체를 교체할 수 있습니다.

---

## 주요 기능

- JWT 기반 로그인/회원가입 (Spring Security + Redis 토큰 관리)
- WebSocket + STOMP 기반 실시간 채팅 (파일 첨부 지원, 최대 30MB)
- OpenVidu를 활용한 음성/화상 통화
- Redis 기반 실시간 유저 온라인 상태 확인
- 룸/카테고리/채널 계층 구조 관리
- 1:1 다이렉트 메시지 및 그룹 DM
- 친구 신청/수락/거절
- Flask AI 서버 연동 채널 추천
- Swagger UI를 통한 API 문서 제공 (`/swagger-ui.html`)

---

## 기술적 의사결정

### 1. RabbitMQ를 메시지 브로커로 선택한 이유

WebSocket은 서버와 클라이언트 사이의 연결 상태를 서버 메모리에 보관합니다.  
서버를 단일 인스턴스로만 운영하면 문제가 없지만, 인스턴스를 2개 이상으로 수평 확장하면 **서로 다른 서버에 연결된 클라이언트끼리 메시지를 주고받을 수 없는 문제**가 생깁니다.

이를 해결하기 위해 외부 메시지 브로커를 두었습니다. Redis Pub/Sub도 검토했지만, **STOMP 프로토콜을 기본 지원**하고 **메시지 지속성과 큐 관리** 측면에서 RabbitMQ를 선택했습니다.

```
클라이언트 → WebSocket/STOMP → Spring 서버 → RabbitMQ → 모든 구독자
```

Nginx는 `least_conn` 알고리즘으로 두 Spring 인스턴스(8081, 8082)에 요청을 분산합니다.

---

### 2. MySQL + MongoDB + Redis 멀티 DB 전략

단일 DB를 쓰지 않고 데이터 특성에 맞게 저장소를 분리했습니다.

| 저장소 | 저장 데이터 | 선택 이유 |
|---|---|---|
| MySQL | 회원, 룸, 채널, 친구 관계 | 관계형 데이터, 트랜잭션 보장 필요 |
| MongoDB | 채팅 메시지 | Append-only 특성, 대량 데이터의 수평 확장 용이 |
| Redis | 유저 온라인 상태 | TTL 기반 만료 처리, 빠른 읽기/쓰기가 필요한 임시 데이터 |

---

### 3. 단위 테스트를 위한 Fake Repository 패턴

Hexagonal Architecture 덕분에 서비스 계층 테스트 시 실제 DB 없이 인메모리 구현체(Fake Repository)를 주입할 수 있습니다.  
Mockito로 DB를 모킹하는 방식보다 실제 비즈니스 로직에 집중한 테스트를 작성할 수 있습니다.

```java
// 실제 JPA 리포지토리 대신 인메모리 구현체를 주입
MemberService memberService = new MemberService(new FakeMemberRepository(), passwordEncoder);
```

---

## 성능 테스트

### 1. RabbitMQ 메모리 설정에 따른 WebSocket 연결 성능

WebSocket + STOMP 연결 과정에서 RabbitMQ의 응답 지연이 전체 병목임을 확인했습니다.  
RabbitMQ에 할당하는 메모리에 따라 성능 차이를 측정했습니다.

![성능 테스트](img_1.png)

| 메모리 할당 | 평균 응답시간 | 표준편차 |
|---|---|---|
| 512MB | 3,826ms | 3,682ms |
| 1,024MB | 2,379ms | 1,894ms |

메모리를 2배로 늘렸을 때 평균 응답시간이 **약 1.6배**, 표준편차가 **약 1.9배** 개선됐습니다.

![성능 개선](img_2.png)

---

### 2. MongoDB 채팅 메시지 읽기 성능

메시지가 대량으로 쌓일 경우 읽기 속도 저하를 우려해 인덱스 도입을 검토했습니다.

![메시지 읽기](img_3.png)

| 데이터 수 | 평균 응답시간 |
|---|---|
| 10,000건 | 10ms |
| 100,000건 | 57ms |

10만 건 기준으로도 57ms로 허용 가능한 수준이라고 판단했습니다.  
인덱스는 쓰기 성능 저하와 저장 공간 트레이드오프가 있으므로, 유의미한 속도 저하가 확인될 때 도입하기로 결정했습니다.

---

## 리팩토링

초기 구현 이후 코드 품질 개선을 위해 아래 5가지 주제로 리팩토링을 진행했습니다.

### 1. 예외 처리 일관성 확보

**문제**
- 비즈니스 예외에 `ClassNotFoundException`(리플렉션 API용 checked exception)을 잘못 사용 → 클라이언트에 500 응답 반환
- `orElseThrow()` 호출 시 예외 메시지 없음 → 디버깅 불가
- 도메인마다 `@RestControllerAdvice`가 분산되어 있고, 일부 컨트롤러 범위 지정이 잘못됨

**개선**
- `FriendshipNotFoundException`, `ChannelNotFoundException`, `RoomMemberNotFoundException` 등 도메인 예외 클래스 신규 추가
- 모든 `orElseThrow()`에 명시적 예외와 메시지 추가
- 4개의 도메인별 핸들러 클래스를 단일 `GlobalExceptionHandler`로 통합

### 2. @Transactional 어노테이션 통일

`jakarta.transaction.Transactional`과 `org.springframework.transaction.annotation.Transactional`이 혼용되어 있던 문제를 Spring 어노테이션으로 통일했습니다.

### 3. DTO 매핑 보일러플레이트 제거

서비스 전반에 `new DTO() + setter` 패턴이 15곳 이상 반복되고 있었습니다.  
각 DTO에 정적 팩토리 메서드 `from()`을 추가하고 서비스에서 메서드 레퍼런스(`RoomDetailDTO::from`)로 교체했습니다.

### 4. 입력 값 검증 추가

`@RequestBody`로 받는 DTO에 Bean Validation(`@NotBlank`, `@Email`, `@Size` 등)을 추가하고, 컨트롤러에 `@Valid`를 적용했습니다.  
검증 실패 시 `GlobalExceptionHandler`에서 400 응답과 첫 번째 필드 에러 메시지를 반환합니다.

### 5. 하드코딩된 설정값 외부화

CORS 허용 origin(`http://localhost:5173`)과 RabbitMQ 자격증명(`guest/guest`)이 설정 클래스에 하드코딩되어 있었습니다.  
`application.properties`에 프로퍼티로 분리하고 `@Value`로 주입하도록 변경했습니다.

---

## 실행 방법

### 환경변수 설정

```bash
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/dizzycode?useSSL=false&useUnicode=true&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password
SPRING_DATA_MONGODB_URI=mongodb+srv://<user>:<password>@...
SPRING_DATA_REDIS_HOST=localhost
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
SPRING_JWT_SECRET=<최소 64자 이상의 시크릿>
OPENVIDU_URL=http://localhost:4443
OPENVIDU_SECRET=secret
FILE_UPLOAD_DIR=./uploads
CORS_ALLOWED_ORIGIN=http://localhost:5173
```

### 백엔드 실행

```bash
git clone https://github.com/Jiwoo-Hwang/dizzycode.git
docker-compose up -d
```

Docker Compose 실행 후 8081, 8082 포트에서 각각 Spring 인스턴스가 기동됩니다.  
API 문서: http://localhost:8081/swagger-ui.html

### 프론트엔드 실행

```bash
git clone https://github.com/DizzyCode2024/client.git
npm i
npm run dev
```

브라우저에서 http://localhost:5173 접속
