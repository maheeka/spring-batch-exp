package au.com.maheeka.spring.springbatch.processor;

import au.com.maheeka.spring.springbatch.model.Customer;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

public class CustomerNameValidatingProcessor implements Validator<Customer> {

    @Override
    public void validate(Customer customer) throws ValidationException {
        if (customer.getFirstName().startsWith("A")) {
            throw new ValidationException("Name shouldn't start with A");
        };
    }
}
