package dev.jpa.team2.checklist.ai;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.checklist.ai.AiPostItemStatDTO;
import dev.jpa.team2.checklist.ai.AiPostTemplateRowDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPostChecklistService {

  private final AiPostChecklistStatRepository statRepo;

  public List<AiPostTemplateRowDTO> listActivePostTemplates() {
    return statRepo.listActivePostTemplateSummary().stream().map(r -> {
      // Object[] 순서: templateId, postGroupCode, versionNo, templateName, completedSessionCnt, avgRating
      Long templateId = toLong(r[0]);
      String postGroupCode = toStr(r[1]);
      Integer versionNo = toInt(r[2]);
      String templateName = toStr(r[3]);
      Long completedCnt = toLong(r[4]);
      Double avgRating = toDouble(r[5]);

      return AiPostTemplateRowDTO.builder()
          .templateId(templateId)
          .postGroupCode(postGroupCode)
          .versionNo(versionNo)
          .templateName(templateName)
          .completedSessionCnt(completedCnt == null ? 0L : completedCnt)
          .avgRating(avgRating)
          .build();
    }).toList();
  }

  public List<AiPostItemStatDTO> getPostTemplateItemStats(Long templateId) {
    return statRepo.getPostItemStats(templateId).stream().map(r -> {
      // 순서: itemId, itemOrder, checkArea, title, requiredYn,
      //       totalCnt, doneCnt, notDoneCnt, notRequiredCnt,
      //       avgRatingWhenDone, avgRatingWhenNotDone
      Long itemId = toLong(r[0]);
      Integer itemOrder = toInt(r[1]);
      String checkArea = toStr(r[2]);
      String title = toStr(r[3]);
      String requiredYn = toStr(r[4]);

      long total = nz(toLong(r[5]));
      long done = nz(toLong(r[6]));
      long notDone = nz(toLong(r[7]));
      long notReq = nz(toLong(r[8]));

      double doneRate = rate(done, total);
      double notDoneRate = rate(notDone, total);
      double notReqRate = rate(notReq, total);

      Double avgDone = toDouble(r[9]);
      Double avgNotDone = toDouble(r[10]);
      Double delta = (avgDone == null || avgNotDone == null) ? null : (avgDone - avgNotDone);

      return AiPostItemStatDTO.builder()
          .itemId(itemId)
          .itemOrder(itemOrder)
          .checkArea(checkArea)
          .title(title)
          .requiredYn(requiredYn)
          .totalCnt(total)
          .doneCnt(done)
          .notDoneCnt(notDone)
          .notRequiredCnt(notReq)
          .doneRate(doneRate)
          .notDoneRate(notDoneRate)
          .notRequiredRate(notReqRate)
          .avgRatingWhenDone(avgDone)
          .avgRatingWhenNotDone(avgNotDone)
          .deltaRating(delta)
          .build();
    }).toList();
  }

  private double rate(long num, long den) {
    if (den <= 0) return 0.0;
    return (double) num / (double) den;
  }

  private long nz(Long v) { return v == null ? 0L : v; }

  private Long toLong(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.longValue();
    return Long.valueOf(o.toString());
  }
  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.intValue();
    return Integer.valueOf(o.toString());
  }
  private Double toDouble(Object o) {
    if (o == null) return null;
    if (o instanceof Number n) return n.doubleValue();
    return Double.valueOf(o.toString());
  }
  private String toStr(Object o) { return o == null ? null : o.toString(); }
  
  public List<AiPostItemSignalDTO> getItemSignals(Long templateId) {
    return getPostTemplateItemStats(templateId).stream()
        .map(PostChecklistSignalRule::judge)
        .toList();
  }

  
}
