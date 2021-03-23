package au.com.maheeka.spring.springbatch.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerWrite {
    private long id;
    private String firstName;
    private String lastName;
    private int age;
}
