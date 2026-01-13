package dev.jpa.team2;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.jpa.team2.tool.HandlerMdcInterceptor;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                          "http://121.160.42.13:5173",
                          "http://localhost:5173",
                          "http://121.160.42.21:5173",
                          "http://121.160.42.28:5173",
                          "http://121.160.42.81:5173"
                ) // 프론트엔드 출처
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    private final HandlerMdcInterceptor handlerMdcInterceptor;


  public WebConfig(HandlerMdcInterceptor handlerMdcInterceptor) {
      this.handlerMdcInterceptor = handlerMdcInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(handlerMdcInterceptor)
      .addPathPatterns("/**")
      .excludePathPatterns("/files/**"); // ✅ 정적 파일 제외 권장
  }
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // ✅ 윈도우 경로도 안전하게 file: URI로 변환
    String storageDir = Paths.get(System.getProperty("user.dir"), "documents", "storage")
        .toUri()
        .toString();
    // 예: file:/C:/kd/team2/team2/documents/storage/

    registry.addResourceHandler("/files/**")
        .addResourceLocations(storageDir)
        .setCachePeriod(3600);
  }
  
  
}