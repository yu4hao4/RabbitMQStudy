package cn.confirm;

import cn.util.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-09-23:00
 * 批量模式
 */
public class Send3 {
    private static final String QUEUE_NAME = "test_queue_confirm3";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //生产者调用 confirmSelect 将 channel 设置为 confirm 模式
        // 注意：如果已经设置了事务模式，就不要再设置 confirm 模式
        channel.confirmSelect();

        //未确认的消息标识
        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<>());

        //通道添加监听
        channel.addConfirmListener(new ConfirmListener() {
            //handNack 有问题的
            @Override
            public void handleAck(long l, boolean b) throws IOException {
                if (b){
                    System.out.println("----handleNack---multiple");
                    confirmSet.headSet(l+1).clear();
                }else {
                    System.out.println("----handleNack---multiple----false");
                    confirmSet.remove(l);
                }
            }

            //没有问题的 handAck
            @Override
            public void handleNack(long l, boolean b) throws IOException {
                if (b){
                    System.out.println("---handleAck---multiple");
                    confirmSet.headSet(l+1).clear();
                }else {
                    System.out.println("---handleAck---multiple---false");
                    confirmSet.remove(l);
                }
            }
        });

        String msgString = "hello sssss!";

        while (true){
            long seqNo = channel.getNextPublishSeqNo();
            channel.basicPublish("", QUEUE_NAME, null, msgString.getBytes());
            confirmSet.add(seqNo);
        }

    }
}
