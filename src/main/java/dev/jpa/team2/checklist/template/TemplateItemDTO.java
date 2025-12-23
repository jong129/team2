package dev.jpa.team2.checklist.template;

public class TemplateItemDTO {

    private Long itemId;
    private Integer order;
    private String title;
    private String description;

    private String requiredYn;
    private String riskLevel;

    public static TemplateItemDTO fromEntity(TemplateItem templateItem) {

        Item item = templateItem.getItem();

        TemplateItemDTO dto = new TemplateItemDTO();
        dto.itemId = item.getItemId();
        dto.order = templateItem.getItemOrder();
        dto.title = item.getItemTitle();
        dto.description = item.getItemDescription();

        // 필수 여부: TEMPLATE_ITEM 기준
        dto.requiredYn = templateItem.getRequiredYn();

        // 위험도: 덮어쓰기 > ITEM 기본값
        dto.riskLevel = templateItem.getRiskLevelOverride() != null
                ? templateItem.getRiskLevelOverride()
                : item.getDefaultRiskLevel();

        return dto;
    }

    // getter
    public Long getItemId() { return itemId; }
    public Integer getOrder() { return order; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRequiredYn() { return requiredYn; }
    public String getRiskLevel() { return riskLevel; }
}
