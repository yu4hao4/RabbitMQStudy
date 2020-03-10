package cn.confirm;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-23:00
 * 批量模式
 */
public class Send2 {
    private static final String QUEUE_NAME = "test_queue_confirm1";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //生产者调用 confirmSelect 将 channel 设置为 confirm 模式
        // 注意：如果已经设置了事务模式，就不要再设置 confirm 模式
        channel.confirmSelect();

        String msgString = "hello confirm message batch!";
        //批量发送
        for (int i = 0; i < 10; i++) {
            channel.basicPublish("", QUEUE_NAME, null, msgString.getBytes());
        }

        //确认
        if (!channel.waitForConfirms()){
            System.out.println("message send failed");
        }else {
            System.out.println("message send ok");
        }

        channel.close();
        connection.close();
    }
}
