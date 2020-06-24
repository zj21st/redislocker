package com.example.demo.web;

import com.example.demo.transactionlocker.AquiredLockWorker;
import com.example.demo.transactionlocker.RedisLocker;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RestController
public class Hello {

    public static Logger logger = LoggerFactory.getLogger(RedisLocker.class);


    @Autowired
    RedisLocker lock;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 非事务锁
     * @param id   事务ID
     * @return
     */
    @GetMapping("/work/{id}")
    public String work(@PathVariable String id) {
        String message = "error";

        RLock mylock = redissonClient.getLock("RLock" + id);
        //3s内尝试获取资源锁，如果获取到了创建一个生命周期为20s的锁
        boolean success = false;
        try {
            success = mylock.tryLock(3, 20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            message = "加锁异常!"+e.getMessage();
            logger.warn(message);
        }
        if (success) {
            message ="获得业务编号"+id+"的锁，开始处理业务。20秒内会自动解锁";
            logger.debug(message);
        }else{
            message ="业务编号"+id+"的锁被占用,等待20秒后重试；也可以调用强制解锁API";
            logger.debug("message");
        }
        return message;
    }

    /**
     * 非事务锁 解锁
     * @param id    事务ID
     * @return
     */
    @GetMapping("/unlock/{id}")
    public String forceunlock(@PathVariable String id) {
        String message = "解锁请求已发送";
        RLock mylock = redissonClient.getLock("RLock" + id);
        mylock.forceUnlock();
        return message;
    }

    /*
    事务锁
     */
    @GetMapping("/getdistributedLocker/{id}")
    public String DistributedLocker(@PathVariable String id){

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(5);
        // 测试5个并发
        for (int i = 0; i < 5; ++i) {
            new Thread(new Worker(startSignal, doneSignal)).start();
        }
        startSignal.countDown(); // let all threads proceed
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("All processors done. Shutdown connection");
        return "transactionlocker";

    }


    class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;

        /**
         * Instantiates a new Worker.
         *
         * @param startSignal the start signal
         * @param doneSignal  the done signal
         */
        Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            try {
                startSignal.await();
                //尝试加锁
                lock.lock ("test", new AquiredLockWorker<Object>() {

                    @Override
                    public Object invokeAfterLockAcquire() {
                        doTask();
                        return "success";
                    }

                });
            } catch (Exception e) {
                logger.warn("获取锁出现异常", e);
            }
        }

        /**
         * Do task.
         */
        void doTask() {
            System.out.println(Thread.currentThread().getName() + " 抢到锁!");
            Random random = new Random();
            int _int = random.nextInt(200);
            System.out.println(Thread.currentThread().getName() + " sleep " + _int + "millis");
            try {
                Thread.sleep(_int);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " 释放锁!");
            doneSignal.countDown();
        }
    }


}

