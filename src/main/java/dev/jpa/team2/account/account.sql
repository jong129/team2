DROP TABLE ACCOUNT CASCADE CONSTRAINTS; -- 자식 무시하고 삭제 가능
DROP TABLE ACCOUNT;
                                                                                                                   
-- 1. ACCOUNT 테이블 생성
CREATE TABLE ACCOUNT (
    ACCOUNT_ID        VARCHAR2(50)    PRIMARY KEY,               -- 로그인 ID (이메일 또는 고유 식별자)
    PASSWORD_HASH     VARCHAR2(200)   NOT NULL,                  -- 해시된 비밀번호
    ROLE              VARCHAR2(20)    NOT NULL,                  -- 'ADMIN' / 'MEMBER'
    REF_ID            NUMBER(10)      NOT NULL,                  -- ADMIN_ID 또는 MEMBER_ID (둘 중 하나)
    IS_ACTIVE         NUMBER(1)       DEFAULT 1 NOT NULL,        -- 1=활성, 0=비활성
    FAIL_COUNT        NUMBER          DEFAULT 0 NOT NULL,        -- 로그인 실패 횟수
    ACCOUNT_STATUS    VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL, -- ACTIVE / LOCKED / INACTIVE

    LAST_LOGIN_AT     TIMESTAMP,                                 -- 마지막 로그인 시간

    -- 정합성(ROLE과 REF_ID 관계 보장)
    CONSTRAINT chk_account_role
        CHECK (
            ROLE IN ('ADMIN', 'MEMBER')
        )
);

-- 2. 컬럼에 대한 주석 추가 (가독성 향상)
COMMENT ON COLUMN ADMIN.ADMIN_ID IS '관리자 고유 ID (PK)';
COMMENT ON COLUMN ADMIN.ADMIN_NAME IS '관리자 이름';
COMMENT ON COLUMN ADMIN.ADMIN_PW IS '관리자용 PW';
COMMENT ON COLUMN ADMIN.ADMIN_ROLE IS '관리자 권한 등급 (SUPER_ADMIN, GENERAL_ADMIN 등)';
COMMENT ON COLUMN ADMIN.ADMIN_EMAIL IS '관리자 이메일';
COMMENT ON COLUMN ADMIN.ADMIN_PHONE IS '관리자 핸드폰';
COMMENT ON COLUMN ADMIN.LOGIN_FAIL_COUNT IS '비밀번호 실패 횟수';
COMMENT ON COLUMN ADMIN.ACCOUNT_STATUS IS '계정 상태 (ACTIVE, LOCKED 등)';
COMMENT ON COLUMN ADMIN.CREATED_AT IS '계정 생성 일시';
COMMENT ON COLUMN ADMIN.UPDATED_AT IS '계정 정보 최종 수정 일시';
COMMENT ON COLUMN ADMIN.LAST_LOGIN_AT IS '마지막 로그인 일시';