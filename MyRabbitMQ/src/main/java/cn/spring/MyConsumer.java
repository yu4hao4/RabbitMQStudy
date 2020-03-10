package cn.spring;

/**
 * @author 喻浩
 * @create 2020-03-10-11:08
 */
public class MyConsumer {

    //具体执行业务的方法
    public void listen(String foo){
        System.out.println("消费者："+foo);
    }
}
