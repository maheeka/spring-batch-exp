package au.com.maheeka.spring.springbatch.configuration;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;

import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.messaging.PollableChannel;

@Configuration
public class IntegrationConfiguration {

    @Bean
    public MessagingTemplate messageTemplate() {
        MessagingTemplate messagingTemplate = new MessagingTemplate(outboundRequests());
        messagingTemplate.setReceiveTimeout(60000000l); //set to a high value to avoid timeouts
        return messagingTemplate;
    }

    @Bean
    public DirectChannel outboundRequests() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundRequests")
    // listen on the outbound request channel, sends a message to partition requests to send work to slaves
    public AmqpOutboundEndpoint amqpOutboundEndpoint(AmqpTemplate template) {
        AmqpOutboundEndpoint endpoint = new AmqpOutboundEndpoint(template);
        endpoint.setExpectReply(true);
        endpoint.setOutputChannel(inboundRequests());
        endpoint.setRoutingKey("partition.requests"); //listen for work in this queue
        return endpoint;
    }

    @Bean
    public Queue requestQueue() {
        return new Queue("partition.requests", false);
    }

    @Bean
    @Profile("slave")
    // listens to messages in RabbitMQ and directs to internal components in Slave JVM for processing
    public AmqpInboundChannelAdapter inbound(SimpleMessageListenerContainer listenerContainer){
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setOutputChannel(inboundRequests()); //send messages to this after receiving from RabbitMQ
        adapter.afterPropertiesSet();
        return adapter;
    }

    @Bean
    // manages the listening
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames("partition.requests");
        container.setAutoStartup(false); // multiple slave jvms will run but only one will pickup
        return container;
    }

    @Bean
    public PollableChannel outboundStaging() {
        return new NullChannel(); //responses are dropped with null channel because we need it
    }

    @Bean
    //listened to by stepexecutionrequest handler and then return to outboundStaging
    public QueueChannel inboundRequests() {
        return new QueueChannel();
    }

}
