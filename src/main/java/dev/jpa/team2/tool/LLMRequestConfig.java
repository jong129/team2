package dev.jpa.team2.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate; // 다른 서버의 접근 지원

@Configuration
public class LLMRequestConfig {
    public LLMRequestConfig() {
      System.out.println("-> LLMRequestConfig created.");  
    }
    
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
    
    private String SpringBoot_FastAPI_KEY = "1234";

    public String getSpringBoot_FastAPI_KEY() {
      return SpringBoot_FastAPI_KEY;
    }
    
}
