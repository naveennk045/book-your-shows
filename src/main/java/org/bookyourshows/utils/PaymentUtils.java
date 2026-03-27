package org.bookyourshows.utils;

import java.util.UUID;

public class PaymentUtils {

    public static String generateGateWayTransactionId() {
        return UUID.randomUUID().toString();
    }
}
