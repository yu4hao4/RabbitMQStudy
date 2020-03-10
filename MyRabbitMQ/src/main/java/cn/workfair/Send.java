package cn.workfair;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-06-12:25
 */
public class Send {

    private static final String QUEUE_NAME = "test_work_queue";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        //获取连接
        Connection connection = ConnectionUtils.getConnection();

        //获取 channel
        Channel channel = connection.createChannel();

        //声明队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // 每个消费者 发送确认消息之前，消息队列不发送下一个消息到消费者，一次只处理一个消息
        // 限制发送给同一个消费者 不得超过一条消息
        int prefetcthCount = 1;
        channel.basicQos(prefetcthCount);

        for (int i = 0; i < 50; i++) {
            String msg = " hello "+i;
            System.out.println("[WQ] send " +msg);
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            Thread.sleep(i*5);
        }

        channel.close();
        connection.close();
    }
}
