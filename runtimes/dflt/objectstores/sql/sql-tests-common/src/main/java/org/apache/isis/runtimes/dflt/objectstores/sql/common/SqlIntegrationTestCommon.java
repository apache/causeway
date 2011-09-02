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

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.NumericTestClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SqlDataClass;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         The Singleton class {@link SqlIntegrationTestSingleton} is used to preserve values between tests. If
 *         {@link SqlIntegrationTestSingleton} state is 0, then a full NOF context is recreated. If
 *         {@link SqlIntegrationTestSingleton} state is 1, then the previous context is re-used.
 * 
 *         The state of 1 is used to separate tests into stand-alone methods, for clarity purposes - without reloading
 *         the entire framework.
 * 
 */
public abstract class SqlIntegrationTestCommon extends SqlIntegrationTestCommonBase {
    private static final Logger LOG = Logger.getLogger(SqlIntegrationTestCommon.class);

    // private static final TimeZone GMTm2_TIME_ZONE;

    // Helper values
    private static final java.sql.Date sqlDate;// = java.sql.Date.valueOf("2010-03-05");

    static {
        /*
         * 
         * // For testing -ve offset timezone local regions. GMTm2_TIME_ZONE = TimeZone.getTimeZone("GMT-0200");
         * //GMTm2_TIME_ZONE = TimeZone.getTimeZone("UTC"); TimeZone.setDefault(GMTm2_TIME_ZONE);
         */

        /*
         * TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC"); if (timeZone == null) { timeZone =
         * TimeZone.getTimeZone("UTC"); } UTC_TIME_ZONE = timeZone;
         */

        /*
         * There is still an issue assigning a java.sql.Date variable from a calendar. final Calendar cal =
         * Calendar.getInstance(); cal.setTimeZone(UTC_TIME_ZONE); cal.clear(); cal.set(Calendar.YEAR, 2011);
         * cal.set(Calendar.MONTH, 4-1); cal.set(Calendar.DAY_OF_MONTH, 8);
         */
        // 2011-4-8 = 1270684800000
        final Date date20100308 = new Date(2010, 4, 8);
        sqlDate = new java.sql.Date(date20100308.getMillisSinceEpoch());
    }

    // {{ Setup
    private static final Date applibDate = new Date(2010, 3, 5); // 2010-03-05 = 1267747200000
    private static final DateTime dateTime = new DateTime(2010, 3, 5, 1, 23);
    private static final TimeStamp timeStamp = new TimeStamp(dateTime.millisSinceEpoch());
    private static final Time time = new Time(14, 56);

    private static final Color color = Color.WHITE;
    private static final Image image = new Image(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
    private static final Password password = new Password("password");
    private static final Percentage percentage = new Percentage(42);
    private static final Money money = new Money(99.99, "ZAR");

    // Standard values
    private static final int intMaxValue = Integer.MAX_VALUE;
    private static final short shortMaxValue = Short.MAX_VALUE;
    private static final long longMaxValue = Long.MAX_VALUE;
    private static final double doubleMaxValue = 1e308;// Double.MAX_VALUE;
    private static final float floatMaxValue = (float) 1e37;// Float.MAX_VALUE;

    private static final int intMinValue = Integer.MIN_VALUE;
    private static final short shortMinValue = Short.MIN_VALUE;
    private static final long longMinValue = Long.MIN_VALUE;
    private static final double doubleMinValue = 1e-307;// Double.MIN_VALUE;
    private static final float floatMinValue = (float) 1e-37;// Float.MIN_VALUE;

    // Collection mapper tests
    private static final List<String> stringList1 = Arrays.asList("Baking", "Bakery", "Canned", "Dairy");
    private static final List<String> stringList2 = Arrays.asList("Fridge", "Deli", "Fresh Produce", "Frozen",
        "Household", "Other..");
    private static List<SimpleClass> simpleClassList1 = new ArrayList<SimpleClass>();
    private static List<SimpleClass> simpleClassList2 = new ArrayList<SimpleClass>();

    private static SimpleClassTwo simpleClassTwoA;
    // private static SimpleClassTwo simpleClassTwoB;

    private static NumericTestClass numericTestClassMax;
    private static NumericTestClass numericTestClassMin;

    public String getPersonTableName() {
        return "sqldataclass";
    }

    public String getSimpleClassTableName() {
        return "simpleclass";
    }

    public String getSimpleClassTwoTableName() {
        return "simpleclasstwo";
    }

    /**
     * Create a {@link SqlDataClass} and persist to the store.
     * 
     * @throws Exception
     */
    public void testCreate() throws Exception {
        SqlIntegrationTestSingleton.drop(getPersonTableName());
        SqlIntegrationTestSingleton.drop(getSimpleClassTableName());
        SqlIntegrationTestSingleton.drop(getSimpleClassTwoTableName());

        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final SqlDataClass sqlDataClass = factory.newDataClass();
        sqlDataClass.setString("Test String");
        sqlDataClass.setDate(applibDate);
        sqlDataClass.setSqlDate(sqlDate);
        sqlDataClass.setMoney(money);
        sqlDataClass.setDateTime(dateTime);
        sqlDataClass.setTimeStamp(timeStamp);
        sqlDataClass.setTime(time);
        sqlDataClass.setColor(color);
        sqlDataClass.setImage(image);
        sqlDataClass.setPassword(password);
        sqlDataClass.setPercentage(percentage);

        // Setup SimpleClassTwo
        simpleClassTwoA = factory.newSimpleClassTwo();
        simpleClassTwoA.setText("A");
        simpleClassTwoA.setIntValue(999);
        simpleClassTwoA.setBooleanValue(true);
        // simpleClassTwoB = factory.newSimpleClassTwo();
        // simpleClassTwoB.setString("A");

        sqlDataClass.setSimpleClassTwo(simpleClassTwoA);

        // NumericClasses
        // standard min types
        numericTestClassMin = factory.newNumericTestClass();
        LOG.log(Level.INFO, "Bits to represent Double: " + Double.SIZE);
        numericTestClassMin.setIntValue(intMinValue);
        numericTestClassMin.setShortValue(shortMinValue);
        numericTestClassMin.setLongValue(longMinValue);
        numericTestClassMin.setDoubleValue(doubleMinValue);
        numericTestClassMin.setFloatValue(floatMinValue);

        sqlDataClass.setNumericTestClassMin(numericTestClassMin);

        // standard max types
        numericTestClassMax = factory.newNumericTestClass();
        numericTestClassMax.setIntValue(intMaxValue);
        numericTestClassMax.setShortValue(shortMaxValue);
        numericTestClassMax.setLongValue(longMaxValue);
        numericTestClassMax.setDoubleValue(doubleMaxValue);
        numericTestClassMax.setFloatValue(floatMaxValue);

        sqlDataClass.setNumericTestClassMax(numericTestClassMax);

        // Initialise collection1
        boolean bMustAdd = false;
        if (simpleClassList1.size() == 0) {
            bMustAdd = true;
        }
        for (final String string : SqlIntegrationTestCommon.stringList1) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoA);
            sqlDataClass.addToSimpleClasses1(simpleClass);
            if (bMustAdd) {
                simpleClassList1.add(simpleClass);
            }
        }

        // Initialise collection2
        /**/
        for (final String string : SqlIntegrationTestCommon.stringList2) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoA);
            sqlDataClass.addToSimpleClasses2(simpleClass);
            if (bMustAdd) {
                simpleClassList2.add(simpleClass);
            }
        }
        /**/
        factory.save(sqlDataClass);

        // For in-memory only!
        if (getProperties().getProperty("isis.persistor") == "in-memory") {
            getSingletonInstance().setState(1);
        }
    }

    // }}

    /**
     * Test loading a persisted {@link SqlDataClass} from the sql store.
     * 
     * @throws Exception
     */
    public void testLoad() throws Exception {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<SqlDataClass> dataClasses = factory.allDataClasses();
        assertEquals(1, dataClasses.size());
        final SqlDataClass sqlDataClass = dataClasses.get(0);
        SqlIntegrationTestSingleton.setDataClass(sqlDataClass);
        getSingletonInstance().setState(1);
    }

    public void testSimpleClassCollection1Lazy() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final List<SimpleClass> collection = sqlDataClass.simpleClasses1;

        assertEquals("collection size is not equal!", collection.size(), simpleClassList1.size());
    }

    /**
     * Test {@link SqlDataClass} {@link String} field.
     * 
     * @throws Exception
     */
    public void testString() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertEquals("Test String", sqlDataClass.getString());
    }

    /**
     * Test {@link SqlDataClass} {@link Date} field.
     * 
     * @throws Exception
     */
    public void testApplibDate() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();

        LOG.log(Level.INFO, "Test: testDate() '2010-3-5' = 1267747200000");

        // 2010-3-5 = 1267747200000
        LOG.log(Level.INFO, "applibDate.dateValue() as String: " + applibDate);
        LOG.log(Level.INFO, "applibDate.dateValue() as Long: " + applibDate.getMillisSinceEpoch());

        // 2010-3-5 = 1267747200000
        LOG.log(Level.INFO, "sqlDataClass.getDate() as String: " + sqlDataClass.getDate());
        LOG.log(Level.INFO, "sqlDataClass.getDate().getTime() as Long: " + sqlDataClass.getDate().getMillisSinceEpoch());

        if (!applibDate.isEqualTo(sqlDataClass.getDate())) {
            fail("Applib date: Test '2010-3-5', expected " + applibDate.toString() + ", but got "
                + sqlDataClass.getDate().toString() + ". Check log for more info.");
            // LOG.log(Level.INFO, "Applib date: Test '2011-3-5', expected " + applibDate.toString() + ", but got "
            // + sqlDataClass.getDate().toString()+". Check log for more info.");
        } else {
            // LOG.log(Level.INFO, "SQL applib.value.date: test passed! Woohoo!");
        }

    }

    /**
     * Test {@link SqlDataClass} {@link java.sql.Date} field.
     * 
     * @throws Exception
     */
    /* */
    @Test
    public void testSqlDate() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();

        LOG.log(Level.INFO, "Test: testSqlDate() '2011-4-8' == 1302220800000");

        // 2011-4-8 = 1302220800000
        LOG.log(Level.INFO, "sqlDate.toString() as String:" + sqlDate); // shows as 2011-04-07
        LOG.log(Level.INFO, "sqlDate.getTime() as Long:" + sqlDate.getTime());

        // 2011-4-8 = 1302220800000
        LOG.log(Level.INFO, "sqlDataClass.getSqlDate() as String:" + sqlDataClass.getSqlDate()); // shows as
        // 2011-04-07
        LOG.log(Level.INFO, "sqlDataClass.getSqlDate().getTime() as Long:" + sqlDataClass.getSqlDate().getTime());

        if (sqlDate.compareTo(sqlDataClass.getSqlDate()) != 0) {
            fail("SQL date: Test '2011-4-8', expected " + sqlDate.toString() + ", but got "
                + sqlDataClass.getSqlDate().toString() + ". Check log for more info.");
            // LOG.log(Level.INFO, "SQL date: Test '2011-4-8', expected " + sqlDate.toString() + ", and got "
            // + sqlDataClass.getSqlDate().toString() +". Check log for more info.");
        } else {
            // LOG.log(Level.INFO, "SQL date: test passed! Woohoo!");
        }

    }/**/

    public void testDateTimezoneIssue() {
        /*
         * At the moment, applib Date and java.sql.Date are restored from ValueSemanticsProviderAbstractTemporal with an
         * explicit hourly offset that comes from the timezone. I.e. in South Africa, with TZ +2h00, they have an
         * implicit time of 02h00 (2AM). This can potentially seriously screw up GMT-X dates, which, I suspect, will
         * actually be set to the dat BEFORE.
         * 
         * This test is a simple test to confirm that date/time before and after checks work as expected.
         */
        /*
         * *
         * SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
         * 
         * DateTime dateTime = sqlDataClass.getDateTime(); // new DateTime(2010, 3, 5, 1, 23); Date date =
         * sqlDataClass.getDate(); // new Date(2010, 3, 5);
         * 
         * //java.sql.Date sqlDate = sqlDataClass.getSqlDate(); // "2010-03-05"
         * //assertTrue("dateTime's value ("+dateTime.dateValue()+ // ") should be after java.sql.date's ("+ sqlDate
         * +")", dateTime.dateValue().after(sqlDate));
         * 
         * assertTrue("dateTime's value ("+dateTime.dateValue()+ ") should be after date's ("+ date +")",
         * dateTime.dateValue().after(date.dateValue()));
         */
    }

    /**
     * Test {@link Money} type.
     */

    public void testMoney() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertEquals(money, sqlDataClass.getMoney());
        // assertTrue("Money " + money.toString() + " is not equal to " + sqlDataClass.getMoney().toString(),
        // money.equals(sqlDataClass.getMoney()));
    }

    /**
     * Test {@link DateTime} type.
     */
    public void testDateTime() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();

        LOG.log(Level.INFO, "Test: testDateTime()");
        LOG.log(Level.INFO, "sqlDataClass.getDateTime() as String:" + sqlDataClass.getDateTime());
        LOG.log(Level.INFO, "dateTime.toString() as String:" + dateTime);

        LOG.log(Level.INFO, "sqlDataClass.getDateTime().getTime() as Long:"
            + sqlDataClass.getDateTime().millisSinceEpoch());
        LOG.log(Level.INFO, "dateTime.getTime() as Long:" + dateTime.millisSinceEpoch());

        if (!dateTime.equals(sqlDataClass.getDateTime())) {
            fail("DateTime " + dateTime.toString() + " is not equal to " + sqlDataClass.getDateTime().toString());
        }
    }

    /**
     * Test {@link TimeStamp} type.
     */
    public void testTimeStamp() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertTrue("TimeStamp " + timeStamp.toString() + " is not equal to " + sqlDataClass.getTimeStamp().toString(),
            timeStamp.isEqualTo(sqlDataClass.getTimeStamp()));
    }

    /**
     * Test {@link Time} type.
     */
    /**/
    public void testTime() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertNotNull("sqlDataClass is null", sqlDataClass);
        assertNotNull("getTime() is null", sqlDataClass.getTime());
        assertTrue("Time 14h56: expected " + time.toString() + ", but got " + sqlDataClass.getTime().toString(),
            time.isEqualTo(sqlDataClass.getTime()));
    }

    /**/

    /**
     * Test {@link Color} type.
     */
    public void testColor() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertEquals(color, sqlDataClass.getColor());
        // assertTrue("Color Black, expected " + color.toString() + " but got " + sqlDataClass.getColor().toString(),
        // color.isEqualTo(sqlDataClass.getColor()));
    }

    /**
     * Test {@link Image} type.
     */
    // TODO: Images are not equal...
    /*
     * public void testImage(){ SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson(); Image image2 =
     * sqlDataClass.getImage(); assertEqual(image, image2); }
     * 
     * private void assertEqual(Image image2, Image image3) { assertEquals(image2.getHeight(), image3.getHeight());
     * assertEquals(image2.getWidth(), image3.getWidth()); boolean same = true; int i=0,j=0; int p1=0, p2=0; String
     * error = ""; int [][] i1 = image2.getImage(), i2 = image3.getImage(); for(i = 0; same &&
     * i<image2.getHeight();i++){ int [] r1 = i1[i], r2 = i2[i]; for (j = 0; same && j < image2.getWidth(); j++){ if
     * (r1[j] != r2[j]){ same = false; p1 = r1[j]; p2 = r2[j]; error = "Images differ at i = "+i+", j = "+j+", "+p1+
     * " is not "+p2+"!"; break; } } } assertTrue(error, same); }
     */

    /**
     * Test {@link Password} type.
     */
    public void testPassword() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertEquals(password, sqlDataClass.getPassword());
    }

    /**
     * Test {@link Percentage} type.
     */
    public void testPercentage() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        assertEquals(percentage, sqlDataClass.getPercentage());
    }

    public void testStandardValueTypesMaxima() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final NumericTestClass numericTestMaxClass = sqlDataClass.getNumericTestClassMax();

        assertEquals(shortMaxValue, numericTestMaxClass.getShortValue());
        assertEquals(intMaxValue, numericTestMaxClass.getIntValue());
        assertEquals(longMaxValue, numericTestMaxClass.getLongValue());
        assertEquals(doubleMaxValue, numericTestMaxClass.getDoubleValue()); // fails in MySQL = infinity
        assertEquals(floatMaxValue, numericTestMaxClass.getFloatValue());
    }

    public void testStandardValueTypesMinima() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final NumericTestClass numericTestMinClass = sqlDataClass.getNumericTestClassMin();

        assertEquals(shortMinValue, numericTestMinClass.getShortValue());
        assertEquals(intMinValue, numericTestMinClass.getIntValue());
        assertEquals(longMinValue, numericTestMinClass.getLongValue());
        assertEquals(doubleMinValue, numericTestMinClass.getDoubleValue()); // fails in MySQL = infinity
        assertEquals(floatMinValue, numericTestMinClass.getFloatValue());
    }

    /**
     * Test {@link StringCollection} type.
     */
    /*
     * public void testStringCollection(){ SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
     * List<String> collection = sqlDataClass.getStringCollection(); int i = 0; for (String string : collection) {
     * assertEquals(SqlIntegrationTestCommon.stringList.get(i++), string); } }
     */

    public void testSingleReferenceLazy() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        if (getProperties().getProperty("isis.persistor") != "in-memory") {
            assertEquals(null, a.text); // must check direct value, as
            // framework can auto-resolve, if you use getText()
        }
    }

    /**
     * Test a collection of {@link SimpleClass} type.
     */
    public void testSimpleClassCollection1() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();

        assertEquals("collection size is not equal!", SqlIntegrationTestCommon.simpleClassList1.size(),
            collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : SqlIntegrationTestCommon.simpleClassList1) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    /**
     * Test another collection of {@link SimpleClass} type.
     */
    /**/
    public void testSimpleClassCollection2() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses2();

        assertEquals("collection size is not equal!", SqlIntegrationTestCommon.simpleClassList2.size(),
            collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : SqlIntegrationTestCommon.simpleClassList2) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    public void testSimpleClassTwoReferenceLazy() {
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        if (getProperties().getProperty("isis.persistor") != "in-memory") {
            for (final SimpleClass simpleClass : collection) {
                final SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
                assertEquals(null, a.text); // must check direct value, as
                                            // framework can auto-resolve, if
                                            // you use getText()
            }
        }
    }

    public void testSingleReferenceResolve() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        factory.resolve(a);
        assertEquals(simpleClassTwoA.getText(), a.getText());
    }

    public void testSimpleClassTwoReferenceResolve() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getDataClass();
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        for (final SimpleClass simpleClass : collection) {
            final SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
            factory.resolve(a);
            assertEquals(simpleClassTwoA.getText(), a.getText());
            assertEquals(simpleClassTwoA.getIntValue(), a.getIntValue());
            assertEquals(simpleClassTwoA.getBooleanValue(), a.getBooleanValue());
        }
    }

    public void testSimpleClassTwo() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(1, classes.size());
        for (final SimpleClassTwo simpleClass : classes) {
            assertEquals(simpleClassTwoA.getText(), simpleClass.getText());
        }
    }

    public void testUpdate1() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(1, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        simpleClass.setText("XXX");
        simpleClass.setBooleanValue(false);
        simpleClassTwoA.setBooleanValue(false);
    }

    public void testUpdate2() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(1, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        assertEquals("XXX", simpleClass.getText());
        assertEquals(simpleClassTwoA.getBooleanValue(), simpleClass.getBooleanValue());
    }

    // Last "test" - Set the Singleton state to 0 to invoke a clean shutdown.
    public void testSetStateZero() {
        getSingletonInstance().setState(0);
    }

}
