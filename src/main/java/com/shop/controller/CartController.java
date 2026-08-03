package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.entity.Cart;
import com.shop.mapper.CartMapper;
import com.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @GetMapping("/list")
    public Result list(@RequestParam Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Cart> list = cartMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Cart cart) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", cart.getUserId())
                .eq("goods_id", cart.getGoodsId())
                .eq("spec", cart.getSpec());
        Cart existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setNum(existing.getNum() + cart.getNum());
            cartMapper.updateById(existing);
            return Result.success(existing);
        } else {
            cart.setChecked(1);
            cartMapper.insert(cart);
            return Result.success(cart);
        }
    }

    @PutMapping("/update")
    public Result update(@RequestBody Cart cart) {
        cartMapper.updateById(cart);
        return Result.success(cart);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Long id) {
        cartMapper.deleteById(id);
        return Result.success("删除成功");
    }
}