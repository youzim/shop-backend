package com.shop.dto;

import com.shop.entity.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public class OrderDTO {
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal payAmount;
    private String addressName;
    private String addressPhone;
    private String addressDetail;
    private List<OrderItem> items;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getFreight() { return freight; }
    public void setFreight(BigDecimal freight) { this.freight = freight; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }
    public String getAddressPhone() { return addressPhone; }
    public void setAddressPhone(String addressPhone) { this.addressPhone = addressPhone; }
    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}