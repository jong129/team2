package dev.jpa.team2;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.jpa.team2.tool.HandlerMdcInterceptor;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

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
              .addPathPatterns("/**");
  }
  
  
}