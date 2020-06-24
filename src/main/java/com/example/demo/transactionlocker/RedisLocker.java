package com.example.demo.transactionlocker;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLocker  implements DistributedLocker{
    @Autowired
    RedissonClient redissonClient;


    public static Logger logger = LoggerFactory.getLogger(RedisLocker.class);
    private final static String LOCKER_PREFIX = "lock:";



    @Override
    public <T> T lock(String resourceName, AquiredLockWorker<T> worker) throws InterruptedException, UnableToAquireLockException, Exception {

        return lock(resourceName, worker, 100);
    }

    @Override
    public <T> T lock(String resourceName, AquiredLockWorker<T> worker, int lockTime) throws UnableToAquireLockException, Exception {
        RLock mylock = redissonClient.getLock(LOCKER_PREFIX + resourceName);
        //5秒内尝试获取资源锁，如果获取到了创建一个生命周期为lockTime的锁
        boolean success = mylock.tryLock(5, lockTime, TimeUnit.SECONDS);
        if (success) {
            logger.debug("-----------------get a locker----------------------");
            try {
                return worker.invokeAfterLockAcquire();
            } finally {
                mylock.unlock();
            }
        }else{
            logger.debug("-----------------can not get locker----------------------");
        }
        throw new UnableToAquireLockException();
    }
}
