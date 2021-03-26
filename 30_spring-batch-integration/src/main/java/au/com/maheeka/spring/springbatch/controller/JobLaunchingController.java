package au.com.maheeka.spring.springbatch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class JobLaunchingController {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job job;

    @Autowired
    private DirectChannel replies;

    @Autowired
    private MessageChannel requests;

    @RequestMapping(value = "/job", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void launch(@RequestParam("name") String name) throws Exception {
        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addString("name", name)
                        .toJobParameters();
        JobLaunchRequest launchRequest = new JobLaunchRequest(this.job, jobParameters);

        replies.subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                JobExecution payload = (JobExecution) message.getPayload();

                System.out.println(">> " + payload.getJobInstance().getJobName() + " resulted in " + payload.getStatus());
            }
        });

        requests.send(MessageBuilder.withPayload(launchRequest).setReplyChannel(replies).build());
    }
}
