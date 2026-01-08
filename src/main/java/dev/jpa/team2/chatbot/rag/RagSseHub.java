package dev.jpa.team2.chatbot.rag;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class RagSseHub {

    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    @Getter
    public static class Job {
        private final Long memberId;
        private final Long sessionId;

        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        private final AtomicBoolean done = new AtomicBoolean(false);
        private volatile Throwable error;

        // 마지막에 meta(참조/후속질문 등) 보내고 싶으면 여기에 저장
        private volatile ChatRagDto finalDto;

        public Job(Long memberId, Long sessionId) {
            this.memberId = memberId;
            this.sessionId = sessionId;
        }

        public void push(String chunk) { queue.offer(chunk); }
        public void complete(ChatRagDto dto) { this.finalDto = dto; done.set(true); }
        public void fail(Throwable t) { error = t; done.set(true); }
        public boolean isDone() { return done.get(); }
        public Throwable getError() { return error; }
        public ChatRagDto getFinalDto() { return finalDto; }
    }

    public void put(String jobId, Job job) { jobs.put(jobId, job); }
    public Job get(String jobId) { return jobs.get(jobId); }
    public void remove(String jobId) { jobs.remove(jobId); }
}
