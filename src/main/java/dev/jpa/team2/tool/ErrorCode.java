package dev.jpa.team2.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    FILE_MISSING(400, "파일이 없습니다."),
    FASTAPI_ERROR(500, "FastAPI 호출 실패"),
    SERVER_ERROR(500, "서버 오류");

    private final int status;
    private final String message;
}

