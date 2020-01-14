package org.apache.isis.core.metamodel.facets.value.temporal;

import java.time.temporal.Temporal;
import java.util.StringTokenizer;
import java.util.function.BiFunction;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class TemporalAdjustUtil {
    
    static <T extends Temporal> T parseAdjustment(
            final BiFunction<TemporalAdjust, T, T> adjuster,
            final T contextTemporal, 
            final String temporalString) {
        
        if (temporalString.startsWith("+")) {
            return TemporalAdjustUtil.relativeTemporal(adjuster, contextTemporal, temporalString, 1);
        } 
        if (temporalString.startsWith("-")) {
            return TemporalAdjustUtil.relativeTemporal(adjuster, contextTemporal, temporalString, -1);
        }
        return null;
    }
    
    private static <T extends Temporal> T relativeTemporal(
            final BiFunction<TemporalAdjust, T, T> adjuster,
            final T contextTemporal, 
            final String str, 
            final int sign) {
        
        T relativeDate = contextTemporal;
        if (str.equals("")) {
            return contextTemporal;
        }

        try {
            final StringTokenizer st = new StringTokenizer(str.substring(1), " ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                relativeDate = adjustTemporal(adjuster, relativeDate, token, sign);
            }
            return relativeDate;
        } catch (final Exception e) {
            return contextTemporal;
        }
    }

    private static <T extends Temporal> T adjustTemporal(
            final BiFunction<TemporalAdjust, T, T> adjuster,
            final T contextDate, 
            final String str, 
            final int sign) {
        
        val temporalAdjust = TemporalAdjust.parse(str).sign(sign);
        return adjuster.apply(temporalAdjust, contextDate);
    }


}
