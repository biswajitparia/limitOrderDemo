package org.example.order;

import org.example.enums.OrderAction;
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
        int quantity = order.getQuantity();
        if (quantity < 1) throw new OrderException("Quantity of Order cannot be less than 1");

        if (order.getSide() == OrderAction.BID) {
            buySide.addOrder(order);
        } else {
            sellSide.addOrder(order);
        }

        if (order.getSide() == OrderAction.BID) {
            while (quantity > 0 && sellSide.isNotEmpty() && order.getPrice() >= sellSide.getLowestPrice()) {
                List<Order> minPricedOrderList = sellSide.getMinPricedOrderList();
                if (minPricedOrderList == null || minPricedOrderList.isEmpty()) break;
                quantity = matchAndExecute(order, minPricedOrderList, quantity, buySide, sellSide);
            }
        } else {
            while (quantity > 0 && buySide.isNotEmpty() && order.getPrice() <= buySide.getHighestPrice()) {
                List<Order> maxPricedOrderList = buySide.getMaxPricedOrderList();
                if (maxPricedOrderList == null || maxPricedOrderList.isEmpty()) break;
                quantity = matchAndExecute(order, maxPricedOrderList, quantity, sellSide, buySide);
            }
        }
    }

    private int matchAndExecute(Order findingSideOrder, List<Order> matchedPriceOrderList, int findingSideQuantityRequested, OrderTree findingSideTree, OrderTree matchingSideTree) {
        for (Order matchedOrder : matchedPriceOrderList) {
            boolean isTraded = false;
            int tradedQuantity = 0;
            // on exact match reduce quantity to zero and delete the order from list
            if (findingSideQuantityRequested <= matchedOrder.getQuantity()) {
                tradedQuantity = findingSideQuantityRequested;
                isTraded = true;
                findingSideTree.deleteOrder(findingSideOrder.getOrderId());
                findingSideQuantityRequested = 0;
                if (matchedOrder.getQuantity() - tradedQuantity == 0) {
                    matchingSideTree.deleteOrder(matchedOrder.getOrderId());
                } else {
                    matchedOrder.updateQuantity(matchedOrder.getQuantity() - tradedQuantity);
                }
            } else {
                isTraded = true;
                tradedQuantity = matchedOrder.getQuantity();
                findingSideQuantityRequested -= matchedOrder.getQuantity();
                matchingSideTree.deleteOrder(matchedOrder.getOrderId());
                findingSideOrder.updateQuantity(findingSideQuantityRequested);
            }

            if (isTraded) {
                System.out.println("**************** Limit Buy Trade Executed ****************");
                System.out.println("OrderID: " + findingSideOrder.getOrderId() + " bought " + tradedQuantity);
                System.out.println("OrderID: " + matchedOrder.getOrderId() + " sold " + tradedQuantity);
                System.out.println("**************** ENDS ****************");
            }
        }
        return findingSideQuantityRequested;
    }

    /**
     * Print Order Book
     *
     * @return string version of order book for printing
     */
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
