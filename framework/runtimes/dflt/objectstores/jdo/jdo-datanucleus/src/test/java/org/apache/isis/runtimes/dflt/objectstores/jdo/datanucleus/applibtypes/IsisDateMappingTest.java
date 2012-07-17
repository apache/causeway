package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.applibtypes;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.Date;

public class IsisDateMappingTest {

    private IsisDateMapping dateMapping;

    @Before
    public void setUp() throws Exception {
        dateMapping = new IsisDateMapping();
    }
    
    @Test
    public void roundTrip() {
        Date date = new Date();
        final Long datastoreValue = dateMapping.objectToLong(date);
        Date date2 = (Date) dateMapping.longToObject(datastoreValue);
        
        // necessary to use dateValue() because the Isis date (rather poorly) does not
        // override equals() / hashCode()
        assertThat(date.dateValue(), is(equalTo(date2.dateValue())));
    }

}
