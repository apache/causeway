package demoapp.dom.ui.custom;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.val;
import lombok.experimental.UtilityClass;


@UtilityClass
public class LatLng {

    public static BigDecimal toBigDecimal(final String val) {
        return new BigDecimal(val);
    }

    public static String toString(BigDecimal val) {
        return val.toPlainString();
    }

    public static String add(final String val, final int hundredths) {
        val scaleBd = new BigDecimal(hundredths).setScale(2, RoundingMode.HALF_UP);
        val scaleDividedBy100 = scaleBd.divide(new BigDecimal(100), RoundingMode.HALF_UP);
        val bd = toBigDecimal(val);
        val newVal = bd.add(scaleDividedBy100);
        return toString(newVal);
    }
}
