/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.application.valueholder;

import org.apache.isis.application.value.ValueParseException;

import java.util.Locale;


public class DatePeriodTest extends ValueTestCase {
    static {
        Locale.setDefault(Locale.UK);
    }

    private DatePeriod mayJul, junAug, junJul, dp1, dp2;

    protected void setUp() throws Exception {
        super.setUp();

        mayJul = new DatePeriod();
        mayJul.getStart().setValue(2003, 5, 1);
        mayJul.getEndDate().setValue(2003, 7, 31);

        junAug = new DatePeriod();
        junAug.getStart().setValue(2003, 6, 1);
        junAug.getEndDate().setValue(2003, 8, 31);

        junJul = new DatePeriod();
        junJul.getStart().setValue(2003, 6, 1);
        junJul.getEndDate().setValue(2003, 7, 31);

        dp1 = new DatePeriod();
        dp2 = new DatePeriod();
    }

    public void testLocale() {
        assertEquals(Locale.UK, Locale.getDefault());

    }

    protected void tearDown() throws Exception {
        mayJul = null;
        junAug = null;
        junJul = null;
    }

    public void testClear() {
        mayJul.clear();
        assertTrue(mayJul.title().toString().equals("~"));
        assertTrue(mayJul.isEmpty());
    }

    public void testOverlaps() throws Exception {

        assertTrue(mayJul.startsBefore(junAug));
        assertFalse(junAug.startsBefore(mayJul));
        assertTrue(junAug.endsAfter(mayJul));
        assertFalse(mayJul.endsAfter(junAug));

        assertTrue(mayJul.overlaps(junAug));

        DatePeriod overlap = new DatePeriod(mayJul);
        overlap.overlap(junAug);
        assertTrue(junJul.isSameAs(overlap));

        overlap = new DatePeriod(junAug);
        overlap.overlap(mayJul);
        assertTrue(junJul.isSameAs(overlap));

        DatePeriod lead = new DatePeriod(junAug);
        lead.leadDifference(mayJul);
        // assertEquals("01-May-2003 ~ 31-May-2003", lead.title().toString());

        DatePeriod tail = new DatePeriod(junAug);
        tail.tailDifference(mayJul);
        assertEquals("01-Aug-2003 ~ 31-Aug-2003", tail.title().toString());
    }

    public void testParse() throws Exception {
        dp1.parseUserEntry("14-Apr-2003 ~ 16-Nov-2004");
        assertEquals("14-Apr-2003 ~ 16-Nov-2004", dp1.title().toString());
        dp1.parseUserEntry("15-Jan-2002  ~   6-May-2005");
        assertEquals("15-Jan-2002 ~ 06-May-2005", dp1.title().toString());
        dp1.parseUserEntry("5-jan-2002~06-may-2005");
        assertEquals("05-Jan-2002 ~ 06-May-2005", dp1.title().toString());
        try {
            dp1.parseUserEntry("hgjuiy");
            fail();
        } catch (ValueParseException e) {
            assertTrue(e.getMessage().equals("No tilde found"));
        }
        try {
            dp1.parseUserEntry("05-Jan-2002 - 06-May-2005");
            fail();
        } catch (ValueParseException e) {
            assertTrue(e.getMessage().equals("No tilde found"));
        }
        try {
            dp1.parseUserEntry("rtyu~ghjk");
            fail();
        } catch (ValueParseException e) {
            assertTrue(e.getMessage().equals("Invalid date rtyu"));
        }
        try {
            dp1.parseUserEntry("14-Apr-2003 ~ 16-Nov-2002");
            fail();
        } catch (ValueParseException expected) {}

        dp1.parseUserEntry("14-Apr-2003~");
        assertTrue(dp1.title().toString().equals("14-Apr-2003 ~"));
        dp1.parseUserEntry("~16-Nov-2004");
        assertTrue(dp1.title().toString().equals("~ 16-Nov-2004"));

    }

    public void testSaveAndRestore() throws Exception {
        dp1.parseUserEntry("04-May-2003 ~ 16-May-2004");
        String s = dp1.asEncodedString();
        assertEquals("20030504~20040516", s);
        dp2.restoreFromEncodedString(s);
        assertEquals("04-May-2003 ~ 16-May-2004", dp2.title().toString());
        assertTrue(dp1.isSameAs(dp2));
    }

}
