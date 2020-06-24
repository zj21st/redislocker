package com.example.demo.transactionlocker;

/**
 * 获取锁后需要处理的逻辑
 */
public interface AquiredLockWorker<T> {

    T invokeAfterLockAcquire() throws Exception;
}
