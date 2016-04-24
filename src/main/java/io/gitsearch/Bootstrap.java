package io.gitsearch;

import io.gitsearch.messagequeue.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component("Bootstrap")
@ComponentScan(basePackages = "io.gitsearch")
@PropertySource("classpath:config.properties")
public class Bootstrap {
    @Autowired
    private MessageService messageService;

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(Bootstrap.class);
        Bootstrap bootstrap = (Bootstrap) context.getBean("Bootstrap");
        bootstrap.start();
    }

    private void start() {
        messageService.listenToMessageQueue();
    }
}
