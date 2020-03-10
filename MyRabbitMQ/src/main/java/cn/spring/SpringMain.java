package cn.spring;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 喻浩
 * @create 2020-03-10-10:48
 */
public class SpringMain {

//    @Autowired
//    private RabbitTemplate template;

    public static void main(String[] args) throws InterruptedException {
        AbstractApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:context.xml");
        //RabbiMQ 模板
        RabbitTemplate template = context.getBean(RabbitTemplate.class);
        //发送消息
        template.convertAndSend("hello world");
        //睡 1 s
        Thread.sleep(1000);
        context.destroy();
    }
}
