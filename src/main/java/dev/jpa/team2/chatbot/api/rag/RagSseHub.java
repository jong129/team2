package dev.jpa.team2.chatbot.api.rag;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import dev.jpa.team2.chatbot.domain.rag.ChatRagDto;
import lombok.Getter;

// 역활 : jobId별로 “생성 중인 답변”의 상태(큐/완료/에러/최종 DTO)를 서버 메모리에 저장해두는 허브(레지스트리)
// 사용 : 프론트가 SSE 연결을 열면 emitter 등록. 백그라운드 작업(LLM 생성)이 진행되면서 이벤트들이 RagSseHub 통해서 해당 사용자에게 전달

@Component
public class RagSseHub {    // 상태 저장소

    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    @Getter
    public static class Job {
        // job 소유자 정보
        private final Long memberId;
        private final Long sessionId;
        
        // 답변 스트리밍 큐 : 생성 스레드가 답변을 쪼개서 push()로 큐에 넣고, SSE 스트리밍 스레드는 poll()로 큐에서 꺼내서 클라이언트에 보냄
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        
        // 완료/실패 상태
        private final AtomicBoolean done = new AtomicBoolean(false);
        private volatile Throwable error;

        // 최종 결과 DTO 저장(메타 전송용)
        private volatile ChatRagDto finalDto;

        public Job(Long memberId, Long sessionId) {
            this.memberId = memberId;
            this.sessionId = sessionId;
        }
        
        // push: 스트리밍할 문자열 조각 생산, complete: 최종 성공, fail: 실패
        public void push(String chunk) { queue.offer(chunk); }
        public void complete(ChatRagDto dto) { this.finalDto = dto; done.set(true); }
        public void fail(Throwable t) { error = t; done.set(true); }
        public boolean isDone() { return done.get(); }
        public Throwable getError() { return error; }
        public ChatRagDto getFinalDto() { return finalDto; }
    }
    
    // Hub의 기본 CRUD
    public void put(String jobId, Job job) { jobs.put(jobId, job); }
    public Job get(String jobId) { return jobs.get(jobId); }
    public void remove(String jobId) { jobs.remove(jobId); }
}
