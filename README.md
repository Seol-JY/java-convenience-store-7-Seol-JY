# 🛒 미션 - 편의점

구매자의 할인 혜택과 재고 상황을 고려하여 **최종 결제 금액을 계산하고 안내하는 결제 시스템**을 구현한다.

## ✨ 기능 구현 목록
### 1️⃣ 프로그램 초기화

---
### 파일 데이터 로드 및 검증 기능
+ [ ] products.md 파일에서 상품 정보 로드
    + [x] ⚠️ 파일이 존재하지 않는 경우 예외 발생
    + [x] ⚠️ 파일 형식이 올바르지 않은 경우 예외 발생
        + [x] 필수 컬럼(name, price, quantity, promotion) 존재 확인
          + [x] 정수 필드 검증 
        + [x] 가격이 0 이상의 정수인지 확인
        + [x] 수량이 0 이상의 정수인지 확인
        + [ ] (상품명, 프로모션) 조합의 중복 여부 검증

+ [x] promotions.md 파일에서 프로모션 정보 로드
    + [x] ⚠️ 파일이 존재하지 않는 경우 예외 발생
    + [x] ⚠️ 파일 형식이 올바르지 않은 경우 예외 발생
        + [x] 필수 컬럼(name, buy, get, start_date, end_date) 존재 확인
          + [x] 날짜 형식 필드 검증  
          + [x] 정수 필드 검증
        + [x] 구매수량(buy)이 1 이상의 정수인지 확인
        + [x] 증정수량(get)이 1 이상의 정수인지 확인
        + [x] start_date 이 end_date 이전 날짜인지 확인
        + [x] 중복된 프로모션명이 있는지 확인

---
### 2️⃣ 상품 주문 프로세스
### 상품 목록 출력 기능
+ [ ] 초기 메시지 출력
    + [ ] `안녕하세요. W편의점입니다.` 출력
    + [ ] "현재 보유하고 있는 상품입니다." 출력
+ [ ] 상품 목록 상세 출력
    + [ ] 각 상품 정보를 형식에 맞게 출력
        + [ ] 상품명과 가격 출력 (천 단위 콤마 포함)
        + [ ] 재고 수량 출력 ("재고 없음" 또는 "n개")
        + [ ] 프로모션이 있는 경우 프로모션명 출력

---
### 주문 입력 및 검증 기능
+ [ ] 안내 문구 출력
    + [ ] `구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])` 출력
+ [ ] 주문 형식 검증
    + [ ] ⚠️ 대괄호([]) 형식 검증
    + [ ] ⚠️ 하이픈(-) 구분자 검증
    + [ ] ⚠️ 쉼표(,) 구분자 검증
+ [ ] 주문 내용 검증
    + [ ] ⚠️ 상품 존재 여부 확인
    + [ ] ⚠️ 동일 상품 중복 주문 처리 (동일 상품이 두번 나오면 합산하여 처리)
    + [ ] ⚠️ 수량이 1 이상의 정수인지 확인
    + [ ] ⚠️ 재고 충분 여부 확인
---
### 3️⃣ 할인 및 프로모션 적용
### 프로모션 검증 및 적용 기능
+ [ ] 프로모션 유효성 검증
    + [ ] 현재 날짜와 프로모션 기간 비교
        + [ ] 시작일 이후, 종료일 이전인지 확인
    + [ ] 프로모션 재고 확인
        + [ ] 일반 재고와 프로모션 재고 구분 확인
    + [ ] 프로모션 종류별(1+1, 2+1 등) 처리 로직 구분

+ [ ] 프로모션 추가 구매 제안
    + [ ] 혜택 받을 수 있는 추가 수량 계산
    + [ ] 안내 메시지 출력
        + [ ] 추가 구매 시 받을 수 있는 혜택 상세 안내
    + [ ] 사용자 입력 처리 (Y/N)
        + [ ] ⚠️ Y/N 이외의 값 입력 시 예외 처리

+ [ ] 프로모션 재고 부족 처리
    + [ ] 부족한 수량 계산
    + [ ] 안내 메시지 출력
        + [ ] 정가 구매해야 하는 수량 안내
    + [ ] 사용자 입력 처리 (Y/N)
        + [ ] ⚠️ Y/N 이외의 값 입력 시 예외 처리
---
### 멤버십 할인 처리 기능
+ [ ] 멤버십 할인 적용 여부 확인
    + [ ] 안내 메시지 출력
    + [ ] 사용자 입력 처리 (Y/N)
        + [ ] ⚠️ Y/N 이외의 값 입력 시 예외 처리

+ [ ] 멤버십 할인 금액 계산
    + [ ] 프로모션 미적용 금액 계산
    + [ ] 30% 할인 금액 계산
    + [ ] 최대 한도(8,000원) 내로 적용
---
### 4️⃣ 결제 및 영수증 처리
### 영수증 출력 기능
+ [ ] 영수증 헤더 출력
    + [ ] 구분선과 매장명 출력 `==============W 편의점================`

+ [ ] 구매 상품 내역 출력
    + [ ] 상품명 좌측 정렬
    + [ ] 수량 우측 정렬
    + [ ] 금액 우측 정렬 (천 단위 콤마 포함)

+ [ ] 증정 상품 내역 출력
    + [ ] 증정 구분선 출력
    + [ ] 증정 상품이 없을 경우 "없음" 출력
    + [ ] 증정 상품명과 수량 출력

+ [ ] 금액 정보 출력
    + [ ] 구분선 출력
    + [ ] 총구매액 출력 (천 단위 콤마 포함)
    + [ ] 행사할인 출력 (마이너스 기호, 천 단위 콤마 포함)
    + [ ] 멤버십할인 출력 (마이너스 기호, 천 단위 콤마 포함)
    + [ ] 최종 결제 금액 출력 (천 단위 콤마 포함)
---
### 재고 관리 기능
+ [ ] 구매 확정 시 재고 차감
    + [ ] 프로모션 상품 재고 처리
        + [ ] 프로모션 재고 우선 차감
        + [ ] 부족 시 일반 재고 차감
    + [ ] 일반 상품 재고 차감
    + [ ] 증정 상품 재고 차감
---
### 5️⃣ 추가 구매 프로세스
### 추가 구매 처리 기능
+ [ ] 추가 구매 확인
    + [ ] 안내 메시지 출력
    + [ ] 사용자 입력 처리 (Y/N)
        + [ ] ⚠️ Y/N 이외의 값 입력 시 예외 처리

+ [ ] 추가 구매 분기 처리
    + [ ] Y 선택 시
        + [ ] 재고 상태 업데이트
        + [ ] 상품 목록 재출력
        + [ ] 주문 프로세스 재시작
    + [ ] N 선택 시
        + [ ] 프로세스 종료
---
### ☑️ 체크리스트
- [ ] main 브랜치에 구현하였는가
- [ ] 모든 예외는 `IllegalArgumentException`으로 처리하는가
- [ ] 모든 예외 발생 시 `[ERROR]` 접두어를 포함한 메시지 출력 후 재입력을 받는가
- [ ] 모든 금액이 `###,###원` 형식으로 출력되는지 확인
- [ ] 할인 금액에 `-` 기호가 정확히 표시되는지 확인
- [ ] 프로모션 및 재고가 정확히 연동되어 관리되는지 확인
- [ ] 모든 출력 문구가 요구사항과 정확히 일치하는지 확인
- [ ] 모든 입력값 검증이 누락없이 구현되었는지 확인

### 💡 프로그램 실행 흐름
1. 프로그램 시작 및 데이터 초기화
    - 파일 데이터 로드 및 검증
    - 초기 상태 설정
2. 상품 목록 출력
    - 환영 메시지 출력
    - 현재 재고 상태 출력
3. 주문 입력 및 검증
    - 주문 형식 검증
    - 재고 검증
4. 프로모션 및 멤버십 할인 적용
    - 프로모션 적용 가능 여부 확인
    - 추가 구매 제안
    - 멤버십 할인 처리
5. 영수증 출력 및 재고 처리
    - 구매 내역 출력
    - 할인 내역 출력
    - 재고 차감
6. 추가 구매 여부 확인
    - Y: 2번으로 돌아가기
    - N: 프로그램 종료