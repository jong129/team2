package dev.jpa.team2.board.photo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardImageBlockLogWriter {

  private final BoardImageBlockLogRepository repo;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void save(BoardImageBlockLog log) {   // ✅ 여기 타입
    repo.saveAndFlush(log);
  }
}
