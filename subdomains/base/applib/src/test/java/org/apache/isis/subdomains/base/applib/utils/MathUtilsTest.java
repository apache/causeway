package org.apache.isis.subdomains.base.applib.utils;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MathUtilsTest {

    public static class IsZeroOrNull extends MathUtilsTest {

        @Test
        public void testIsZeroOrNull() {
            Assert.assertTrue(MathUtils.isZeroOrNull(null));
            Assert.assertTrue(MathUtils.isZeroOrNull(BigDecimal.valueOf(0)));
            Assert.assertFalse(MathUtils.isZeroOrNull(BigDecimal.valueOf(100)));
            Assert.assertFalse(MathUtils.isNotZeroOrNull(null));
            Assert.assertFalse(MathUtils.isNotZeroOrNull(BigDecimal.valueOf(0)));
            Assert.assertTrue(MathUtils.isNotZeroOrNull(BigDecimal.valueOf(100)));
        }
    }

    public static class Round extends MathUtilsTest {

        @Test
        public void roundDown() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.54"), 1), is(new BigDecimal("4.5")));
        }

        @Test
        public void noRounding() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.54"), 2), is(new BigDecimal("4.54")));
        }

        @Test
        public void roundUp() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.55"), 1), is(new BigDecimal("4.6")));
        }
    }

    public static class Max extends MathUtilsTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(MathUtils.max(null, BigDecimal.ZERO, new BigDecimal("123.45"), new BigDecimal("123")), is(new BigDecimal("123.45")));
        }
    }

    public static class SomeThing extends MathUtilsTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(MathUtils.maxUsingFirstSignum(new BigDecimal("-123.45"), new BigDecimal("-123")), is(new BigDecimal("-123.45")));
            assertThat(MathUtils.maxUsingFirstSignum(null, BigDecimal.ZERO, new BigDecimal("-123.45"), new BigDecimal("-123")), is(new BigDecimal("-123.45")));
        }
    }

}
