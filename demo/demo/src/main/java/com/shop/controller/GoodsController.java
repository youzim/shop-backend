package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.entity.Goods;
import com.shop.service.impl.GoodsService;
import com.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public Result<List<Goods>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (subCategoryId != null) {
            wrapper.eq("sub_category_id", subCategoryId);
        }
        wrapper.orderByDesc("sales");
        return Result.ok(goodsService.list(wrapper));
    }

    @GetMapping("/detail/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        return Result.ok(goodsService.getById(id));
    }

    @GetMapping("/search")
    public Result<List<Goods>> search(@RequestParam String keyword) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.like("title", keyword).or().like("subtitle", keyword);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("sales");
        return Result.ok(goodsService.list(wrapper));
    }
}