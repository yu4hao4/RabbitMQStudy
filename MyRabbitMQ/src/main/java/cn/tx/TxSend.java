package cn.tx;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-22:11
 */
public class TxSend {

    private static final String QUEUE_NAME = "test_queue_tx";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

        String msgString = "hello tx message!";

        try{
            channel.txSelect();
            channel.basicPublish("", QUEUE_NAME, null, msgString.getBytes());

            int x = 1/0;

            System.out.println("send: "+msgString);
            channel.txCommit();
        } catch (Exception e){
            channel.txRollback();
            System.out.println("send message rollback");
        }

        channel.close();
        connection.close();
    }
}
