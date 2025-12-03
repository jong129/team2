DROP TABLE ADMIN CASCADE CONSTRAINTS; -- 자식 무시하고 삭제 가능
DROP TABLE ADMIN;

-- 1. ADMIN 테이블 생성
CREATE TABLE ADMIN (
    ADMIN_ID              NUMBER(10)      PRIMARY KEY,  -- 관리자 고유 ID (PK)
    ADMIN_NAME            VARCHAR2(100)   NOT NULL,     -- 관리자 이름
    
    -- **[보안 강화]**
    ADMIN_ROLE            VARCHAR2(20)    DEFAULT 'GENERAL_ADMIN' NOT NULL, -- 권한 등급
    ADMIN_EMAIL           VARCHAR2(100)   UNIQUE,       -- 이메일
    ADMIN_PHONE           VARCHAR2(20),                  -- 핸드폰 번호
    LOGIN_FAIL_COUNT      NUMBER          DEFAULT 0 NOT NULL, -- 로그인 실패 횟수
    ACCOUNT_STATUS        VARCHAR2(10)    DEFAULT 'ACTIVE' NOT NULL, -- 계정 상태 (ACTIVE(활동), LOCKED(잠김), INACTIVE(휴면))
    
    -- **[메타 데이터]**
    CREATED_AT            TIMESTAMP       DEFAULT SYSDATE NOT NULL, -- 생성 일시
    UPDATED_AT            TIMESTAMP,                              -- 최종 수정 일
    LAST_LOGIN_AT         TIMESTAMP                               -- 마지막 로그인 일
);

-- 2. 컬럼에 대한 주석 추가 (가독성 향상)
COMMENT ON COLUMN ADMIN.ADMIN_ID IS '관리자 고유 ID (PK)';
COMMENT ON COLUMN ADMIN.ADMIN_NAME IS '관리자 이름';
COMMENT ON COLUMN ADMIN.ADMIN_ROLE IS '관리자 권한 등급 (SUPER_ADMIN, GENERAL_ADMIN 등)';
COMMENT ON COLUMN ADMIN.ADMIN_EMAIL IS '관리자 이메일';
COMMENT ON COLUMN ADMIN.ADMIN_PHONE IS '관리자 핸드폰';
COMMENT ON COLUMN ADMIN.LOGIN_FAIL_COUNT IS '비밀번호 실패 횟수';
COMMENT ON COLUMN ADMIN.ACCOUNT_STATUS IS '계정 상태 (ACTIVE, LOCKED 등)';
COMMENT ON COLUMN ADMIN.CREATED_AT IS '계정 생성 일시';
COMMENT ON COLUMN ADMIN.UPDATED_AT IS '계정 정보 최종 수정 일시';
COMMENT ON COLUMN ADMIN.LAST_LOGIN_AT IS '마지막 로그인 일시';

DROP SEQUENCE ADMIN_SEQ;

CREATE SEQUENCE ADMIN_SEQ
  START WITH 1                -- 시작 번호
  INCREMENT BY 1            -- 증가값
  MAXVALUE 9999999999  -- 최대값: 9999999999 --> NUMBER(10) 대응
  CACHE 2                        -- 2번은 메모리에서만 계산
  NOCYCLE;                      -- 다시 1부터 생성되는 것을 방지

INSERT INTO ADMIN (ADMIN_ID, ADMIN_NAME,  ADMIN_ROLE, ADMIN_EMAIL, ADMIN_PHONE, CREATED_AT) 
VALUES ( ADMIN_SEQ.nextval, 'admin01', '최고 관리자', 'admin01@homescanner.com', '010-1234-5678', SYSDATE);

INSERT INTO ADMIN (ADMIN_ID, ADMIN_NAME, ADMIN_PW,  ADMIN_ROLE, ADMIN_EMAIL, ADMIN_PHONE, CREATED_AT) 
VALUES ( ADMIN_SEQ.nextval, 'admin02', '매니저', 'admin02@homescanner.com', '010-1234-5678', SYSDATE);

INSERT INTO ADMIN (ADMIN_ID, ADMIN_NAME, ADMIN_PW,  ADMIN_ROLE, ADMIN_EMAIL, ADMIN_PHONE, CREATED_AT) 
VALUES ( ADMIN_SEQ.nextval, 'admin03', '분석 담당자', 'admin03@homescanner.com', '010-1234-5678', SYSDATE);

-- 변경사항 저장
COMMIT;

-- 전체 관리자 목록 조회
SELECT ADMIN_ID, ADMIN_NAME, ADMIN_ROLE, ADMIN_EMAIL, ADMIN_PHONE, ACCOUNT_STATUS, CREATED_AT, UPDATED_AT, LAST_LOGIN_AT
FROM ADMIN
ORDER BY CREATED_AT DESC;

-- 특정 관리자 상세 조회 (ID로 검색)
SELECT * FROM ADMIN 
WHERE ADMIN_ID = 'admin01';

-- 관리자 정보 수정 (전화번호, 이메일 등) 수정
UPDATE ADMIN
SET ADMIN_PHONE = '010-9876-5432', ADMIN_EMAIL = 'new_email@test.com', UPDATED_AT = SYSDATE            
WHERE ADMIN_ID = 'admin01';

COMMIT;

-- 로그인 실패 횟수 초기화 및 계정 잠금 해제
UPDATE ADMIN
SET 
    LOGIN_FAIL_COUNT = 0,
    ACCOUNT_STATUS = 'ACTIVE',
    UPDATED_AT = SYSDATE
WHERE ADMIN_ID = 'admin01';

COMMIT;

-- 데이터 삭제 (DELETE)
DELETE FROM ADMIN 
WHERE ADMIN_ID = 'admin01';

COMMIT;