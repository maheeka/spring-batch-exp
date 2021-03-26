package au.com.maheeka.spring.springbatch.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerWrite implements Serializable {
    private long id;
    private String firstName;
    private String lastName;
    private int age;
}
