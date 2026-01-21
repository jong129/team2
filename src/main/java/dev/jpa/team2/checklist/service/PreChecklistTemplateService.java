package dev.jpa.team2.checklist.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import dev.jpa.team2.checklist.dto.PreChecklistTemplateDto;
import dev.jpa.team2.checklist.dto.PreChecklistTemplateItemDto;
import dev.jpa.team2.checklist.enums.ChecklistPhase;
import dev.jpa.team2.checklist.enums.TemplateStatus;
import dev.jpa.team2.checklist.model.ChecklistItemMaster;
import dev.jpa.team2.checklist.model.ChecklistTemplate;
import dev.jpa.team2.checklist.model.ChecklistTemplateItem;
import dev.jpa.team2.checklist.repository.ItemMasterRepository;
import dev.jpa.team2.checklist.repository.TemplateItemRepository;
import dev.jpa.team2.checklist.repository.TemplateRepository;

@Service
@RequiredArgsConstructor
public class PreChecklistTemplateService {

  private final TemplateRepository templateRepository;
  private final TemplateItemRepository templateItemRepository;
  private final ItemMasterRepository itemMasterRepository;

  /**
   * ✅ ACTIVE 상태의 PRE 체크리스트 템플릿 조회
   */
  public PreChecklistTemplateDto getActivePreTemplate() {

    // 1️⃣ ACTIVE + PRE 템플릿 1개
    ChecklistTemplate template = templateRepository.findFirstByPhaseOrderByVersionNoDesc(ChecklistPhase.PRE)
        .orElseThrow(() -> new IllegalStateException("PRE 템플릿이 존재하지 않습니다."));

    // 2️⃣ 템플릿에 속한 아이템 연결 정보
    List<ChecklistTemplateItem> templateItems = templateItemRepository
        .findByTemplate_TemplateIdOrderByItemOrderAsc(template.getTemplateId());

    // 3️⃣ ITEM_MASTER 조회 + 프론트용 구조 조립
    List<PreChecklistTemplateItemDto> items = new ArrayList<>();

    for (ChecklistTemplateItem ti : templateItems) {

      ChecklistItemMaster im = ti.getItemMaster();

      items.add(new PreChecklistTemplateItemDto(im.getItemMasterId(), im.getCheckArea(), im.getTitle(),
          im.getDescription(), ti.getRequiredYn()));
    }

    return new PreChecklistTemplateDto(template.getTemplateId(), template.getTemplateName(), items);
  }
}
