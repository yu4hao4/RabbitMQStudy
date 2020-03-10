package cn.simple;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者，发送消息
 * @author 喻浩
 * @create 2020-03-06-0:06
 */
public class Send {
    private static final String QUEUE_NAME="test_simple_queue";
    public static void main(String[] args) throws IOException, TimeoutException {
        //获取一个连接
        Connection connection = ConnectionUtils.getConnection();

        //从连接中获取一个 channel
        Channel channel = connection.createChannel();

        //创建队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String msg = "hello simple! 2";

        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());

        System.out.println("sen msg ： " + msg);

        channel.close();
        connection.close();
    }
}
