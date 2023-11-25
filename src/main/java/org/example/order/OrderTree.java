package org.example.order;

import java.util.*;

public class OrderTree {
    TreeMap<Double, LinkedList<Order>> orderTree = new TreeMap<>();
    HashMap<String, Order> orderMap = new HashMap<>();
    int volume = 0;

    public void addOrder(Order order) {
        String orderId = order.getOrderId();
        double price = order.getPrice();

        orderMap.put(orderId, order);
        volume += order.getQuantity();

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
        volume -= order.getQuantity();
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
        builder.append("\n" + "Price, Quantity, Orders" + "\n");
        for (Map.Entry<Double, LinkedList<Order>> entry : orderTree.entrySet()) {
            int nOrdersAtPriceLevel = entry.getValue()
                    .stream()
                    .map(o -> o.getQuantity())
                    .reduce(0, Integer::sum);
            builder.append(entry.getKey() + ", " + nOrdersAtPriceLevel + ", " + entry.getValue().size());
            builder.append("\n");
        }

        return builder.toString();
    }
}