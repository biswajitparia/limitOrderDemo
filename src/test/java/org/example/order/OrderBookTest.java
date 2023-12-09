package org.example.order;

import org.example.enums.OrderAction;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderBookTest {
    private OrderBook book = new OrderBook();

    @Test
    public void test_execute_buy_market_order_case_1() {
        List<Order> listOfOrders = Arrays.asList(

                Order.getLimitOrder(Instant.now(), 35, 100, "1", "T1", OrderAction.ASK, "Apple"),
                Order.getLimitOrder(Instant.now(), 35, 100, "2", "T1", OrderAction.ASK, "Apple"),
                Order.getLimitOrder(Instant.now(), 10, 200, "3", "T2", OrderAction.ASK, "Apple"),
                Order.getMarketOrder(Instant.now(), 1000, "4", "T2", OrderAction.ASK, "Apple"),

                Order.getMarketOrder(Instant.now(), 10000.0, "5", "T3", OrderAction.BID, "Apple"),
                Order.getMarketOrder(Instant.now(), 20000.0, "6", "T3", OrderAction.BID, "Apple")
        );
        listOfOrders.forEach(order -> book.processOrder(order));
        assertTrue(book.getBuySide().getOrderCount() == 1);
        assertTrue(book.getSellSide().getOrderCount() == 0);

        /*
        Results:
        Explanation: Total amount from Order 1-4 (including Limit and Market ASK/sell order) will be offset by BID/buy Market Order-5
        Order-6 of amount 20k will be in waiting execution to be BID.

        Note: Price for Market order is shown below log is due to internal implementation design - please ignore this.
        Since Marker Order's Amount is the main price construct.
        ######################################################
         -------------- BUY SIDE --------------
        Price, Quantity, Orders, Amount
        1.7976931348623157E308, 0, 1, 20000.0

         -------------- SELL SIDE --------------
        Price, Quantity, Orders, Amount

        ######################################################
         */
    }

    @Test
    public void test_execute_sell_market_order_case_2() {
        List<Order> listOfOrders = Arrays.asList(

                Order.getLimitOrder(Instant.now(), 35, 100, "1", "T1", OrderAction.BID, "Apple"),
                Order.getLimitOrder(Instant.now(), 35, 100, "2", "T1", OrderAction.BID, "Apple"),
                Order.getLimitOrder(Instant.now(), 10, 200, "3", "T2", OrderAction.BID, "Apple"),
                Order.getMarketOrder(Instant.now(), 1000, "4", "T2", OrderAction.BID, "Apple"),

                Order.getMarketOrder(Instant.now(), 10000.0, "5", "T3", OrderAction.ASK, "Apple"),
                Order.getMarketOrder(Instant.now(), 20000.0, "6", "T3", OrderAction.ASK, "Apple")

        );
        listOfOrders.forEach(order -> book.processOrder(order));
        assertTrue(book.getBuySide().getOrderCount() == 0);
        assertTrue(book.getSellSide().getOrderCount() == 1);
        /*
        Results:
        Explanation: Total Amount from Order 1-4 (including Limit and Market BID/buy order) will be offset by ASK/sell Market Order-5
        Order-6 of amount 20k will be in waiting execution to be ASK.

        Note: Price for Market order is shown below log is due to internal implementation design - please ignore this.
        Since Marker Order's Amount is the main price construct.
        ######################################################
         -------------- BUY SIDE --------------
        Price, Quantity, Orders, Amount

         -------------- SELL SIDE --------------
        Price, Quantity, Orders, Amount
        4.9E-324, 0, 1, 20000.0

        ######################################################
         */
    }

    @Test
    public void test_execute_buy_limit_order_case_3() {
        List<Order> listOfOrders = Arrays.asList(

                Order.getLimitOrder(Instant.now(), 35, 100, "1", "T1", OrderAction.ASK, "Apple"),
                Order.getLimitOrder(Instant.now(), 35, 100, "2", "T1", OrderAction.ASK, "Apple"),
                Order.getLimitOrder(Instant.now(), 10, 200, "3", "T2", OrderAction.ASK, "Apple"),
                Order.getMarketOrder(Instant.now(), 1000, "4", "T2", OrderAction.ASK, "Apple"),

                Order.getLimitOrder(Instant.now(), 50, 1000, "5", "T3", OrderAction.BID, "Apple"),
                Order.getLimitOrder(Instant.now(), 25, 800, "6", "T3", OrderAction.BID, "Apple")
        );
        listOfOrders.forEach(order -> book.processOrder(order));
        assertTrue(book.getBuySide().getOrderCount() == 0);
        assertTrue(book.getSellSide().getOrderCount() == 1);
        /*
        Results:
        Explanation: when BID/buy Limit order-5 is placed, it will be first matched with Market Order-4, and then after execution 49 quantity will remain.
        next, 49 quantity of Limit Order-5 will be executed against 70 quantity of order 1&2, and then 21 quantity of  order-2 will remain.
        next, when  BID/buy Limit order-6 is placed of 25 quantity, it will be executed against Order 2&3 of total 31 quantity, and 6 quantity of order-3 will remain at Last.
        ######################################################
         -------------- BUY SIDE --------------
        Price, Quantity, Orders, Amount

         -------------- SELL SIDE --------------
        Price, Quantity, Orders, Amount
        200.0, 6, 1, 0.0

        ######################################################
         */
    }

    @Test
    public void test_execute_sell_limit_order_case_4() {
        List<Order> listOfOrders = Arrays.asList(

                Order.getLimitOrder(Instant.now(), 35, 100, "1", "T1", OrderAction.BID, "Apple"),
                Order.getLimitOrder(Instant.now(), 35, 100, "2", "T1", OrderAction.BID, "Apple"),
                Order.getLimitOrder(Instant.now(), 10, 200, "3", "T2", OrderAction.BID, "Apple"),
                Order.getMarketOrder(Instant.now(), 1000, "4", "T2", OrderAction.BID, "Apple"),

                Order.getLimitOrder(Instant.now(), 50, 1000, "5", "T3", OrderAction.ASK, "Apple"),
                Order.getLimitOrder(Instant.now(), 25, 800, "6", "T3", OrderAction.ASK, "Apple")
        );
        listOfOrders.forEach(order -> book.processOrder(order));
        assertTrue(book.getBuySide().getOrderCount() == 3);
        assertTrue(book.getSellSide().getOrderCount() == 2);
        /*
        Results:
        Explanation: when ASK/sell Limit order-5 is placed, it will be first matched with Market Order-4, and then after execution 49 quantity will remain for order-5.
        next, 49 quantity of Limit Order-5 will be executed against all remaining order on buy-side, but nothing will be executed since limit order price(1000) is higher than all
        existing order of buy side.
        Next, Limit order-6 will be placed for ASK/sell, Again no execution will happen since limit order price(800) is higher than all existing order of buy side.
        So in this scenario, only one execution happened.
        ######################################################
         -------------- BUY SIDE --------------
        Price, Quantity, Orders, Amount
        100.0, 70, 2, 0.0
        200.0, 10, 1, 0.0

         -------------- SELL SIDE --------------
        Price, Quantity, Orders, Amount
        800.0, 25, 1, 0.0
        1000.0, 49, 1, 0.0

        ######################################################
         */
    }

}