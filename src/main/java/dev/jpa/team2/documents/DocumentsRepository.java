package dev.jpa.team2.documents;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentsRepository extends JpaRepository<Documents,Long> {
  List<Documents> findByUserIdOrderByDocIdDesc(Long userId);
  Optional<DocumentReport> findByDocId(Long docId);
  void deleteByDocId(Long docId);
  @Modifying
  @Query(value="UPDATE contents SET file1=:file1, file1saved=:file1saved, thumb1=:thumb1, size1=:size1 WHERE contentsno =:contentsno", nativeQuery = true)
  public int update_file1(@Param("file1") String file1,
                                 @Param("file1saved") String file1saved,
                                 @Param("thumb1") String thumb1, 
                                 @Param("size1") long size1,
                                 @Param("contentsno") long contentsno);
  
}
