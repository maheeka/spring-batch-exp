package au.com.maheeka.spring.springbatch.configuration;

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
    public StatelessItemReader statelessItemReader() {
        List<String> data = new ArrayList<>(3);
        data.add("A");
        data.add("B");
        data.add("C");
        return new StatelessItemReader(data);
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String, String> chunk(2)
                .reader(statelessItemReader())
                .writer(list -> {
                    list.forEach(i -> log.info(">> {}", i));
                })
                .build();
    }

    @Bean
    public Job jobParametersJob() {
        return jobBuilderFactory.get("interfacesJob")
                .start(step1())
                .build();
    }

}
