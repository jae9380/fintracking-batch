# fintracking-batch

> Spring Batch 월간 통계 집계 · Decorator 패턴 · 스케줄러

---

## 사용 기술

| 분류        | 기술                               |
| ----------- | ---------------------------------- |
| 메시지      | Apache Kafka (Consumer + Producer) |
| 스케줄링    | Spring Scheduler (`@Scheduled`)    |
| 설정 관리   | Spring Cloud Config                |
| 서비스 등록 | Spring Cloud Eureka Client         |

---

## 핵심 설계 패턴 — Decorator

### 문제 상황

배치 실행 시 로깅, 실패 재시도, 완료 알림 등 부가 기능이 필요하다.
이 기능들을 핵심 실행 로직에 직접 넣으면 코드가 복잡해지고,
특정 기능만 빼거나 순서를 바꾸기 어렵다.

### 해결 — Decorator 패턴

각 부가 기능을 별도 Decorator로 분리하고, 핵심 실행자를 감쌉니다.

```
외부 호출
    │
    ▼
LoggingBatchDecorator           ← 실행 시작/종료 로그 기록
    │  위임
    ▼
NotificationBatchDecorator      ← 완료 시 Kafka로 리포트 이벤트 발행
    │  위임
    ▼
RetryBatchDecorator             ← 실패 시 최대 3회 재시도
    │  위임
    ▼
JobLauncherBatchExecutor        ← 실제 Spring Batch Job 실행 (핵심 로직)
```

**효과:**

- 재시도 기능만 끄고 싶으면 `RetryBatchDecorator`만 제거
- 새 기능 추가 시 새 Decorator 클래스만 만들어서 체인에 끼우면 됨
- 핵심 실행 로직(`JobLauncherBatchExecutor`)은 전혀 건드리지 않음

```java
// AbstractBatchDecorator — 추상 Decorator
public abstract class AbstractBatchDecorator implements BatchJobExecutor {
    protected final BatchJobExecutor delegate; // 감싸는 대상

    protected AbstractBatchDecorator(BatchJobExecutor delegate) {
        this.delegate = delegate;
    }
    // 각 Decorator가 execute()에서 delegate.execute()를 호출
}
```

---

## Spring Batch Job 구조

### monthlyStatisticsJob

```
monthlyStatisticsJob
    └── monthlyStatisticsStep (청크 크기: 10)
            │
            │ ItemReader (UserItemReader)
            │   → JpaPagingItemReader 기반
            │   → BatchUser 목록을 10명씩 읽기
            │
            │ ItemProcessor (MonthlyStatisticsProcessor)
            │   → BatchUser 1명 → List<MonthlyStatistics> 변환
            │   → 해당 사용자의 이번 달 거래 요약 계산
            │
            ▼ ItemWriter (MonthlyStatisticsWriter)
              → List<List<MonthlyStatistics>> 평탄화
              → 한꺼번에 DB 저장 (saveAll)
```

### 청크(Chunk) 처리란?

한 번에 모든 데이터를 메모리에 올리지 않고, 일정 크기씩 나누어 처리한다.
사용자가 10,000명이어도 10명씩 읽고 → 처리하고 → 저장하는 방식으로 메모리 효율이 좋다.

```
[전체 사용자 N명]

청크 1: 1~10번 사용자  → 읽기 → 처리 → 저장 → 커밋
청크 2: 11~20번 사용자 → 읽기 → 처리 → 저장 → 커밋
...
청크 N/10: (N-9)~N번  → 읽기 → 처리 → 저장 → 커밋

중간에 실패해도 이미 커밋된 청크는 재처리하지 않음 → 안전
```

---

## 실행 흐름 전체

```
[BatchScheduler]
  매월 말일 23:59:59
    │
    │  LoggingBatchDecorator.execute(yearMonth)
    ▼
[로그: "배치 시작 - 2026-05"]
    │
    │  NotificationBatchDecorator.execute(yearMonth)
    ▼
[RetryBatchDecorator.execute(yearMonth)]
    │  실패 시 최대 3회 재시도
    ▼
[JobLauncherBatchExecutor.execute(yearMonth)]
    │  Spring Batch JobLauncher.run(monthlyStatisticsJob)
    ▼
[monthlyStatisticsStep 실행]
    │  모든 사용자 10명씩 처리
    ▼
[완료]
    │
    ▼ (NotificationBatchDecorator에서)
[BatchReportEvent 발행]
    │  topic: batch.report
    ▼
[notification-service]
  월간 리포트 알림 발송
```

---

## Kafka 이벤트

### Consumer — UserRegisteredEvent

신규 사용자가 가입하면 배치 서비스도 그 사용자를 인식해야 한다.

```
[auth-service] 회원가입 완료
    │ topic: user.registered
    ▼
[UserRegisteredEventHandler]
    → BatchUser 테이블에 사용자 등록
    → 이후 배치 실행 시 이 사용자도 포함됨
```

### Consumer — TransactionSummaryEvent

거래 데이터를 자체 요약 테이블에 집계한다.

```
[transaction-service] 거래 생성
    │ topic: transaction.created
    ▼
[TransactionSummaryEventHandler]
    → BatchTransactionSummary 테이블에 누적
    → 배치 실행 시 이 요약 데이터 사용
```

### Producer — BatchReportEvent

```java
record BatchReportEvent(
    String eventId,
    Long userId,
    String yearMonth,   // "2026-05"
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    LocalDateTime generatedAt
)
```

---

## 패키지 구조

```
com.ft.batch
├── domain/
│   ├── BatchUser.java                  ← 배치 처리 대상 사용자
│   ├── BatchTransactionSummary.java    ← 거래 요약 집계 엔티티
│   └── MonthlyStatistics.java          ← 월간 통계 결과 엔티티
│
├── application/
│   ├── BatchJobExecutor.java               ← 실행 인터페이스 (Decorator 공통)
│   ├── JobLauncherBatchExecutor.java       ← 실제 Job 실행 구현체
│   ├── BatchReportEventPublisher.java      ← Kafka Producer
│   └── decorator/
│       ├── AbstractBatchDecorator.java     ← Decorator 추상 클래스
│       ├── LoggingBatchDecorator.java      ← 로깅 기능 추가
│       ├── RetryBatchDecorator.java        ← 재시도 기능 추가
│       ├── NotificationBatchDecorator.java ← 완료 알림 기능 추가
│       └── MetricBatchDecorator.java       ← 메트릭 기록 기능 추가
│
├── infrastructure/
│   ├── job/
│   │   ├── MonthlyStatisticsJobConfig.java  ← Job/Step Bean 설정
│   │   ├── UserItemReader.java              ← JpaPagingItemReader 구성
│   │   ├── MonthlyStatisticsProcessor.java  ← User → Statistics 변환
│   │   └── MonthlyStatisticsWriter.java     ← DB 저장
│   ├── scheduler/
│   │   ├── BatchScheduler.java              ← @Scheduled 매월 실행
│   │   └── BatchConfig.java                 ← Spring Batch 설정
│   ├── kafka/
│   │   ├── UserRegisteredEventHandler.java         ← Consumer
│   │   └── TransactionSummaryEventHandler.java     ← Consumer
│   └── persistence/                               ← JPA 구현체들
│
└── (presentation 없음 — 외부 API 없이 스케줄러로만 동작)
```

---

## 설정

```yaml
spring:
  batch:
    job:
      enabled: false # 자동 실행 비활성화 (스케줄러가 직접 트리거)
```

---

## 에러 코드

| 코드                         | HTTP | 설명                  |
| ---------------------------- | ---- | --------------------- |
| `BATCH_MAX_RETRY_EXCEEDED`   | 500  | 최대 재시도 횟수 초과 |
| `BATCH_JOB_EXECUTION_FAILED` | 500  | Job 실행 실패         |

---

## 테스트

```
test/
├── infrastructure/
│   ├── MonthlyStatisticsProcessorTest.java ← Processor 단위 테스트
│   └── kafka/
│       └── TransactionSummaryEventHandlerTest.java ← Consumer 테스트
└── kafka/
    └── KafkaTopicLogTest.java              ← 토픽 상수 검증
```
