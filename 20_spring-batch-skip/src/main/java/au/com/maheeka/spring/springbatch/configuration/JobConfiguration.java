package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.exceptions.CustomerRetryableException;
import au.com.maheeka.spring.springbatch.processor.SkipItemProcessor;
import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
import au.com.maheeka.spring.springbatch.writer.SkipItemWriter;
import java.util.HashMap;
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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
    public JdbcPagingItemReader<Customer> customerItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.customerDataSource);
        reader.setFetchSize(10);
        reader.setRowMapper(new CustomerRowMapper());

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);

        return reader;
    }

    @Bean
    @StepScope
    public SkipItemProcessor customerItemProcessor(@Value("#{jobParameters['skip']}")String skip) {
        SkipItemProcessor processor = new SkipItemProcessor();
        processor.setSkip(StringUtils.hasText(skip) && skip.equalsIgnoreCase("processor"));
        return processor;
    }

    @Bean
    @StepScope
    public SkipItemWriter customerItemWriter(@Value("#{jobParameters['skip']}")String skip) {
        SkipItemWriter writer = new SkipItemWriter();
        writer.setSkip(StringUtils.hasText(skip) && skip.equalsIgnoreCase("writer"));
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader())
                .processor(customerItemProcessor(null))
                .writer(customerItemWriter(null))
                .listener(new ChunkListener())
                .faultTolerant()
                .skip(CustomerRetryableException.class)
                .skipLimit(15)
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("skipJob")
                .start(step1())
                .build();
    }
}
