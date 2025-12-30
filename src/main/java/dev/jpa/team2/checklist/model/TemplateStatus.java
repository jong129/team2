package dev.jpa.team2.checklist.model;

public enum TemplateStatus {
    DRAFT, ACTIVE, RETIRED
    /*
     * DRAFT: 초안 상태(관리자 작성 중, 사용자 미노출)
     * ACTIVE: 현재 서비스 중인 템플릿
     * RETIRED: 과거 템플릿
     */
}
