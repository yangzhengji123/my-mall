package com.xiaomimall.exception;


/**
 * 重复异常类
 * 该异常用于表示某个操作违反了唯一性约束，例如尝试创建一个已经存在的资源。
 */
public class DuplicateException extends RuntimeException {

    public DuplicateException(String message) {
        super(message);
    }
}