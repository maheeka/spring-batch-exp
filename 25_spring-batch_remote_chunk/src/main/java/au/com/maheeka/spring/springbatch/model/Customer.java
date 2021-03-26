package au.com.maheeka.spring.springbatch.model;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer implements Serializable {
    private long id;
    private String firstName;
    private String lastName;
    private Date birthdate;
}
