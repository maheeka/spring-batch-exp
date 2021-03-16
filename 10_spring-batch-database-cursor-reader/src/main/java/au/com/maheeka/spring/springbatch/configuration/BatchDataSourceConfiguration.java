package au.com.maheeka.spring.springbatch.configuration;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Configuration
public class BatchDataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix="datasource.batch-db")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

}
