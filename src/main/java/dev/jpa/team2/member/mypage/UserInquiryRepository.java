package dev.jpa.team2.member.mypage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInquiryRepository extends JpaRepository<UserInquiry, Long> {

  Page<UserInquiry> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

  Optional<UserInquiry> findByInquiryIdAndMemberId(Long inquiryId, Long memberId);
}
