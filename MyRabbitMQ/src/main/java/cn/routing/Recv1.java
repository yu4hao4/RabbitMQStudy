package cn.routing;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-20:46
 */
public class Recv1 {

    private static final String EXCHANGE_NAME = "test_exchange_direct";
    private static final String QUEUE_NAME = "test_queue_direct";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

        channel.basicQos(1);

        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");

        //定义一个消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            //消息到达触发这个方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[1] Recv msg : "+msg);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("[1] done ");
                    //手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;//自动应答  改成 false
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);
    }
}
