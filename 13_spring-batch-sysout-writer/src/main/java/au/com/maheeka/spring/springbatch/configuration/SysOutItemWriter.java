package au.com.maheeka.spring.springbatch.configuration;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class SysOutItemWriter implements ItemWriter<String> {

    @Override
    public void write(List<? extends String> items) throws Exception {
        log.info("Size of the chunk :::: {} :::: {} ", items.size(), items);
    }
}
