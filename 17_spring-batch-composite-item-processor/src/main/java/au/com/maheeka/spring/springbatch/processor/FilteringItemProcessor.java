package au.com.maheeka.spring.springbatch.processor;

import au.com.maheeka.spring.springbatch.model.Customer;
import org.springframework.batch.item.ItemProcessor;

public class FilteringItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        if (customer.getFirstName().startsWith("A")) {
            return null;
        }
        return customer;
    }
}
