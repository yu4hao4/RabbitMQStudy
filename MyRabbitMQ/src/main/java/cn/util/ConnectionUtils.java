package cn.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author 喻浩
 * @create 2020-03-06-0:00
 */
public class ConnectionUtils {

    /**
     * 获取 mq 的连接
     * @return
     */
    public static Connection getConnection() throws IOException, TimeoutException {
        //定义一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();

        //设置服务地址
        factory.setHost("121.36.49.252");

        //AMQP 5672
        factory.setPort(5672);

        //vhost
        factory.setVirtualHost("/vhost_like_db");

        //用户名
        factory.setUsername("user");

        //密码
        factory.setPassword("user");

        return factory.newConnection();
    }
}
