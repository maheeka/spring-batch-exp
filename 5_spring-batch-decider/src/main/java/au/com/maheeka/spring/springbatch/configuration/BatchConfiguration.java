package au.com.maheeka.spring.springbatch.configuration;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

   @Bean
   public Step startStep() {
       return stepBuilderFactory.get("startStep")
               .tasklet(((contribution, chunkContext) -> {
                   log.info("This is start tasklet");
                   return RepeatStatus.FINISHED;
               })).build();
   }

    @Bean
    public Step evenStep() {
        return stepBuilderFactory.get("evenStep")
                .tasklet(((contribution, chunkContext) -> {
                    log.info("This is even tasklet");
                    return RepeatStatus.FINISHED;
                })).build();
    }

    @Bean
    public Step oddStep() {
        return stepBuilderFactory.get("oddStep")
                .tasklet(((contribution, chunkContext) -> {
                    log.info("This is odd tasklet");
                    return RepeatStatus.FINISHED;
                })).build();
    }

    @Bean
    public JobExecutionDecider decider() {
       return new OddDecider();
    }

    @Bean
    public Job job() {
       return jobBuilderFactory.get(UUID.randomUUID().toString())
               .start(startStep())
               .next(decider())
               .from(decider()).on("ODD").to(oddStep())
               .from(decider()).on("EVEN").to(evenStep())
               .from(oddStep()).on("*").to(decider()) // if odd step was called, regardless of the status call decider, which will make the decider even
               .from(decider()).on("ODD").to(oddStep())
               .from(decider()).on("EVEN").to(evenStep())
               .end()
               .build();
    }

    public static class OddDecider implements JobExecutionDecider {

        private int count = 0;


        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            count ++;

            System.out.println(">> " + count);
            if (count % 2 == 0) {
                return new FlowExecutionStatus("EVEN"); // used to decide what next step will be
            } else {
                return new FlowExecutionStatus("ODD");
            }
        }
    }

}
