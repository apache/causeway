package org.apache.isis.subdomains.base.applib.utils;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * General-purpose math utilities.
 */
public final class MathUtils {
    
    private MathUtils() {}
    
    public static BigDecimal round(final BigDecimal input, final int precision) {
        final MathContext mc = new MathContext(precision+1);
        return input.round(mc);
    }

    public static boolean isZeroOrNull(final BigDecimal input) {
        return input == null ? true : input.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isNotZeroOrNull(final BigDecimal input) {
        return !isZeroOrNull(input);
    }

    public static BigDecimal firstNonZero(final BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal max(BigDecimal... input){
        BigDecimal max = BigDecimal.ZERO;
        for (BigDecimal value : input){
            if (value != null){
                max = max.max(value);
            }
        }
        return max;
    }

    public static BigDecimal maxUsingFirstSignum(final BigDecimal... values) {
        BigDecimal max = BigDecimal.ZERO;
        boolean signumDetected = false;
        BigDecimal signum = BigDecimal.ONE;
        for (BigDecimal value : values){
            if (value != null && value.compareTo(BigDecimal.ZERO) != 0){
                if (!signumDetected) {
                    signum = BigDecimal.valueOf(value.signum() == -1 ? -1 : 1);
                    signumDetected = true;
                }
                max = max.max(value.multiply(signum));
            }
        }
        return max.multiply(signum);

    }
}
