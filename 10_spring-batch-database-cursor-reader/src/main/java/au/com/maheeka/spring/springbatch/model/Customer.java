package au.com.maheeka.spring.springbatch.model;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {
    private long id;
    private String firstName;
    private String lastName;
    private Date birthdate;
}
