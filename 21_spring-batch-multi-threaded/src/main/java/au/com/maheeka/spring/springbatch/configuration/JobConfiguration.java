package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
    public ItemWriter<Customer> customerItemWriter() {
        return items -> items.forEach(item -> log.info(item.toString()));
    }

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(10);
        return executor;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Customer, Customer>chunk(10)
                .reader(customerItemReader())
                .writer(customerItemWriter())
                .listener(new ChunkListener())
               // .taskExecutor(new SimpleAsyncTaskExecutor()) //this executor does not reuse threads
                .taskExecutor(executor()) //this executor reuses threads. Use for executing large number of short lived tasks
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("multi-threaded-job-2" + UUID.randomUUID())
                .start(step1())
                .build();
    }
}
