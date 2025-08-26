package com.xiaomimall.service;

import com.xiaomimall.dto.SeckillActivityDTO;
import com.xiaomimall.dto.SeckillResultDTO;
import com.xiaomimall.entity.PaymentType;

import java.util.List;

/**
 * 秒杀服务接口
 * 定义秒杀相关操作
 */
public interface SeckillService {
    SeckillResultDTO executeSeckill(Long userId, Long seckillId, PaymentType paymentType);//执行秒杀

    List<SeckillActivityDTO> getActiveSeckills();//获取进行中的秒杀活动

    List<SeckillActivityDTO> getUpcomingSeckills();//获取即将进行的秒杀活动

    SeckillActivityDTO createSeckillActivity(SeckillActivityDTO activityDTO);//创建秒杀活动

    SeckillActivityDTO updateSeckillActivity(Long id, SeckillActivityDTO activityDTO);

    SeckillActivityDTO updateSeckillStock(Long activityId, Integer newStock);
    void deleteSeckillActivity(Long id);
}