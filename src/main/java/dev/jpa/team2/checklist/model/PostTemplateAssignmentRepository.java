package dev.jpa.team2.checklist.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTemplateAssignmentRepository extends JpaRepository<PostTemplateAssignment, Long> {

  Optional<PostTemplateAssignment> findByProfileKeyIdAndPostGroupCodeAndActiveYn(Long profileKeyId,
      String postGroupCode, String activeYn);
}
