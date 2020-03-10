# 3.消息应答与消息持久化

    boolean autoAck = false
    channel.basicConsume(QUEUE_NAME,autoAck,consumer);

boolean autoAck = true;（自动确认模式）一旦 rabbitmq 将消息分发给消费者，就会从内存中删除；
这种情况下，如果杀死赈灾执行的消费者就会丢失正在处理的消息

boolean autoAck = false;（手动模式），如果有一个消费者挂掉，就会交付给其他消费者， rabbitmq 支持消息应答，消费者发送一个消息应答，告诉 rabbitmq 这个消息我已经处理完成 你可以删除了，然后 rabbitmq 删除内存中的消息

消息应答默认是打开的

如果 rabbitmq 挂了，消息依然会丢失

## 消息的持久化
    //声明队列
    boolean durable = false;
    channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

我们将程序中的 boolean durable = false; 改成 true；是不可以的，尽管代码是正确的，他也不会运行成功，因为我们已经定义了一个叫 test_work_queue  这个 queue 是未持久化的，rabbitmq 不允许重新定义（不同参数）一个已经存在的队列


# 4. 订阅模式 publish/subscribe
## 模型
**解读：** 
  1. 一个生产者，多个消费者
  2. 每一个消费者都有自己的队列
  3. 生产者没有直接把消息发送到队列，而是发到了交换机 转换器 exchange
  4. 每个队列都要绑定到交换机上
  5. 生产者发送的消息，经过交换机，到达队列 就能实现 一个消息被多个消费者消费

注册->邮件->短信

```java
public class Send {
    private static final String EXCHANGE_NAME = "test_exchange_fanout";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        //声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");//分发

        //发送消息
        String msg = "hello ps ";

        channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());

        System.out.println("Send: "+msg);

        channel.close();
        connection.close();
    }
}
```

交换机没有存储的功能，在 rabbitmq 里面只有队列有存储功能，如果没有队列绑定到这个交换机，那么，数据就会丢失

## 消费者1
```java
public class Recv1 {

    private static final String QUEUE_NAME = "test_queue_fanout_email";
    private static final String EXCHANGE_NAME = "test_exchange_fanout";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        //队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //绑定队列到交换机 转换器
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        channel.basicQos(1);// 保证一次只发一个

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
```

## 消费者2
```java
public class Recv2 {

    private static final String QUEUE_NAME = "test_queue_fanout_sms";
    private static final String EXCHANGE_NAME = "test_exchange_fanout";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        //队列声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //绑定队列到交换机 转换器
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        channel.basicQos(1);// 保证一次只发一个

        //定义一个消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            //消息到达触发这个方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] Recv msg : "+msg);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("[2] done ");
                    //手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;//自动应答  改成 false
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);

    }
}
```

# 5. Exchange(交换机 转换器)

一方面是接受生产者的消息，另一方面是向队列推送消息

匿名转发 ""

fanout(不处理路由键)  只要经过交换器的消息，都会发送到绑定的队列上面

Direct(处理路由键)

## 路由模式
生产者
```java
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
```

消费者1
```java
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
```

消费者2
```java
public class Recv2 {

    private static final String EXCHANGE_NAME = "test_exchange_direct";
    private static final String QUEUE_NAME = "test_queue_direct_2";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);

        channel.basicQos(1);

        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "error");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "info");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "warning");

        //定义一个消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            //消息到达触发这个方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] Recv msg : "+msg);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("[2] done ");
                    //手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;//自动应答  改成 false
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);
    }
}
```

# 6. Topic Exchange

将路由键和某模式匹配

\#号 匹配一个或者多个<br/>
\*号 匹配一个

商品：发布 删除 修改 查询 ...

生产者
```java
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
```

消费者1
```java
public class Recv1 {

    private static final String EXCHANGE_NAME = "test_exchange_topic";
    private static final String QUEUE_NAME = "test_queue_topic_1";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "goods.add");

        channel.basicQos(1);


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
```

消费者2
```java
public class Recv2 {

    private static final String EXCHANGE_NAME = "test_exchange_topic";
    private static final String QUEUE_NAME = "test_queue_topic_2";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "goods.#");

        channel.basicQos(1);


        //定义一个消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            //消息到达触发这个方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println("[2] Recv msg : "+msg);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("[2] done ");
                    //手动回执
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        boolean autoAck = false;//自动应答  改成 false
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);
    }
}
```

## Rabbitmq 的消息确认机制（事务+confirm）

在 rabbitmq 中 我们可以通过持久化数据 解决 rabbitmq 服务器异常 的数据丢失问题

问题：生产者将消息发送出去之后，消息到底有没有到达 rabbitmq 服务器，默认的情况是不知道的

两种方式解决：
    AMQP 实现了事务计支
    Confirm 模式
    
## 事务机制
txSelect txCommit txRollback

- txSelect:用户将当前 channel 设置成 transaction 模式
- txCommit:用于提交事务
- txRollback:回滚事务

生产者
```java
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
```

消费者
```java
public class TxRecv {
    private static final String QUEUE_NAME = "test_queue_tx";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("recv[tx] msg:"+ new String(body, StandardCharsets.UTF_8) );
            }
        });
    }
}
```
这种模式还是很耗时的，采用这种方式，降低了 Rabbitmq 的消息吞吐量

## Confirm模式

### 生产者端 confirm 模式的实现原理

生产者将信道设置成 confirm 模式，一旦信道进入 confirm 模式，所有在该信道上发布的消息都会被指派唯一的 ID（从1开始），一旦消息被投递到所有匹配的队列只会，broker 就会发送一个确认给生产者（包含消息的唯一ID），这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会将消息写入磁盘后发出，broker 回传给生产者的确认消息中 deliver-tag 域包含了确认消息的序列号，此外 broker 也可以设置 basic.ack 的 multiple 域，表示到这个序列号之前的所有消息都已经得到了处理

confirm 模式最大的好处在于他是异步的，不用等待

出现异常会返回 Nack 消息

开启 confirm 模式：
    channel.confirmSelect();

编程模式：

    1. 普通  发一条  调用 waitForConfirms()
    2. 批量的  发一批  调用 waitForConfirms()
    3. 异步  confirm  模式：提供一个回调方法，有监听函数在监听

### Confirm 单条

```java
public class Send1 {
    private static final String QUEUE_NAME = "test_queue_confirm1";
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Connection connection = ConnectionUtils.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //生产者调用 confirmSelect 将 channel 设置为 confirm 模式
        // 注意：如果已经设置了事务模式，就不要再设置 confirm 模式
        channel.confirmSelect();

        String msgString = "hello confirm message!";
        channel.basicPublish("", QUEUE_NAME, null, msgString.getBytes());

        if (!channel.waitForConfirms()){
            System.out.println("message send failed");
        }else {
            System.out.println("message send ok");
        }

        channel.close();
        connection.close();
    }
}
```

### Confirm 批量

```java
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
```



### 异步模式

Channel 对象提供的 ConfirmListener() 回调方法只包含 deliveryTag（当前 Channel 发出的消息序号），我们需要自己为每一个 Channel 维护一个 unconfirm 的消息序号集合，每 publish 一条数据，集合中元素加 1，每回调一次 handleAck 方法，unconfirm 集合删掉相应的一条（multiple=false）或多条（multiple=true）记录，从程序运行效率上看，这个 unconfirm 集合最好采用有序集合 SortedSet 存储结构
