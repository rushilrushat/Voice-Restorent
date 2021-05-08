package com.rushil.voicerestaurant.model;

public class OrderItemModel {
    String i_id;
    String o_id;
    String status;
    int quantity;
    double totalPrice;
    String itemName;
    String userId;




    public OrderItemModel() {

    }

    public OrderItemModel(String uId, String o_id, String i_id, String status, int quantity, double totalPrice, String itemName) {
        this.o_id = o_id;
        this.i_id = i_id;
        this.status = status;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.itemName = itemName;
        this.userId = uId;
    }


    public String getO_id() {
        return o_id;
    }

    public void setO_id(String o_id) {
        this.o_id = o_id;
    }

    public String getI_id() {
        return i_id;
    }

    public void setI_id(String i_id) {
        this.i_id = i_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
