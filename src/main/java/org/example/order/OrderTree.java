package org.example.order;

import java.util.*;

public class OrderTree {
    private TreeMap<Double, LinkedList<Order>> orderTree = new TreeMap<>();
    private HashMap<String, Order> orderMap = new HashMap<>();

    public void addOrder(Order order) {
        String orderId = order.getOrderId();
        double price = order.getPrice();
        orderMap.put(orderId, order);
        orderTree.computeIfPresent(price, (k, v) -> {
            v.add(order);
            return v;
        });

        orderTree.computeIfAbsent(price, v -> {
            LinkedList<Order> orders = new LinkedList<>();
            orders.add(order);
            return orders;
        });
    }

    public void deleteOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null) return;
        double price = order.getPrice();
        LinkedList<Order> priceList = orderTree.get(price);
        priceList.remove(order);
        if (priceList.isEmpty()) {
            orderTree.remove(price);
        }
        orderMap.remove(orderId);
    }

    public double getHighestPrice() {
        return orderTree.isEmpty() ? Double.MAX_VALUE : orderTree.lastKey();
    }

    public double getLowestPrice() {
        return orderTree.isEmpty() ? Double.MAX_VALUE : orderTree.firstKey();
    }


    public List<Order> getMaxPricedOrderList() {
        if (orderTree.isEmpty()) return Collections.unmodifiableList(new LinkedList<>());
        return List.copyOf(orderTree.lastEntry().getValue());
    }

    public List<Order> getMinPricedOrderList() {
        if (orderTree.isEmpty()) return Collections.unmodifiableList(new LinkedList<>());
        return List.copyOf(orderTree.firstEntry().getValue());
    }

    public boolean isNotEmpty() {
        return !orderTree.isEmpty();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n" + "Price, Quantity, Orders, Amount" + "\n");
        for (Map.Entry<Double, LinkedList<Order>> entry : orderTree.entrySet()) {
            int nOrdersAtPriceLevel = entry.getValue()
                    .stream()
                    .map(o -> o.getQuantity())
                    .reduce(0, Integer::sum);

            Double nAmountTotal = entry.getValue()
                    .stream()
                    .map(o -> o.getAmount())
                    .reduce(0.0, Double::sum);
            builder.append(entry.getKey() + ", " + nOrdersAtPriceLevel + ", " + entry.getValue().size() + ", " + nAmountTotal);
            builder.append("\n");
        }

        return builder.toString();
    }

    public int getOrderCount() {
        return orderMap.size();
    }
}