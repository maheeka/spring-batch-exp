package au.com.maheeka.spring.springbatch.writer;

import au.com.maheeka.spring.springbatch.exceptions.CustomerRetryableException;
import au.com.maheeka.spring.springbatch.model.Customer;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class RetryItemWriter implements ItemWriter<Customer> {

    @Setter
    @Getter
    private boolean retry = false;
    private int attemptCount = 0;

    @Override
    public void write(List<? extends Customer> customers) {
        customers.forEach(customer -> {
            log.info(">>>>> Writing item {}", customer.getId());

            if (retry && customer.getId() == 84) {
                attemptCount++;

                if (attemptCount >= 5) {
                    log.info(">>>>> Successful write {}", customer.getId());
                    retry = false;
                    log.info(">>>>> {}", customer);
                } else {
                    log.info(">>>>> Writing of item {} failed", customer.getId());
                    throw new CustomerRetryableException("Write failed. Attempt : " + attemptCount);
                }
            } else {
                log.info(">>>>> Write without retries {}", customer.getId());
            }
        });
    }
}
