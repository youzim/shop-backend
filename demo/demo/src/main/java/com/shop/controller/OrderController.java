package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shop.dto.OrderDTO;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.OrderItemMapper;
import com.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @GetMapping("/list")
    public Result list(@RequestParam Long userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Order> list = orderMapper.selectList(wrapper);

        for (Order order : list) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setItems(items);
        }

        return Result.success(list);
    }
    /**
     * 根据订单号查询详情
     */
    @GetMapping("/detail/{orderNo}")
    public Result detail(@PathVariable String orderNo) {
        Order order = orderMapper.selectOne(
                new UpdateWrapper<Order>().eq("order_no", orderNo)
        );
        if (order == null) {
            return Result.error(400, "订单不存在");
        }
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        order.setItems(items);
        return Result.success(order);
    }
    @PostMapping("/create")
    @Transactional
    public Result create(@RequestBody OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserId(orderDTO.getUserId());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setFreight(orderDTO.getFreight());
        order.setPayAmount(orderDTO.getPayAmount());
        order.setAddressName(orderDTO.getAddressName());
        order.setAddressPhone(orderDTO.getAddressPhone());
        order.setAddressDetail(orderDTO.getAddressDetail());
        order.setStatus(0);
        orderMapper.insert(order);

        for (OrderItem item : orderDTO.getItems()) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return Result.success(order);
    }
}