package org.example.order;

import org.example.enums.OrderAction;
import org.example.enums.OrderType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderBookTest {
    OrderBook book = new OrderBook();

    @Test
    public void test_orderbook_process() {
        Order sellLimitOrder1 = new Order(Instant.now(), 35, 122.5, "1", "T1", OrderAction.ASK, "Apple", OrderType.LIMIT);
        Order sellLimitOrder2 = new Order(Instant.now(), 35, 122.5, "2", "T1", OrderAction.ASK, "Apple", OrderType.LIMIT);
        Order sellLimitOrder3 = new Order(Instant.now(), 80, 202.5, "3", "T2", OrderAction.ASK, "Apple", OrderType.LIMIT);

        Order buyMarketOrder4 = new Order(Instant.now(), 99, 23.5, "4", "T3", OrderAction.BID, "Apple", OrderType.MARKET);
        Order buyLimitOrder5 = new Order(Instant.now(), 15, 201.5, "5", "T4", OrderAction.BID, "Apple", OrderType.LIMIT);

        book.processOrder(sellLimitOrder1);
        book.processOrder(sellLimitOrder2);
        book.processOrder(sellLimitOrder3);
        book.processOrder(buyMarketOrder4);
        book.processOrder(buyLimitOrder5);

        assertTrue(sellLimitOrder3.getQuantity() == 51);
        assertTrue(buyLimitOrder5.getQuantity() == 15);

    }

}