package org.apache.isis.core.metamodel.facetapi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.isis.applib.Identifier;
import org.junit.Test;

public class FeatureTypeTest_identifierFor {

    public static class SomeDomainClass {
        private BigDecimal aBigDecimal;
        public BigDecimal getABigDecimal() {
            return aBigDecimal;
        }
        public void setABigDecimal(BigDecimal aBigDecimal) {
            this.aBigDecimal = aBigDecimal;
        }
        private BigDecimal anotherBigDecimal;
        public BigDecimal getAnotherBigDecimal() {
            return anotherBigDecimal;
        }
        public void setAnotherBigDecimal(BigDecimal anotherBigDecimal) {
            this.anotherBigDecimal = anotherBigDecimal;
        }
    }
    
    @Test
    public void property_whenMethodNameIs_XYyyZzz() throws Exception {
        Method method = SomeDomainClass.class.getMethod("getABigDecimal");
        final Identifier identifierFor = FeatureType.PROPERTY.identifierFor(SomeDomainClass.class, method);
        assertThat(identifierFor.getMemberName(), is("ABigDecimal")); // very odd compared to anotherBigDecimal, but arises from Introspector class, so presumably part of the javabeans spec.
    }

    @Test
    public void property_whenMethodNameIs_XxxxYyyZzz() throws Exception {
        Method method = SomeDomainClass.class.getMethod("getAnotherBigDecimal");
        final Identifier identifierFor = FeatureType.PROPERTY.identifierFor(SomeDomainClass.class, method);
        assertThat(identifierFor.getMemberName(), is("anotherBigDecimal"));
    }

}
