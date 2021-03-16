package au.com.maheeka.spring.springbatch.configuration;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @StepScope // tells spring to instantiate when step is executed. Not in startup. Meantime a proxy is used in its place to initialize
    public Tasklet helloWorldTasklet(@Value("#{jobParameters['message']}") String message) {
        return (contribution, chunkContext) -> {
            log.info("message : {}", message);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(helloWorldTasklet(null))
                .build();
    }

    @Bean
    public Job jobParametersJob() {
        return jobBuilderFactory.get("jobParametersTest")
                .start(step1())
                .build();
    }

}
