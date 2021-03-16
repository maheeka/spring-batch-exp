package au.com.maheeka.spring.springbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
public class ChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        log.info(">> Before chunk");
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        log.info("<< After chunk");
    }
}
