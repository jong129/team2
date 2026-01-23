package dev.jpa.team2.documents;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentAnalyzeResponse {

    // ✅ Spring DB에서 생성된 진짜 docId
    private Long docId;
    private Long userId;

    // 문서 기본 정보
    private String docType;
    private String policyVersion;
    private Integer riskScore;
    private List<String> reasons;
    private String aiExplanation;

    // 원본/추가 데이터(필요하면 그대로 실어 보내기)
    private Map<String, Object> parsedData;
    private List<String> docEvidence;
    private Integer docConfidence;

    // 디버그용(선택)
    private String imagePath;
}
