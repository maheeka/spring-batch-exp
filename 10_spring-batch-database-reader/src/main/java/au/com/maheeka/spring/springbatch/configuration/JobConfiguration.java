package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
    public JdbcCursorItemReader<Customer> cursorItemReader() {
        JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<>();

        reader.setSql("SELECT id, firstName, lastName, birthdate from customer");
        reader.setDataSource(customerDataSource());
        reader.setRowMapper(new CustomerRowMapper());

        return reader;
    }

    @Bean
    public ItemWriter customerItemWriter() {
        return items -> items.forEach(item -> System.out.println(item));
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(cursorItemReader())
                .writer(customerItemWriter())
                .listener(new ChunkListener())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("dbJob" + UUID.randomUUID().toString())
                .start(step1())
                .build();
    }

    @Bean(name = "customerDataSource")
    @ConfigurationProperties(prefix="datasource.customer-db")
    public DataSource customerDataSource() {
        return DataSourceBuilder.create().build();
    }
}
