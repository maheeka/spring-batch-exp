package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public ListItemReader<String> itemReader() {
        List<String> items = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            items.add(String.valueOf(i));
        }
        return new ListItemReader(items);
    }

    public ItemWriter itemWriter() {
        return new SysOutItemWriter();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(new ChunkListener())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("item writer")
                .start(step1())
                .build();
    }
}
