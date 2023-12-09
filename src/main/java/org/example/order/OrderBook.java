package org.example.order;

import org.example.enums.OrderAction;
import org.example.enums.OrderType;
import org.example.exception.OrderException;

import java.util.List;

public class OrderBook {

    private OrderTree buySide;
    private OrderTree sellSide;

    public OrderBook() {
        this.buySide = new OrderTree();
        this.sellSide = new OrderTree();
    }

    public void processOrder(Order order) {
        validateOrder(order);
        matchAndExecute(order);
        printOrderStatus();
    }

    private void validateOrder(Order order) {
        if ((OrderType.LIMIT == order.getType() && order.getQuantity() < 1) || (OrderType.MARKET == order.getType() && order.getAmount() <= 0.0))
            throw new OrderException("Quantity or amount of order cannot be less than 1 or 0.0 respectively!");
    }

    private void matchAndExecute(Order order) {
        OrderType orderType = order.getType();
        int quantity = order.getQuantity();
        double price = order.getPrice();
        double amount = order.getAmount();
        // process for BID
        if (order.getSide() == OrderAction.BID) {
            buySide.addOrder(order);
            while (quantity > 0 && orderType == OrderType.LIMIT && sellSide.isNotEmpty() && price >= sellSide.getLowestPrice()) {
                List<Order> minPricedOrderList = sellSide.getMinPricedOrderList();
                if (minPricedOrderList == null || minPricedOrderList.isEmpty()) break;
                quantity = executeLimitOrder(order, minPricedOrderList, quantity, buySide, sellSide);
            }

            while (amount > 0.0 && orderType == OrderType.MARKET && sellSide.isNotEmpty() && price >= sellSide.getLowestPrice()) {
                List<Order> minPricedOrderList = sellSide.getMinPricedOrderList();
                if (minPricedOrderList == null || minPricedOrderList.isEmpty()) break;
                amount = executeMarketOrder(order, minPricedOrderList, amount, buySide, sellSide);
            }
            // process for ASK
        } else {
            sellSide.addOrder(order);
            while (quantity > 0 && orderType == OrderType.LIMIT && buySide.isNotEmpty() && price <= buySide.getHighestPrice()) {
                List<Order> maxPricedOrderList = buySide.getMaxPricedOrderList();
                if (maxPricedOrderList == null || maxPricedOrderList.isEmpty()) break;
                quantity = executeLimitOrder(order, maxPricedOrderList, quantity, sellSide, buySide);
            }

            while (amount > 0.0 && orderType == OrderType.MARKET && buySide.isNotEmpty() && price <= buySide.getHighestPrice()) {
                List<Order> maxPricedOrderList = buySide.getMaxPricedOrderList();
                if (maxPricedOrderList == null || maxPricedOrderList.isEmpty()) break;
                amount = executeMarketOrder(order, maxPricedOrderList, amount, sellSide, buySide);
            }
        }

    }


    private int executeLimitOrder(Order findingOrder, List<Order> maxPricedOrderList, int findingOrderQuantity, OrderTree findingTree, OrderTree matchingTree) {

        for (Order matchedOrder : maxPricedOrderList) {
            if (matchedOrder.getType() == OrderType.MARKET) {
                double findingOrderPrice = findingOrder.getPrice();
                double findingOrderTotalAmount = findingOrderPrice * findingOrderQuantity;
                double matchingOrderAmount = matchedOrder.getAmount();

                if (matchingOrderAmount >= findingOrderTotalAmount) {
                    findingTree.deleteOrder(findingOrder.getOrderId());

                    if (matchingOrderAmount == findingOrderTotalAmount) {
                        matchingTree.deleteOrder(matchedOrder.getOrderId());
                    } else {
                        matchedOrder.updateAmount(matchingOrderAmount - findingOrderTotalAmount);
                    }
                    findingOrderQuantity = 0;
                } else if (matchingOrderAmount >= findingOrderPrice) {
                    int matchedQuantity = new Double(matchingOrderAmount / findingOrderPrice).intValue();
                    findingOrder.updateQuantity(findingOrderQuantity - matchedQuantity);
                    matchingTree.deleteOrder(matchedOrder.getOrderId());
                    findingOrderQuantity = findingOrderQuantity - matchedQuantity;
                }
            }

            if (matchedOrder.getType() == OrderType.LIMIT) {
                int matchingOrderQuantity = matchedOrder.getQuantity();
                if (findingOrderQuantity <= matchingOrderQuantity) {
                    findingTree.deleteOrder(findingOrder.getOrderId());
                    if (findingOrderQuantity == matchingOrderQuantity) {
                        matchingTree.deleteOrder(matchedOrder.getOrderId());
                    } else {
                        matchedOrder.updateQuantity(matchingOrderQuantity - findingOrderQuantity);
                    }
                    findingOrderQuantity = 0;
                } else {
                    matchingTree.deleteOrder(matchedOrder.getOrderId());
                    findingOrder.updateQuantity(findingOrderQuantity - matchingOrderQuantity);
                    findingOrderQuantity = findingOrderQuantity - matchingOrderQuantity;
                }
            }
        }
        return findingOrderQuantity;
    }

    private double executeMarketOrder(Order findingOrder, List<Order> minPricedOrderList, double findingOrderAmount, OrderTree findingTree, OrderTree matchingTree) {

        for (Order matchedOrder : minPricedOrderList) {
            if (matchedOrder.getType() == OrderType.MARKET) {
                double matchingOrderAmount = matchedOrder.getAmount();
                if (findingOrderAmount <= matchingOrderAmount) {
                    findingTree.deleteOrder(findingOrder.getOrderId());
                    if (findingOrderAmount == matchingOrderAmount) {
                        matchingTree.deleteOrder(matchedOrder.getOrderId());
                    } else {
                        matchedOrder.updateAmount(matchingOrderAmount - findingOrderAmount);
                    }
                    findingOrderAmount = 0.0;
                } else {
                    matchingTree.deleteOrder(matchedOrder.getOrderId());
                    findingOrder.updateAmount(findingOrderAmount - matchingOrderAmount);
                    findingOrderAmount = findingOrderAmount - matchingOrderAmount;
                }
            }


            if (matchedOrder.getType() == OrderType.LIMIT) {
                double matchingOrderPrice = matchedOrder.getPrice();
                int matchingOrderQuantity = matchedOrder.getQuantity();
                double matchedSideTotalAmount = matchingOrderPrice * matchingOrderQuantity;
                if (findingOrderAmount >= matchedSideTotalAmount) {
                    matchingTree.deleteOrder(matchedOrder.getOrderId());
                    if (findingOrderAmount == matchedSideTotalAmount) {
                        findingTree.deleteOrder(findingOrder.getOrderId());
                        findingOrderAmount = 0.0;
                    } else {
                        findingOrder.updateAmount(findingOrderAmount - matchedSideTotalAmount);
                        findingOrderAmount = findingOrderAmount - matchedSideTotalAmount;
                    }
                } else if (findingOrderAmount >= matchingOrderPrice) {
                    int matchedQuantity = new Double(findingOrderAmount / matchingOrderPrice).intValue();
                    matchedOrder.updateQuantity(matchingOrderQuantity - matchedQuantity);
                    findingTree.deleteOrder(findingOrder.getOrderId());
                    findingOrderAmount = 0.0;
                }
            }
        }
        return findingOrderAmount;
    }

    private void printOrderStatus() {
        System.out.println(this);
    }

    public OrderTree getBuySide() {
        return buySide;
    }

    public OrderTree getSellSide() {
        return sellSide;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("######################################################");
        builder.append("\n");
        builder.append(" -------------- BUY SIDE --------------");
        builder.append(this.buySide.toString());
        builder.append("\n");
        builder.append(" -------------- SELL SIDE --------------");
        builder.append(this.sellSide.toString());
        builder.append("\n");
        builder.append("######################################################");
        return builder.toString();
    }
}
