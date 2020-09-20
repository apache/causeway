package org.apache.isis.extensions.commandlog.impl.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BigDecimalUtils {

    /**
     * @return in seconds, to 3 decimal places.
     */
    public static BigDecimal durationBetween(Timestamp startedAt, Timestamp completedAt) {
        if (completedAt == null) {
            return null;
        } else {
            long millis = completedAt.getTime() - startedAt.getTime();
            return toSeconds(millis);
        }
    }

    private static final BigDecimal DIVISOR = new BigDecimal(1000);

    private static BigDecimal toSeconds(long millis) {
        return new BigDecimal(millis)
                .divide(DIVISOR, RoundingMode.HALF_EVEN)
                .setScale(3, RoundingMode.HALF_EVEN);
    }

}
