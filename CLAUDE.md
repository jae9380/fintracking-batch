# fintracking-batch

Spring Batch 월간 통계 집계 — Decorator 패턴

---

## 패턴: Decorator

```
BatchExecutor 인터페이스
  └── JobLauncherBatchExecutor (기본 실행)
        └── RetryBatchDecorator
              └── NotificationBatchDecorator
                    └── LoggingBatchDecorator (최외곽)
```

로깅/재시도/알림 기능을 독립적으로 추가/제거 가능.

---

## Spring Batch Job

### monthlyStatisticsJob

- **Step**: `monthlyStatisticsStep` (청크 크기 10)
- **ItemReader**: `UserItemReader` (`JpaPagingItemReader` 기반)
- **ItemProcessor**: `MonthlyStatisticsProcessor`
- **ItemWriter**: `MonthlyStatisticsWriter`

```bash
# JobParameter 형식
yearMonth: "2026-04"  # String "yyyy-MM"
```

---

## 설정

```yaml
spring.batch.job.enabled: false  # 스케줄러가 직접 트리거
```

---

## MSA 주의사항

현재 batch-service는 다른 서비스의 DB에 직접 접근하는 구조가 있을 수 있음.
MSA 원칙에 맞게 개선 필요:
- 직접 JPA 접근 대신 각 서비스 API 또는 Kafka 이벤트 기반으로 데이터 수집 권장
- 또는 CQRS 패턴으로 읽기 전용 집계 DB 분리 고려

---

## 패키지 구조

```
com.ft.batch
  ├── job/             — Job/Step 설정
  ├── reader/          — ItemReader 구현
  ├── processor/       — ItemProcessor 구현
  ├── writer/          — ItemWriter 구현
  └── executor/        — BatchExecutor + Decorator 체인
```
