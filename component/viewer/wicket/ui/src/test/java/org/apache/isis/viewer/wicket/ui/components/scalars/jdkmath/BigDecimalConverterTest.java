package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalConverterTest {

    final BigDecimal bd_123_45_scale2 = new BigDecimal("123.45").setScale(2);
    final BigDecimal bd_123_45_scale4 = new BigDecimal("123.45").setScale(4);
    final BigDecimal bd_123_4500_scale2 = new BigDecimal("123.4500").setScale(2);
    final BigDecimal bd_123_4500_scale4 = new BigDecimal("123.4500").setScale(4);
    
    @Test
    public void test_scale2() {
        final BigDecimal actual = new BigDecimalConverter(2).convertToObject("123.45", null);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);
        
        Assert.assertNotEquals(bd_123_4500_scale4, actual);
        Assert.assertNotEquals(bd_123_45_scale4, actual);
    }
    
    @Test
    public void test_scale4() {
        final BigDecimal actual = new BigDecimalConverter(4).convertToObject("123.45", null);
        Assert.assertNotEquals(bd_123_4500_scale2, actual);
        Assert.assertNotEquals(bd_123_45_scale2, actual);
        
        Assert.assertEquals(bd_123_4500_scale4, actual);
        Assert.assertEquals(bd_123_45_scale4, actual);
    }
    

}
