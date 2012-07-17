package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.applibtypes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.Date;

public class IsisDateConverterTest {

    private IsisDateConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new IsisDateConverter();
    }
    
    @Test
    public void roundTrip() {
        Date date = new Date();
        final Long value = converter.toLong(date);
        Date date2 = (Date) converter.toObject(value);
        
        // necessary to use dateValue() because the Isis date (rather poorly) does not
        // override equals() / hashCode()
        assertThat(date.dateValue(), is(equalTo(date2.dateValue())));
    }

    @Test
    public void toLong_whenNull() {
        assertNull(converter.toLong(null));
    }

    @Test
    public void toObject_whenNull() {
        assertNull(converter.toObject(null));
    }

}
