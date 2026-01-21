package dev.jpa.team2.checklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jpa.team2.checklist.enums.Yn;
import dev.jpa.team2.checklist.model.PostTemplateAssignment;

public interface PostAssignmentRepository
        extends JpaRepository<PostTemplateAssignment, Long> {

    // POST 시작 시: 프로필 기반 활성 템플릿 매핑 조회
    Optional<PostTemplateAssignment>
        findByProfileKeyIdAndActiveYn(Long profileKeyId, Yn activeYn);
}
