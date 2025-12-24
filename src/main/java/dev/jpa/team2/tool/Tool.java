package dev.jpa.team2.tool;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Tool {
  /**
   * yyyy-MM-dd hh:mm:ss 형식 날짜 시간 
   * @return
   */
  public static String getDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    String now = sdf.format(new Date());
    
    return now;
  }

public static String getServerDir(String dir) {
    String baseDir = System.getProperty("user.dir"); // 프로젝트 루트

    File storageDir = new File(
        baseDir + File.separator + dir + File.separator + "storage"
    );

    if (!storageDir.exists()) {
        storageDir.mkdirs();
    }
    return storageDir.getAbsolutePath();
}

  public static synchronized boolean checkUploadFile(String file) {
    boolean sw = false;
    if (file != null) {
      file = file.toLowerCase();
      if (file.endsWith("jpg") || file.endsWith(".jpeg") || file.endsWith(".png") || file.endsWith("gif")
          || file.endsWith("txt") || file.endsWith("hwp") || file.endsWith("xls") || file.endsWith("xlsx")
          || file.endsWith("ppt") || file.endsWith("pptx") || file.endsWith("zip") || file.endsWith("tar")
          || file.endsWith("mp3") || file.endsWith("mp4") || file.endsWith("gz") || file.endsWith("ipynb")
          || file.endsWith("doc") || file.endsWith("csv")) {
        sw = true;
      }
    }
    return sw;
  }

  /**
   * byte 수를 전달받아 자료의 단위를 적용합니다.
   * 
   * @param size
   * @return 1000 → 1000 Byte
   */
  public static synchronized String unit(long size) {
    String str = "";

    if (size < 1024) { // 1 KB 이하, 1024 byte 미만이면
      str = size + " Byte";
    } else if (size < 1024 * 1024) { // 1 MB 이하, 1048576 byte 미만이면 KB
      str = (int) (Math.ceil(size / 1024.0)) + " KB";
    } else if (size < 1024 * 1024 * 1024) { // 1 GB 이하, 1073741824 byte 미만
      str = (int) (Math.ceil(size / 1024.0 / 1024.0)) + " MB";
    } else if (size < 1024L * 1024 * 1024 * 1024) { // 1 TB 이하, 큰 정수 표현을 위해 int -> long형식으로 변환
      str = (int) (Math.ceil(size / 1024.0 / 1024.0 / 1024.0)) + " GB";
    } else if (size < 1024L * 1024 * 1024 * 1024 * 1024) { // 1 PT 이하
      str = (int) (Math.ceil(size / 1024.0 / 1024.0 / 1024.0 / 1024.0)) + " TB";
    } else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024) { // 1 EX 이하
      str = (int) (Math.ceil(size / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0)) + " PT";
    } else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) { // 1 ZB 이하
      str = (int) (Math.ceil(size / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0)) + " EX";
    } else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) { // 1 YB 이하
      str = (int) (Math.ceil(size / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0 / 1024.0)) + " ZB"; // Google이
                                                                                                            // 사용 중인 단위
    }

    return str;
  }

  /**
   * 이미지인지 검사
   * 
   * @param file 파일명
   * @return
   */
  public static synchronized boolean isImage(String file) {
    boolean sw = false;
    if (file != null) {
      file = file.toLowerCase();
      if (file.endsWith("jpg") || file.endsWith(".jpeg") || file.endsWith(".png") || file.endsWith("gif")) {
        sw = true;
      }
    }
    return sw;
  }

  /**
   * 이미지 사이즈를 변경하여 새로운 Preview 이미지를 생성합니다.
   * 
   * <pre>
   사용예): Tool.preview(folder 명, 원본 파일명, 200, 150)
   * </pre>
   * 
   * @param upDir  원본 이미지 폴더
   * @param _src   원본 파일명
   * @param width  생성될 이미지 너비
   * @param height 생성될 이미지 높이, ImageUtil.RATIO는 자동 비례 비율
   * @return src.jpg 파일을 이용하여 src_t.jpg 파일을 생성하여 파일명 리턴
   */
  public static synchronized String preview(String upDir, String _src, int width, int height) {
    int RATIO = 0;
    int SAME = -1;

    File src = new File(upDir + "/" + _src); // 원본 파일 객체 생성
    String srcname = src.getName(); // 원본 파일명 추출

    // System.out.println("-> preview() srcname: " + srcname);

    int ext_index = srcname.lastIndexOf("."); // 파일 구분자 "." 위치 추출
    String _dest = srcname.substring(0, ext_index); // 순수 파일명 추출, mt.jpg -> mt 만 추출
    String ext_filename = srcname.substring(ext_index); // 파일 확장자 추출, .jpg

    // System.out.println("-> preview() ext_filename: " + ext_filename);

    // 축소 이미지 조합 /upDir/mt_t.jpg
    File dest = new File(upDir + "/" + _dest + "_t " + ext_filename);
    // System.out.println("-> preview() dest: " + dest);

    Image srcImg = null;

    String name = src.getName().toLowerCase(); // 파일명을 추출하여 소문자로 변경
    // 이미지 파일인지 검사
    if (name.endsWith("jpg") || name.endsWith("jpeg") || name.endsWith("bmp") || name.endsWith("png")
        || name.endsWith("gif")) {
      try {
        srcImg = ImageIO.read(src); // 메모리에 원본 이미지 생성
        int srcWidth = srcImg.getWidth(null); // 원본 이미지 너비 추출
        int srcHeight = srcImg.getHeight(null); // 원본 이미지 높이 추출
        int destWidth = -1, destHeight = -1; // 대상 이미지 크기 초기화

        if (width == SAME) { // width가 같은 경우
          destWidth = srcWidth;
        } else if (width > 0) {
          destWidth = width; // 새로운 width를 할당
        }

        if (height == SAME) { // 높이가 같은 경우
          destHeight = srcHeight;
        } else if (height > 0) {
          destHeight = height; // 새로운 높이로 할당
        }

        // 비율에 따른 크기 계산
        if (width == RATIO && height == RATIO) {
          destWidth = srcWidth;
          destHeight = srcHeight;
        } else if (width == RATIO) {
          double ratio = ((double) destHeight) / ((double) srcHeight);
          destWidth = (int) ((double) srcWidth * ratio);
        } else if (height == RATIO) {
          double ratio = ((double) destWidth) / ((double) srcWidth);
          destHeight = (int) ((double) srcHeight * ratio);
        }

        // 메모리에 대상 이미지 생성
        Image imgTarget = srcImg.getScaledInstance(destWidth, destHeight, Image.SCALE_SMOOTH);
        int pixels[] = new int[destWidth * destHeight];
        PixelGrabber pg = new PixelGrabber(imgTarget, 0, 0, destWidth, destHeight, pixels, 0, destWidth);

        pg.grabPixels();

        BufferedImage destImg = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
        destImg.setRGB(0, 0, destWidth, destHeight, pixels, 0, destWidth);

        // 파일에 기록
        ImageIO.write(destImg, "jpg", dest);

        System.out.println(dest.getName() + " 이미지를 생성했습니다.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return dest.getName();
  }

}
