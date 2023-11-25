package org.example.order;

import org.example.enums.OrderAction;
import org.example.enums.OrderType;
import org.example.exception.OrderException;

import java.time.Instant;

public class Order {

    private Instant timestamp;
    private int quantity;
    private double price;
    private String orderId;
    private String tradeId;
    private OrderAction side;
    private String stockName;
    private OrderType type;

    public Order(Instant timestamp, int quantity, double price, String orderId, String tradeId, OrderAction side, String stockName, OrderType type) {
        this.timestamp = timestamp;
        this.quantity = quantity;
        this.price = price;
        this.orderId = orderId;
        this.tradeId = tradeId;
        this.side = side;
        this.stockName = stockName;
        this.type = type;

        if (type == OrderType.MARKET) {
            if (side == OrderAction.BID) {
                this.price = Double.MAX_VALUE;
            } else {
                this.price = Double.MIN_VALUE;
            }
        }
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new OrderException("Quantity cannot be less than 1");
        }
        this.quantity = newQuantity;
    }


    public int getQuantity() {
        return quantity;
    }


    public double getPrice() {
        return price;
    }


    public String getOrderId() {
        return orderId;
    }


    public OrderAction getSide() {
        return side;
    }


    public String toString() {
        return "order.Order ID" + this.orderId + " quantity " + this.quantity + " side " + this.side;
    }

}