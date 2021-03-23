package au.com.maheeka.spring.springbatch.configuration;

import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
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
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("customerDataSource")
    private DataSource customerDataSource;

    @Autowired
    @Qualifier("customerWriteDataSource")
    private DataSource customerWriteDataSource;

    @Bean
    @StepScope
    public Tasklet restartTasklet() {
        return (contribution, chunkContext) -> {
            Map<String, Object> stepExecutionContext = chunkContext.getStepContext().getStepExecutionContext();

            if (stepExecutionContext.containsKey("ran")) {
                log.info(">>>>> This time we let it go");
                return RepeatStatus.FINISHED;
            } else {
                log.info(">>>>> I dont think so");
                chunkContext.getStepContext().getStepExecution().getExecutionContext().put("ran", true);
                throw new RuntimeException(">>>>> Not this time ...");
            }
        };
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Step step2() throws Exception {
        return stepBuilderFactory.get("step2")
                .tasklet(restartTasklet())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job-restart")
                .start(step1())
                .next(step2())
                .build();
    }


}
