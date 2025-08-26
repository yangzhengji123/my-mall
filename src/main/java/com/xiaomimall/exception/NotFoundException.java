package com.xiaomimall.exception;

/**
 * 自定义异常类：NotFoundException
 * 用于表示资源未找到的异常情况
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);//传递错误信息
    }
}