package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
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
    public ItemWriter<? super Object> customerItemWriter() {
        JdbcBatchItemWriter<? super Object> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(customerWriteDataSource);
        itemWriter.setSql("INSERT INTO CUSTOMER VALUES (:id, :firstName, :lastName, :birthdate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        return itemWriter;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(customerItemReader())
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
}
