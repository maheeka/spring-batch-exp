package au.com.maheeka.spring.springbatch.launcher;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledLauncher {

    @Autowired
    public JobOperator jobOperator;

    @Scheduled(fixedRate = 2000)
    public void runJob() {
        try {
            this.jobOperator.startNextInstance("scheduled-job-2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
