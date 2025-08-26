package com.xiaomimall.exception;

/**
 * 秒杀异常
 * 用于秒杀相关的异常情况
 */
public class SeckillException extends RuntimeException {
    public SeckillException(String message) {
        super(message);
    }
    
    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}