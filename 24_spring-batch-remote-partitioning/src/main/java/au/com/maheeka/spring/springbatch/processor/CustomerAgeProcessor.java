package au.com.maheeka.spring.springbatch.processor;

import au.com.maheeka.spring.springbatch.model.Customer;
import au.com.maheeka.spring.springbatch.model.CustomerWrite;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.batch.item.ItemProcessor;

public class CustomerAgeProcessor implements ItemProcessor<Customer, CustomerWrite> {
    @Override
    public CustomerWrite process(Customer customer) throws Exception {
        LocalDate birthdate = null;
        Date customerBirthdate = customer.getBirthdate();
        if (customer.getBirthdate() instanceof java.sql.Date) {
            birthdate = ((java.sql.Date) customerBirthdate).toLocalDate();
        } else {
            birthdate = customer.getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        return CustomerWrite.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName().toUpperCase())
                .lastName(customer.getLastName())
                .age(age)
                .build();
    }
}
