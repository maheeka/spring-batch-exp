package au.com.maheeka.spring.springbatch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Slf4j
public class JobLaunchingController {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job job;

    @RequestMapping(value = "/job", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String launch(@RequestParam("name") String name) throws Exception {
        Long id = this.jobOperator.start("job-stop-4", String.format("name=%s", name));
        log.info("id >>>>>> {}", id);
        return id.toString();
    }

    @RequestMapping(value = "/job/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void stop(@PathVariable("id") long id) throws Exception {
        this.jobOperator.stop(id);
    }
}
