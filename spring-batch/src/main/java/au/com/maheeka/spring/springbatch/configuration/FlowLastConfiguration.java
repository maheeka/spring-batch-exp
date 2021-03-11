package au.com.maheeka.spring.springbatch.configuration;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class FlowFirstConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> This is step 1");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> This is step 2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> This is step 3");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    // SIMPLE STEPS
//    @Bean
//    public Job transitionJobSimpleNext() {
//        return jobBuilderFactory.get(UUID.randomUUID().toString())
//                .start(step1())
//                .next(step2())
//                .next(step3())
//                .build();
//    }

    // CONDITIONAL STEPS. SAME AS ABOVE
    @Bean
    public Job transitionJobSimpleNext() {
        return jobBuilderFactory.get(UUID.randomUUID().toString())
                .start(step1())
                .on(ExitStatus.COMPLETED.getExitCode()).to(step2()) // can have any custom status here
//                .from(step2()).on("COMPLETED").stop()
                .from(step2()).on("COMPLETED").stopAndRestart(step3())
//                .from(step2()).on("COMPLETED").to(step3())
                .from(step3()).end()
                .build();
    }

}
