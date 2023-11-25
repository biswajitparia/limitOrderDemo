package org.example;

import org.example.enums.OrderAction;
import org.example.enums.OrderType;
import org.example.order.Order;
import org.example.order.OrderBook;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!");
        Order firstSellLimitOrder = new Order(
                Instant.now(),
                35,
                122.5,
                "O123456-S",
                "T12345-S",
                OrderAction.ASK,
                "Apple",
                OrderType.LIMIT
        );

        Order secondSellLimitOrder = new Order(
                Instant.now(),
                35,
                122.5,
                "O123456-d",
                "T12345-d",
                OrderAction.ASK,
                "Apple",
                OrderType.LIMIT
        );

        Order thirdSellLimitOrder = new Order(
                Instant.now(),
                80,
                202.5,
                "O123456-S1",
                "T12345-S",
                OrderAction.ASK,
                "Apple",
                OrderType.LIMIT
        );

        Order buyMarketOrder = new Order(
                Instant.now(),
                99,
                23.5,
                "O123456",
                "T12345",
                OrderAction.BID,
                "Apple",
                OrderType.MARKET
        );

        Order buyLimitOrder = new Order(
                Instant.now(),
                95,
                202.5,
                "O1234567",
                "T123457",
                OrderAction.BID,
                "Apple",
                OrderType.LIMIT
        );

        Order sellMarketOrder = new Order(
                Instant.now(),
                15,
                204.5,
                "O1234568",
                "T1234578",
                OrderAction.ASK,
                "Apple",
                OrderType.MARKET
        );


        OrderBook book = new OrderBook();
        book.processOrder(firstSellLimitOrder);
        book.processOrder(secondSellLimitOrder);
        book.processOrder(thirdSellLimitOrder);
        System.out.println(book);

        book.processOrder(buyMarketOrder);
        System.out.println(book);

        book.processOrder(buyLimitOrder);
        System.out.println(book);

    }
}