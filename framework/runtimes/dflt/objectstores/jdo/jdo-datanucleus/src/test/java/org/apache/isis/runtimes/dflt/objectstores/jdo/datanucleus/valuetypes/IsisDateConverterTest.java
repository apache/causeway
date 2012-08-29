package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.Date;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.valuetypes.IsisDateConverter;

public class IsisDateConverterTest {

    private IsisDateConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new IsisDateConverter();
    }
    
    @Test
    public void roundTrip() {
        Date date = new Date();
        final Long value = converter.toDatastoreType(date);
        Date date2 = (Date) converter.toMemberType(value);
        
        // necessary to use dateValue() because the Isis date (rather poorly) does not
        // override equals() / hashCode()
        assertThat(date.dateValue(), is(equalTo(date2.dateValue())));
    }

    @Test
    public void toLong_whenNull() {
        assertNull(converter.toDatastoreType(null));
    }

    @Test
    public void toObject_whenNull() {
        assertNull(converter.toMemberType(null));
    }

}
