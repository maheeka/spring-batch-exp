package au.com.maheeka.spring.springbatch.configuration;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class FlowLastConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step myStepLast() {
        return stepBuilderFactory.get("myStepLast")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> This is myStep");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Job flowLastJob(Flow flow) {
        return jobBuilderFactory.get("flowLastJob") // cannot use the start...next in flow first jobs. need to use `on` style
                .start(myStepLast())
                .on("COMPLETED").to(flow)
                .end()
                .build();
    }

}
