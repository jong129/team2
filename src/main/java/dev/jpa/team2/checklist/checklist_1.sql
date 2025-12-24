/* =========================================================
 * CHECKLIST_TEMPLATE
 * 체크리스트 템플릿 테이블
 *
 * - 사전(PRE) / 사후(POST) 체크리스트 구분
 * - 체크리스트의 "문서 단위 틀"
 * - 버전 관리 및 활성 여부 관리
 * ========================================================= */


------------------------------------------------------------
-- 1) TEMPLATE PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_CHECKLIST_TEMPLATE_ID
  START WITH 1       -- 시작 값
  INCREMENT BY 1     -- 증가 폭
  NOCACHE            -- 캐시 사용 안 함(개발/학습용)
  NOCYCLE;           -- 반복 없음


------------------------------------------------------------
-- 2) CHECKLIST_TEMPLATE 테이블 생성
------------------------------------------------------------

CREATE TABLE CHECKLIST_TEMPLATE (
  TEMPLATE_ID     NUMBER        NOT NULL,        -- PK
  TEMPLATE_TYPE   VARCHAR2(10)  NOT NULL,        -- 템플릿 타입(PRE / POST)
  TEMPLATE_NAME   VARCHAR2(100) NOT NULL,        -- 템플릿 이름
  VERSION_NO      NUMBER        DEFAULT 1 NOT NULL, -- 템플릿 버전
  DESCRIPTION     VARCHAR2(500),                 -- 템플릿 설명
  IS_ACTIVE_YN    CHAR(1)       DEFAULT 'Y' NOT NULL, -- 활성 여부
  CREATED_AT      DATE          DEFAULT SYSDATE NOT NULL, -- 생성일
  UPDATED_AT      DATE          DEFAULT SYSDATE NOT NULL, -- 수정일

  -- PK
  CONSTRAINT PK_CHECKLIST_TEMPLATE PRIMARY KEY (TEMPLATE_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_TEMPLATE_TYPE CHECK (TEMPLATE_TYPE IN ('PRE','POST')),
  CONSTRAINT CK_TEMPLATE_ACTIVE CHECK (IS_ACTIVE_YN IN ('Y','N'))
);


------------------------------------------------------------
-- 3) 중복 방지 제약
--    같은 이름 + 같은 버전의 템플릿 중복 생성 방지
------------------------------------------------------------

ALTER TABLE CHECKLIST_TEMPLATE
  ADD CONSTRAINT UQ_TEMPLATE_NAME_VER
  UNIQUE (TEMPLATE_NAME, VERSION_NO);


------------------------------------------------------------
-- 4) COMMENT
------------------------------------------------------------

COMMENT ON TABLE CHECKLIST_TEMPLATE IS '사전(PRE)/사후(POST) 체크리스트 템플릿(문서 단위)';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.TEMPLATE_ID IS '템플릿 ID(PK). SEQ_CHECKLIST_TEMPLATE_ID 사용';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.TEMPLATE_TYPE IS '템플릿 타입(PRE=계약 전, POST=계약 후)';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.TEMPLATE_NAME IS '템플릿 이름';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.VERSION_NO IS '템플릿 버전 번호';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.DESCRIPTION IS '템플릿 목적 및 설명';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.IS_ACTIVE_YN IS '활성 여부(Y/N)';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.CREATED_AT IS '생성일';
COMMENT ON COLUMN CHECKLIST_TEMPLATE.UPDATED_AT IS '수정일';



/* =========================================================
 * CHECKLIST_ITEM
 * 체크리스트 항목 마스터 테이블
 *
 * - 체크리스트를 구성하는 "개별 질문/확인 항목"
 * - 여러 템플릿에서 재사용 가능
 * - 기본 위험도 및 기본 필수 여부 정의
 * ========================================================= */


------------------------------------------------------------
-- 5) ITEM PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_CHECKLIST_ITEM_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 6) CHECKLIST_ITEM 테이블 생성
------------------------------------------------------------

CREATE TABLE CHECKLIST_ITEM (
  ITEM_ID             NUMBER        NOT NULL,           -- PK
  ITEM_TITLE          VARCHAR2(200) NOT NULL,           -- 항목 제목
  ITEM_DESCRIPTION    VARCHAR2(1000),                   -- 항목 설명/가이드
  DEFAULT_REQUIRED_YN CHAR(1)       DEFAULT 'N' NOT NULL, -- 기본 필수 여부
  DEFAULT_RISK_LEVEL  VARCHAR2(10)  DEFAULT 'LOW' NOT NULL, -- 기본 위험도
  CREATED_AT          DATE          DEFAULT SYSDATE NOT NULL, -- 생성일
  UPDATED_AT          DATE          DEFAULT SYSDATE NOT NULL, -- 수정일

  -- PK
  CONSTRAINT PK_CHECKLIST_ITEM PRIMARY KEY (ITEM_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_ITEM_REQUIRED CHECK (DEFAULT_REQUIRED_YN IN ('Y','N')),
  CONSTRAINT CK_ITEM_RISK CHECK (DEFAULT_RISK_LEVEL IN ('LOW','MEDIUM','HIGH'))
);


------------------------------------------------------------
-- 7) COMMENT
------------------------------------------------------------

COMMENT ON TABLE CHECKLIST_ITEM IS '체크리스트 항목 마스터(질문/확인사항 단위)';
COMMENT ON COLUMN CHECKLIST_ITEM.ITEM_ID IS '항목 ID(PK). SEQ_CHECKLIST_ITEM_ID 사용';
COMMENT ON COLUMN CHECKLIST_ITEM.ITEM_TITLE IS '체크리스트 항목 제목';
COMMENT ON COLUMN CHECKLIST_ITEM.ITEM_DESCRIPTION IS '항목 설명 및 안내 문구';
COMMENT ON COLUMN CHECKLIST_ITEM.DEFAULT_REQUIRED_YN IS '기본 필수 여부(Y/N)';
COMMENT ON COLUMN CHECKLIST_ITEM.DEFAULT_RISK_LEVEL IS '기본 위험도(LOW/MEDIUM/HIGH)';
COMMENT ON COLUMN CHECKLIST_ITEM.CREATED_AT IS '생성일';
COMMENT ON COLUMN CHECKLIST_ITEM.UPDATED_AT IS '수정일';


/* =========================================================
 * TEMPLATE_ITEM
 * 체크리스트 템플릿과 체크리스트 항목을 연결하는 매핑 테이블
 *
 * - 하나의 템플릿(CHECKLIST_TEMPLATE)에
 *   여러 항목(CHECKLIST_ITEM)을 연결
 * - 항목의 노출 순서, 필수 여부, 위험도 덮어쓰기 설정 가능
 *
 * 이 테이블이 실제 "체크리스트 양식" 역할을 함
 * ========================================================= */


-- 템플릿-항목 매핑 PK용 시퀀스
CREATE SEQUENCE SEQ_TEMPLATE_ITEM_ID
  START WITH 1       -- 시작 값
  INCREMENT BY 1     -- 증가 폭
  NOCACHE            -- 캐시 사용 안 함
  NOCYCLE;           -- 반복 없음


CREATE TABLE TEMPLATE_ITEM (
  TEMPLATE_ITEM_ID     NUMBER        NOT NULL, -- PK
  TEMPLATE_ID          NUMBER        NOT NULL, -- 템플릿 ID (FK)
  ITEM_ID              NUMBER        NOT NULL, -- 체크리스트 항목 ID (FK)

  ITEM_ORDER           NUMBER        NOT NULL, -- 체크리스트에서의 표시 순서
  REQUIRED_YN          CHAR(1)       DEFAULT 'N' NOT NULL, -- 해당 템플릿에서 필수 여부
  RISK_LEVEL_OVERRIDE  VARCHAR2(10), -- 템플릿 기준 위험도 덮어쓰기(없으면 ITEM 기본값 사용)

  CREATED_AT           DATE          DEFAULT SYSDATE NOT NULL, -- 생성일
  UPDATED_AT           DATE          DEFAULT SYSDATE NOT NULL, -- 수정일

  -- PK
  CONSTRAINT PK_TEMPLATE_ITEM PRIMARY KEY (TEMPLATE_ITEM_ID),

  -- FK
  CONSTRAINT FK_TEMPLATE_ITEM_TEMPLATE
    FOREIGN KEY (TEMPLATE_ID)
    REFERENCES CHECKLIST_TEMPLATE (TEMPLATE_ID),

  CONSTRAINT FK_TEMPLATE_ITEM_ITEM
    FOREIGN KEY (ITEM_ID)
    REFERENCES CHECKLIST_ITEM (ITEM_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_TEMPLATE_ITEM_REQUIRED CHECK (REQUIRED_YN IN ('Y','N')),
  CONSTRAINT CK_TEMPLATE_ITEM_RISK CHECK (
    RISK_LEVEL_OVERRIDE IS NULL
    OR RISK_LEVEL_OVERRIDE IN ('LOW','MEDIUM','HIGH')
  )
);


-- 하나의 템플릿에 같은 항목이 중복으로 들어가는 것 방지
ALTER TABLE TEMPLATE_ITEM
  ADD CONSTRAINT UQ_TEMPLATE_ITEM
  UNIQUE (TEMPLATE_ID, ITEM_ID);


-- =========================
-- COMMENT 
-- =========================

COMMENT ON TABLE TEMPLATE_ITEM IS '체크리스트 템플릿과 항목을 연결하는 매핑 테이블(실제 체크리스트 양식 구성)';
COMMENT ON COLUMN TEMPLATE_ITEM.TEMPLATE_ITEM_ID IS '템플릿-항목 매핑 ID(PK)';
COMMENT ON COLUMN TEMPLATE_ITEM.TEMPLATE_ID IS '체크리스트 템플릿 ID(FK)';
COMMENT ON COLUMN TEMPLATE_ITEM.ITEM_ID IS '체크리스트 항목 ID(FK)';
COMMENT ON COLUMN TEMPLATE_ITEM.ITEM_ORDER IS '체크리스트 내 항목 노출 순서';
COMMENT ON COLUMN TEMPLATE_ITEM.REQUIRED_YN IS '해당 템플릿에서의 필수 여부(Y/N)';
COMMENT ON COLUMN TEMPLATE_ITEM.RISK_LEVEL_OVERRIDE IS '템플릿 기준 위험도 덮어쓰기(없으면 ITEM 기본 위험도 사용)';
COMMENT ON COLUMN TEMPLATE_ITEM.CREATED_AT IS '생성일';
COMMENT ON COLUMN TEMPLATE_ITEM.UPDATED_AT IS '수정일';

/* =========================================================
 * USER_SCENARIO
 * 사용자 환경(주거 환경/부동산 경험) 입력 테이블
 *
 * - 사용자의 "프로필/상황" 데이터를 저장
 * - 체크리스트 개인화(맞춤형 항목/위험도)에 사용
 * - AI 챗봇이 사용자 상황을 이해하는 기반 데이터
 * ========================================================= */


------------------------------------------------------------
-- 1) USER_SCENARIO PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_USER_SCENARIO_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 2) USER_SCENARIO 테이블 생성
------------------------------------------------------------

CREATE TABLE USER_SCENARIO (
  SCENARIO_ID        NUMBER        NOT NULL, -- PK
  MEMBER_ID          NUMBER        NOT NULL, -- 사용자 ID (FK - MEMBER)

  -- 아래 컬럼들은 "예시" 형태의 기본 프로필 컬럼 (추후 확장 가능)
  EXPERIENCE_LEVEL   VARCHAR2(20)  DEFAULT 'BEGINNER' NOT NULL, -- BEGINNER/INTERMEDIATE/EXPERT
  HOUSING_TYPE       VARCHAR2(30), -- 오피스텔/빌라/아파트/원룸 등
  HAS_PET_YN         CHAR(1)       DEFAULT 'N' NOT NULL, -- 반려동물 여부
  HAS_PARKING_YN     CHAR(1)       DEFAULT 'N' NOT NULL, -- 주차 필요 여부
  REGION_TEXT        VARCHAR2(200), -- 희망 지역(자유 텍스트)

  CREATED_AT         DATE          DEFAULT SYSDATE NOT NULL,
  UPDATED_AT         DATE          DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_USER_SCENARIO PRIMARY KEY (SCENARIO_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_SCENARIO_PET CHECK (HAS_PET_YN IN ('Y','N')),
  CONSTRAINT CK_SCENARIO_PARK CHECK (HAS_PARKING_YN IN ('Y','N')),
  CONSTRAINT CK_SCENARIO_EXP CHECK (EXPERIENCE_LEVEL IN ('BEGINNER','INTERMEDIATE','EXPERT'))

  -- FK (MEMBER 테이블이 이미 있을 때 활성화)
  -- ,CONSTRAINT FK_USER_SCENARIO_MEMBER
  --   FOREIGN KEY (MEMBER_ID)
  --   REFERENCES MEMBER (MEMBER_ID)
);


------------------------------------------------------------
-- 3) COMMENT
------------------------------------------------------------

COMMENT ON TABLE USER_SCENARIO IS '사용자 주거 환경/부동산 경험(프로필) 저장. 체크리스트 개인화에 사용';
COMMENT ON COLUMN USER_SCENARIO.SCENARIO_ID IS '사용자 시나리오 ID(PK). SEQ_USER_SCENARIO_ID 사용';
COMMENT ON COLUMN USER_SCENARIO.MEMBER_ID IS '회원 ID(FK - MEMBER)';
COMMENT ON COLUMN USER_SCENARIO.EXPERIENCE_LEVEL IS '부동산 경험 수준(BEGINNER/INTERMEDIATE/EXPERT)';
COMMENT ON COLUMN USER_SCENARIO.HOUSING_TYPE IS '주거 형태(아파트/빌라/오피스텔/원룸 등)';
COMMENT ON COLUMN USER_SCENARIO.HAS_PET_YN IS '반려동물 여부(Y/N)';
COMMENT ON COLUMN USER_SCENARIO.HAS_PARKING_YN IS '주차 필요 여부(Y/N)';
COMMENT ON COLUMN USER_SCENARIO.REGION_TEXT IS '희망 지역(텍스트)';
COMMENT ON COLUMN USER_SCENARIO.CREATED_AT IS '생성일';
COMMENT ON COLUMN USER_SCENARIO.UPDATED_AT IS '수정일';




/* =========================================================
 * USER_CHECKLIST
 * 사용자에게 "생성된" 체크리스트(사전/사후) 헤더 테이블
 *
 * - 사용자가 선택한 계약 단계(PRE/POST) 템플릿 기반으로 생성
 * - 어떤 템플릿으로 만들었는지, 어떤 사용자 시나리오로 만들었는지 기록
 * - 실제 항목/답변은 다음 테이블(USER_CHECKLIST_ITEM / ANSWER)에 저장
 * ========================================================= */


------------------------------------------------------------
-- 4) USER_CHECKLIST PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_USER_CHECKLIST_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 5) USER_CHECKLIST 테이블 생성
------------------------------------------------------------

CREATE TABLE USER_CHECKLIST (
  USER_CHECKLIST_ID  NUMBER        NOT NULL, -- PK
  MEMBER_ID          NUMBER        NOT NULL, -- 사용자 ID (FK - MEMBER)
  SCENARIO_ID        NUMBER,                 -- 사용자 시나리오 ID (FK - USER_SCENARIO)
  TEMPLATE_ID        NUMBER        NOT NULL, -- 사용한 템플릿 ID (FK - CHECKLIST_TEMPLATE)

  CHECKLIST_STAGE    VARCHAR2(10)  NOT NULL, -- 'PRE' 또는 'POST' (조회/분석 편의)
  STATUS             VARCHAR2(20)  DEFAULT 'IN_PROGRESS' NOT NULL, -- IN_PROGRESS/COMPLETED
  CREATED_AT         DATE          DEFAULT SYSDATE NOT NULL,
  UPDATED_AT         DATE          DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_USER_CHECKLIST PRIMARY KEY (USER_CHECKLIST_ID),

  -- FK
  CONSTRAINT FK_USER_CHECKLIST_TEMPLATE
    FOREIGN KEY (TEMPLATE_ID)
    REFERENCES CHECKLIST_TEMPLATE (TEMPLATE_ID),

  CONSTRAINT FK_USER_CHECKLIST_SCENARIO
    FOREIGN KEY (SCENARIO_ID)
    REFERENCES USER_SCENARIO (SCENARIO_ID),

  -- 값 제한
  CONSTRAINT CK_USER_CHECKLIST_STAGE CHECK (CHECKLIST_STAGE IN ('PRE','POST')),
  CONSTRAINT CK_USER_CHECKLIST_STATUS CHECK (STATUS IN ('IN_PROGRESS','COMPLETED'))

  -- FK (MEMBER 테이블이 이미 있을 때 활성화)
  -- ,CONSTRAINT FK_USER_CHECKLIST_MEMBER
  --   FOREIGN KEY (MEMBER_ID)
  --   REFERENCES MEMBER (MEMBER_ID)
);


------------------------------------------------------------
-- 6) COMMENT
------------------------------------------------------------

COMMENT ON TABLE USER_CHECKLIST IS '사용자에게 생성된 체크리스트(사전/사후) 헤더. 템플릿/시나리오 기반';
COMMENT ON COLUMN USER_CHECKLIST.USER_CHECKLIST_ID IS '사용자 체크리스트 ID(PK). SEQ_USER_CHECKLIST_ID 사용';
COMMENT ON COLUMN USER_CHECKLIST.MEMBER_ID IS '회원 ID(FK - MEMBER)';
COMMENT ON COLUMN USER_CHECKLIST.SCENARIO_ID IS '사용자 시나리오 ID(FK - USER_SCENARIO)';
COMMENT ON COLUMN USER_CHECKLIST.TEMPLATE_ID IS '사용한 템플릿 ID(FK - CHECKLIST_TEMPLATE)';
COMMENT ON COLUMN USER_CHECKLIST.CHECKLIST_STAGE IS '체크리스트 단계(PRE/POST). 조회/분석 편의';
COMMENT ON COLUMN USER_CHECKLIST.STATUS IS '진행 상태(IN_PROGRESS/COMPLETED)';
COMMENT ON COLUMN USER_CHECKLIST.CREATED_AT IS '생성일';
COMMENT ON COLUMN USER_CHECKLIST.UPDATED_AT IS '수정일';

/* =========================================================
 * USER_CHECKLIST_ITEM
 * 사용자 체크리스트에 포함된 "항목 목록" 테이블
 *
 * - USER_CHECKLIST(헤더) 기준으로 실제 포함된 항목들을 저장
 * - 어떤 항목이 생성되었는지 / 표시 순서 / 필수 여부 / 위험도(최종) 등을 보관
 * - TEMPLATE_ITEM 기반으로 생성되지만, 개인화 결과를 스냅샷으로 남기는 용도
 * ========================================================= */


------------------------------------------------------------
-- 1) USER_CHECKLIST_ITEM PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_USER_CHECKLIST_ITEM_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 2) USER_CHECKLIST_ITEM 테이블 생성
------------------------------------------------------------

CREATE TABLE USER_CHECKLIST_ITEM (
  USER_CHECKLIST_ITEM_ID  NUMBER        NOT NULL, -- PK
  USER_CHECKLIST_ID       NUMBER        NOT NULL, -- 사용자 체크리스트 ID (FK)
  ITEM_ID                 NUMBER        NOT NULL, -- 체크리스트 항목 ID (FK)

  ITEM_ORDER              NUMBER        NOT NULL, -- 사용자 체크리스트에서 노출 순서
  REQUIRED_YN             CHAR(1)       DEFAULT 'N' NOT NULL, -- 개인화 결과 기준 필수 여부
  FINAL_RISK_LEVEL        VARCHAR2(10)  DEFAULT 'LOW' NOT NULL, -- 개인화 결과 기준 최종 위험도(LOW/MEDIUM/HIGH)

  CREATED_AT              DATE          DEFAULT SYSDATE NOT NULL,
  UPDATED_AT              DATE          DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_USER_CHECKLIST_ITEM PRIMARY KEY (USER_CHECKLIST_ITEM_ID),

  -- FK
  CONSTRAINT FK_UCLI_USER_CHECKLIST
    FOREIGN KEY (USER_CHECKLIST_ID)
    REFERENCES USER_CHECKLIST (USER_CHECKLIST_ID),

  CONSTRAINT FK_UCLI_ITEM
    FOREIGN KEY (ITEM_ID)
    REFERENCES CHECKLIST_ITEM (ITEM_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_UCLI_REQUIRED CHECK (REQUIRED_YN IN ('Y','N')),
  CONSTRAINT CK_UCLI_RISK CHECK (FINAL_RISK_LEVEL IN ('LOW','MEDIUM','HIGH'))
);


------------------------------------------------------------
-- 3) 중복 방지 제약
--    하나의 사용자 체크리스트에 같은 ITEM이 중복으로 들어가는 것 방지
------------------------------------------------------------

ALTER TABLE USER_CHECKLIST_ITEM
  ADD CONSTRAINT UQ_UCLI_USERCHECKLIST_ITEM
  UNIQUE (USER_CHECKLIST_ID, ITEM_ID);


------------------------------------------------------------
-- 4) COMMENT
------------------------------------------------------------

COMMENT ON TABLE USER_CHECKLIST_ITEM IS '사용자 체크리스트에 포함된 항목 목록(개인화 결과 스냅샷)';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.USER_CHECKLIST_ITEM_ID IS '사용자 체크리스트-항목 ID(PK). SEQ_USER_CHECKLIST_ITEM_ID 사용';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.USER_CHECKLIST_ID IS '사용자 체크리스트 ID(FK - USER_CHECKLIST)';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.ITEM_ID IS '체크리스트 항목 ID(FK - CHECKLIST_ITEM)';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.ITEM_ORDER IS '사용자 체크리스트에서의 항목 노출 순서';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.REQUIRED_YN IS '개인화 결과 기준 필수 여부(Y/N)';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.FINAL_RISK_LEVEL IS '개인화 결과 기준 최종 위험도(LOW/MEDIUM/HIGH)';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.CREATED_AT IS '생성일';
COMMENT ON COLUMN USER_CHECKLIST_ITEM.UPDATED_AT IS '수정일';




/* =========================================================
 * USER_CHECKLIST_ANSWER
 * 사용자 체크리스트 항목에 대한 "응답(체크/미체크/메모)" 테이블
 *
 * - USER_CHECKLIST_ITEM(항목 목록) 1건당 응답 1건을 저장하는 구조
 * - 사용자가 체크리스트를 작성하면서 체크 여부/메모 등을 저장
 * - 나중에 "미체크 항목 + 필수 항목 미체크" 기반 위험도 계산에 사용
 * ========================================================= */


------------------------------------------------------------
-- 5) USER_CHECKLIST_ANSWER PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_USER_CHECKLIST_ANSWER_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 6) USER_CHECKLIST_ANSWER 테이블 생성
------------------------------------------------------------

CREATE TABLE USER_CHECKLIST_ANSWER (
  USER_CHECKLIST_ANSWER_ID  NUMBER        NOT NULL, -- PK
  USER_CHECKLIST_ITEM_ID    NUMBER        NOT NULL, -- 사용자 체크리스트 항목 ID (FK)

  CHECKED_YN                CHAR(1)       DEFAULT 'N' NOT NULL, -- 체크 여부(Y/N)
  ANSWER_TEXT               VARCHAR2(1000), -- 추가 메모/응답(선택)
  ANSWERED_AT               DATE          DEFAULT SYSDATE NOT NULL, -- 응답 시각

  CREATED_AT                DATE          DEFAULT SYSDATE NOT NULL,
  UPDATED_AT                DATE          DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_USER_CHECKLIST_ANSWER PRIMARY KEY (USER_CHECKLIST_ANSWER_ID),

  -- FK
  CONSTRAINT FK_UCLA_USER_CHECKLIST_ITEM
    FOREIGN KEY (USER_CHECKLIST_ITEM_ID)
    REFERENCES USER_CHECKLIST_ITEM (USER_CHECKLIST_ITEM_ID),

  -- 값 제한
  CONSTRAINT CK_UCLA_CHECKED CHECK (CHECKED_YN IN ('Y','N'))
);


------------------------------------------------------------
-- 7) 중복 방지 제약
--    항목당 응답 1개(덮어쓰기) 정책일 때 중복 생성 방지
------------------------------------------------------------

ALTER TABLE USER_CHECKLIST_ANSWER
  ADD CONSTRAINT UQ_UCLA_ITEM_ONE_ANSWER
  UNIQUE (USER_CHECKLIST_ITEM_ID);


------------------------------------------------------------
-- 8) COMMENT
------------------------------------------------------------

COMMENT ON TABLE USER_CHECKLIST_ANSWER IS '사용자 체크리스트 항목에 대한 응답(체크 여부/메모). 항목당 1응답';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.USER_CHECKLIST_ANSWER_ID IS '응답 ID(PK). SEQ_USER_CHECKLIST_ANSWER_ID 사용';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.USER_CHECKLIST_ITEM_ID IS '사용자 체크리스트 항목 ID(FK - USER_CHECKLIST_ITEM)';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.CHECKED_YN IS '체크 여부(Y/N)';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.ANSWER_TEXT IS '추가 메모/응답(선택)';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.ANSWERED_AT IS '응답 시각';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.CREATED_AT IS '생성일';
COMMENT ON COLUMN USER_CHECKLIST_ANSWER.UPDATED_AT IS '수정일';

/* =========================================================
 * CHECKLIST_ITEM_RULE
 * 체크리스트 항목 개인화/위험도 조정 룰 테이블
 *
 * - 사용자 시나리오(주거환경/경험) 조건에 따라
 *   특정 항목을 "추가/강조/필수로 변경/위험도 상향" 등의 처리를 하기 위한 룰
 *
 * 예)
 *  - EXPERIENCE_LEVEL=BEGINNER 이면
 *    '등기부등본 확인' 항목을 REQUIRED='Y'로 강제
 *  - HAS_PET_YN='Y' 이면
 *    '반려동물 특약' 항목의 위험도를 MEDIUM→HIGH로 상향
 * ========================================================= */


------------------------------------------------------------
-- 1) CHECKLIST_ITEM_RULE PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_CHECKLIST_ITEM_RULE_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 2) CHECKLIST_ITEM_RULE 테이블 생성
------------------------------------------------------------

CREATE TABLE CHECKLIST_ITEM_RULE (
  RULE_ID              NUMBER        NOT NULL, -- PK
  ITEM_ID              NUMBER        NOT NULL, -- 대상 ITEM (FK)
  TEMPLATE_TYPE        VARCHAR2(10),           -- PRE/POST에만 적용(없으면 전체 적용)

  -- 조건(간단 버전: "키-연산자-값" 형태로 저장)
  COND_KEY             VARCHAR2(50)  NOT NULL, -- 예: EXPERIENCE_LEVEL, HAS_PET_YN, HOUSING_TYPE
  COND_OPERATOR        VARCHAR2(10)  DEFAULT '=' NOT NULL, -- =, !=, IN 등(단순화)
  COND_VALUE           VARCHAR2(200) NOT NULL, -- 예: BEGINNER, Y, 오피스텔

  -- 효과(무엇을 바꿀지)
  EFFECT_TYPE          VARCHAR2(30)  NOT NULL, -- RISK_OVERRIDE / REQUIRED_OVERRIDE / EMPHASIZE / HIDE
  EFFECT_VALUE         VARCHAR2(200),          -- 예: HIGH, Y, 강조문구 등

  PRIORITY_NO          NUMBER        DEFAULT 100 NOT NULL, -- 룰 충돌 시 우선순위(작을수록 우선)
  IS_ACTIVE_YN         CHAR(1)       DEFAULT 'Y' NOT NULL, -- 활성 여부

  DESCRIPTION          VARCHAR2(500),          -- 룰 설명(팀원/관리자용)

  CREATED_AT           DATE          DEFAULT SYSDATE NOT NULL,
  UPDATED_AT           DATE          DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_CHECKLIST_ITEM_RULE PRIMARY KEY (RULE_ID),

  -- FK
  CONSTRAINT FK_ITEM_RULE_ITEM
    FOREIGN KEY (ITEM_ID)
    REFERENCES CHECKLIST_ITEM (ITEM_ID),

  -- 값 제한(데이터 품질)
  CONSTRAINT CK_ITEM_RULE_TEMPLATE_TYPE CHECK (
    TEMPLATE_TYPE IS NULL OR TEMPLATE_TYPE IN ('PRE','POST')
  ),
  CONSTRAINT CK_ITEM_RULE_OPERATOR CHECK (COND_OPERATOR IN ('=','!=','IN')),
  CONSTRAINT CK_ITEM_RULE_EFFECT CHECK (EFFECT_TYPE IN ('RISK_OVERRIDE','REQUIRED_OVERRIDE','EMPHASIZE','HIDE')),
  CONSTRAINT CK_ITEM_RULE_ACTIVE CHECK (IS_ACTIVE_YN IN ('Y','N'))
);


------------------------------------------------------------
-- 3) COMMENT 
------------------------------------------------------------

COMMENT ON TABLE CHECKLIST_ITEM_RULE IS '사용자 시나리오 조건에 따라 체크리스트 항목을 개인화하는 룰(위험도/필수/노출 조정)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.RULE_ID IS '룰 ID(PK). SEQ_CHECKLIST_ITEM_RULE_ID 사용';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.ITEM_ID IS '대상 체크리스트 항목 ID(FK - CHECKLIST_ITEM)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.TEMPLATE_TYPE IS '적용 범위(PRE/POST). NULL이면 전체 적용';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.COND_KEY IS '조건 키(예: EXPERIENCE_LEVEL, HAS_PET_YN 등)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.COND_OPERATOR IS '조건 연산자(=, !=, IN)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.COND_VALUE IS '조건 값(예: BEGINNER, Y 등)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.EFFECT_TYPE IS '효과 타입(RISK_OVERRIDE/REQUIRED_OVERRIDE/EMPHASIZE/HIDE)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.EFFECT_VALUE IS '효과 값(예: HIGH, Y 등)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.PRIORITY_NO IS '우선순위(작을수록 우선 적용)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.IS_ACTIVE_YN IS '활성 여부(Y/N)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.DESCRIPTION IS '룰 설명(관리/협업용)';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.CREATED_AT IS '생성일';
COMMENT ON COLUMN CHECKLIST_ITEM_RULE.UPDATED_AT IS '수정일';




/* =========================================================
 * USER_CHECKLIST_RESULT
 * 체크리스트 분석 결과(위험도/위험요소/행동조언/요약) 저장 테이블
 *
 * - 사용자가 체크리스트 작성 완료 후 계산된 결과를 저장
 * - "총 위험도 점수/등급", "핵심 위험요소", "사후 행동 조언" 등을 보관
 * - AI 챗봇이 근거로 인용하기 좋은 형태로 저장하는 목적
 * ========================================================= */


------------------------------------------------------------
-- 4) USER_CHECKLIST_RESULT PK용 SEQUENCE
------------------------------------------------------------

CREATE SEQUENCE SEQ_USER_CHECKLIST_RESULT_ID
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;


------------------------------------------------------------
-- 5) USER_CHECKLIST_RESULT 테이블 생성
------------------------------------------------------------

CREATE TABLE USER_CHECKLIST_RESULT (
  RESULT_ID           NUMBER         NOT NULL, -- PK
  USER_CHECKLIST_ID   NUMBER         NOT NULL, -- FK → USER_CHECKLIST

  -- 결과(요약)
  TOTAL_RISK_SCORE    NUMBER(5,2)    DEFAULT 0 NOT NULL, -- 총 위험 점수(예: 0~100)
  TOTAL_RISK_LEVEL    VARCHAR2(10)   DEFAULT 'LOW' NOT NULL, -- LOW/MEDIUM/HIGH

  -- 위험요소/근거(텍스트 요약 형태: MVP에 적합)
  RISK_SUMMARY        VARCHAR2(2000), -- 핵심 위험 요약
  RISK_FACTS          VARCHAR2(2000), -- 위험 요소 근거(예: 필수 항목 미체크 목록 등)

  -- 행동 가이드
  ACTION_GUIDE        VARCHAR2(2000), -- "무엇을 해야 하는지" 조언
  AI_EXPLANATION      VARCHAR2(2000), -- 챗봇/AI가 사용자에게 보여줄 설명 초안(선택)

  GENERATED_AT        DATE           DEFAULT SYSDATE NOT NULL, -- 결과 생성 시각

  CREATED_AT          DATE           DEFAULT SYSDATE NOT NULL,
  UPDATED_AT          DATE           DEFAULT SYSDATE NOT NULL,

  -- PK
  CONSTRAINT PK_USER_CHECKLIST_RESULT PRIMARY KEY (RESULT_ID),

  -- FK
  CONSTRAINT FK_UCR_USER_CHECKLIST
    FOREIGN KEY (USER_CHECKLIST_ID)
    REFERENCES USER_CHECKLIST (USER_CHECKLIST_ID),

  -- 값 제한
  CONSTRAINT CK_UCR_RISK_LEVEL CHECK (TOTAL_RISK_LEVEL IN ('LOW','MEDIUM','HIGH'))
);


------------------------------------------------------------
-- 6) 1:1 결과 보장
--    한 USER_CHECKLIST(헤더)당 결과는 1개만 저장(덮어쓰기/최신값 유지 정책)
------------------------------------------------------------

ALTER TABLE USER_CHECKLIST_RESULT
  ADD CONSTRAINT UQ_UCR_ONE_PER_CHECKLIST
  UNIQUE (USER_CHECKLIST_ID);


------------------------------------------------------------
-- 7) COMMENT 
------------------------------------------------------------

COMMENT ON TABLE USER_CHECKLIST_RESULT IS '체크리스트 분석 결과(총 위험도/위험요약/근거/행동조언) 저장';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.RESULT_ID IS '결과 ID(PK). SEQ_USER_CHECKLIST_RESULT_ID 사용';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.USER_CHECKLIST_ID IS '사용자 체크리스트 ID(FK - USER_CHECKLIST)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.TOTAL_RISK_SCORE IS '총 위험 점수(예: 0~100)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.TOTAL_RISK_LEVEL IS '총 위험 등급(LOW/MEDIUM/HIGH)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.RISK_SUMMARY IS '핵심 위험 요약';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.RISK_FACTS IS '위험 요소 근거(예: 필수 항목 미체크 등)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.ACTION_GUIDE IS '권장 행동 조언(사전/사후 대응)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.AI_EXPLANATION IS 'AI/챗봇이 보여줄 설명용 텍스트(선택)';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.GENERATED_AT IS '결과 생성 시각';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.CREATED_AT IS '생성일';
COMMENT ON COLUMN USER_CHECKLIST_RESULT.UPDATED_AT IS '수정일';

commit;

--

INSERT INTO CHECKLIST_TEMPLATE (
    TEMPLATE_ID,
    TEMPLATE_TYPE,
    TEMPLATE_NAME,
    VERSION_NO,
    DESCRIPTION,
    IS_ACTIVE_YN,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    SEQ_CHECKLIST_TEMPLATE_ID.NEXTVAL,
    'PRE',
    '통합 사전 체크리스트',
    1,
    '전세계약 전 필수 확인 체크리스트',
    'Y',
    SYSDATE,
    SYSDATE
);

INSERT INTO CHECKLIST_ITEM (
    ITEM_ID,
    ITEM_TITLE,
    ITEM_DESCRIPTION,
    DEFAULT_REQUIRED_YN,
    DEFAULT_RISK_LEVEL,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    SEQ_CHECKLIST_ITEM_ID.NEXTVAL,
    '전세가율이 70% 이하인지 확인했다',
    '전세가율이 높을수록 깡통전세 위험이 증가합니다.',
    'Y',
    'HIGH',
    SYSDATE,
    SYSDATE
);

INSERT INTO CHECKLIST_ITEM (
    ITEM_ID,
    ITEM_TITLE,
    ITEM_DESCRIPTION,
    DEFAULT_REQUIRED_YN,
    DEFAULT_RISK_LEVEL,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    SEQ_CHECKLIST_ITEM_ID.NEXTVAL,
    '등기부등본 갑구에 압류가 없는지 확인했다',
    '압류·경매 이력은 보증금 회수에 직접적인 영향을 줍니다.',
    'Y',
    'HIGH',
    SYSDATE,
    SYSDATE
);

INSERT INTO TEMPLATE_ITEM (
    TEMPLATE_ITEM_ID,
    TEMPLATE_ID,
    ITEM_ID,
    ITEM_ORDER,
    REQUIRED_YN,
    RISK_LEVEL_OVERRIDE,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    SEQ_TEMPLATE_ITEM_ID.NEXTVAL,
    1, -- 방금 만든 TEMPLATE_ID
    1, -- 첫 번째 ITEM_ID
    1,
    'Y',
    NULL,
    SYSDATE,
    SYSDATE
);

SELECT TEMPLATE_ID, TEMPLATE_TYPE, TEMPLATE_NAME, IS_ACTIVE_YN, VERSION_NO
FROM CHECKLIST_TEMPLATE
WHERE TEMPLATE_TYPE = 'PRE';


commit;

--