package au.com.maheeka.spring.springbatch.configuration;

import au.com.maheeka.spring.springbatch.listener.ChunkListener;
import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerRowMapper;
import au.com.maheeka.spring.springbatch.model.CustomerWrite;
import au.com.maheeka.spring.springbatch.processor.CustomerAgeProcessor;
import au.com.maheeka.spring.springbatch.processor.FilteringItemProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.batch.item.support.CompositeItemProcessor;
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
    public ItemWriter<CustomerWrite> customerItemWriter() {
        JdbcBatchItemWriter<CustomerWrite> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(customerWriteDataSource);
        itemWriter.setSql("INSERT INTO CUSTOMER VALUES (:id, :firstName, :lastName, :age)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        return itemWriter;
    }

    @Bean
    public CompositeItemProcessor compositeItemProcessor() throws Exception {
        List<ItemProcessor> delegates = new ArrayList<>();
        delegates.add(new FilteringItemProcessor());
        delegates.add(new CustomerAgeProcessor());

        CompositeItemProcessor compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(delegates);
        compositeItemProcessor.afterPropertiesSet();

        return compositeItemProcessor;
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .chunk(10)
                .reader(customerItemReader())
                .processor(compositeItemProcessor())
                .writer(customerItemWriter())
                .listener(new ChunkListener())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("dbJob" + UUID.randomUUID().toString())
                .start(step1())
                .build();
    }
}
