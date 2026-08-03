package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.math.BigDecimal;

public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String title;
    private String spec;
    private BigDecimal price;
    private Integer num;
    private String image;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getGoodsId() { return goodsId; }
    public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getNum() { return num; }
    public void setNum(Integer num) { this.num = num; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}