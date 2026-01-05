package dev.jpa.team2;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                          "http://121.160.42.13:5173",
                          "http://121.160.42.28:5173",
                          "http://localhost:5173"
                ) // 프론트엔드 출처
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

//  @Override // WebMvcConfigurer interface의 추상 메소드를 구현하는 선언
//  public void addCorsMappings(CorsRegistry registry) {
//      registry.addMapping("/**")
//              .allowedOriginPatterns("*") // 프론트엔드 출처
//              .allowedMethods("*")
//              .allowedHeaders("*")
//              .allowCredentials(true); // Cookie 사용
//  }

  
  
}