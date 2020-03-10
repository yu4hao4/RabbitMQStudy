package cn.simple;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 消费者获取消息
 * @author 喻浩
 * @create 2020-03-06-0:19
 */
public class Recv {
    private static final String QUEUE_NAME="test_simple_queue";
    public static void main(String[] args) throws IOException, TimeoutException {
        //获取一个连接
        Connection connection = ConnectionUtils.getConnection();

        //创建频道
        Channel channel = connection.createChannel();

        //队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //实现消费方法
        DefaultConsumer defaultConsumer =new DefaultConsumer(channel){
            //当接收到消息后 此方法将被调用
            /**
             * @param consumerTag  消费者标签，用来标识消费者的，在监听队列时设置channel.basicConsume
             * @param envelope 信封，通过envlope
             * @param properties
             * @param body
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                //交换机
//                String exchange= envelope.getExchange();
//                //消息id, mq在channel 中用来标识消息的id,可用于接收
//                long deliveryTag =envelope.getDeliveryTag();
                //消息内容
                String message=new String(body, StandardCharsets.UTF_8);
                System.out.println("receive message:"+message);
            }
        };

        //监听队列
        //1、队列名称
        //2、自动回复 ，当消费者收到消息后要告诉mq消息已接收，如果将此参数设置为true表示会自动回复mq,如果设置为false要通过编程实现回复
        //3、callback,消费方法，当消费者接收到消息要执行的方法
        channel.basicConsume(QUEUE_NAME,false,defaultConsumer);
    }
}
