package cn.routing;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-20:42
 */
public class Send {
    private static final String EXCHANGE_NAME = "test_exchange_direct";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        //声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");//分发

        String msg = "hello direct!";

        String routingKey = "info";
        channel.basicPublish(EXCHANGE_NAME, routingKey,null,msg.getBytes());

        System.out.println("Send "+msg);

        channel.close();
        connection.close();
    }

}
