package com.xiaomimall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 订单号生成器
 * 生成唯一的订单号
 */
public class OrderNoGenerator {
    
    /**
     * 生成订单号
     * 格式: yyyyMMddHHmmss + 6位随机数
     */
    public static String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        
        // 生成6位随机数
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000;
        
        return dateStr + randomNum;
    }
}