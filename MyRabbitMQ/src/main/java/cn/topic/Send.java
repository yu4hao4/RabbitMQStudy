package cn.topic;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-20:58
 */
public class Send {
    private static final String EXCHANGE_NAME = "test_exchange_topic";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        //exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        String msg = "商品...2";

//        String routingKey = "goods.add";
        String routingKey = "goods.delete";
        channel.basicPublish(EXCHANGE_NAME, routingKey,null,msg.getBytes());

        System.out.println("Send "+msg);

        channel.close();
        connection.close();
    }

}
