package org.apache.isis.viewer.bdd.common.parsers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;


public class DateParserTest {

    /**
     * Tracking down problem in ISIS-18...
     * <p>
     * This fails because 'MMM' is gonna be different in different languages.
     */
    @Ignore
    @Test
    public void parsesUnder_enUK_butNotUnder_deDE() throws Exception {
        DateParser dateParser = new DateParser();
        dateParser.setDateFormat("dd-MMM-yyyy");
        dateParser.setTimeFormat("hh:mm");
        Date parse = dateParser.parse("02-May-2010 09:20");
        assertThat(parse, is(not(nullValue())));
    }


}
