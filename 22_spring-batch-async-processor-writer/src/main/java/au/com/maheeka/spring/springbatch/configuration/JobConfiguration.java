package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


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
    public AsyncItemProcessor asyncItemProcessor() throws Exception {
        AsyncItemProcessor<Customer, Customer> itemProcessor = new AsyncItemProcessor<>();
        itemProcessor.setDelegate(itemProcessor());
        itemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor()); //use threadpooltaskexecutor
        itemProcessor.afterPropertiesSet();
        return itemProcessor;
    }

    @Bean
    public ItemProcessor itemProcessor() {
        return (ItemProcessor<Customer, Customer>) item -> {
            Thread.sleep(new Random().nextInt(10));
            return Customer.builder()
                    .id(item.getId())
                    .firstName(item.getFirstName().toUpperCase())
                    .lastName(item.getLastName().toUpperCase())
                    .birthdate(item.getBirthdate())
                    .build();
        };
    }

    @Bean
    public AsyncItemWriter asyncItemWriter() throws Exception {
        AsyncItemWriter<Customer> itemWriter = new AsyncItemWriter<>();
        itemWriter.setDelegate(itemWriter());
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    public ItemWriter itemWriter() {
        return items -> items.forEach(item -> log.info(item.toString()));
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader())
                .processor(asyncItemProcessor())
//                .processor(itemProcessor())
                .writer(asyncItemWriter())
//                .writer(itemWriter())
                .listener(new ChunkListener())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job" + UUID.randomUUID())
                .start(step1())
                .build();
    }
}
