package au.com.maheeka.spring.springbatch.processor;

import au.com.maheeka.spring.springbatch.exceptions.CustomerRetryableException;
import au.com.maheeka.spring.springbatch.model.Customer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class SkipItemProcessor implements ItemProcessor<Customer, Customer> {

    @Setter
    @Getter
    private boolean skip = false;
    private int attemptCount = 0;

    @Override
    public Customer process(Customer customer) {
        log.info(">>>>> Processing item {}", customer.getId());

        if (skip && customer.getId() == 42) {
            attemptCount++;

            log.info(">>>>> Processing of item {} failed", customer.getId());
            throw new CustomerRetryableException(">>>>> Process failed. Attempt " + attemptCount);
        } else {
            log.info(">>>>> Process without retry item {}", customer.getId());
            return map(customer);
        }
    }

    private Customer map(Customer customer) {
        return Customer.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName().toUpperCase())
                .lastName(customer.getLastName())
                .birthdate(customer.getBirthdate())
                .build();
    }
}
