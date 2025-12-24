package dev.jpa.team2.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.tool.Tool;


@Service
@Transactional
public class DocumentsService {
  
  @Autowired
  DocumentsRepository documentsRepository;
  /**
   * 파일 수정
   * 
   * @param file1
   * @param file1saved
   * @param thumb1
   * @param size1
   * @param contentsno
   * @return
   */
  public int update_file1(String file1, String file1saved, String thumb1, long size1, long contentsno) {
    return this.documentsRepository.update_file1(file1, file1saved, thumb1, size1, contentsno);
  }
  
  public Documents save(DocumentsDTO documentsDTO) {
    Documents documents = this.documentsRepository.save(documentsDTO.toEntityWithFile());
    
    return documents;
  }
}
