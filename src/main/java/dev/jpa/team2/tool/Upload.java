package dev.jpa.team2.tool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.web.multipart.MultipartFile;

public class Upload  {
  public static String saveFile(MultipartFile multipartFile, String absPath) {

    if (multipartFile == null || multipartFile.isEmpty()) {
        return "";
    }

    String originalFilename = multipartFile.getOriginalFilename();
    if (originalFilename == null) {
        return "";
    }

    // 파일명 / 확장자 분리
    int dotIndex = originalFilename.lastIndexOf(".");
    String baseName = (dotIndex > -1) ? originalFilename.substring(0, dotIndex) : originalFilename;
    String extension = (dotIndex > -1) ? originalFilename.substring(dotIndex) : "";

    // 저장 디렉토리 보장
    File dir = new File(absPath);
    if (!dir.exists()) {
        dir.mkdirs();
    }

    // 중복 파일명 처리
    String savedFilename = originalFilename;
    File saveFile = new File(dir, savedFilename);

    for (int i = 1; saveFile.exists(); i++) {
        savedFilename = baseName + "_" + i + extension;
        saveFile = new File(dir, savedFilename);
    }

    // 파일 저장
    try (
        InputStream in = multipartFile.getInputStream();
        OutputStream out = new FileOutputStream(saveFile)
    ) {
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

    } catch (Exception e) {
        throw new RuntimeException("파일 저장 실패: " + originalFilename, e);
    }

    System.out.println("-> 파일 저장 완료: " + saveFile.getAbsolutePath());

    return saveFile.getAbsolutePath();
}
}


