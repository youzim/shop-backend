package com.shop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.entity.Goods;
import com.shop.mapper.GoodsMapper;
import com.shop.service.impl.GoodsService;
import org.springframework.stereotype.Service;

@Service  // ← 必须有这个
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
}