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

package org.apache.isis.objectstore.sql.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.sqlos.SqlDomainObjectRepository;
import org.apache.isis.core.tck.dom.sqlos.data.SimpleClass;
import org.apache.isis.core.tck.dom.sqlos.data.SimpleClassTwo;
import org.apache.isis.core.tck.dom.sqlos.data.SqlDataClass;
import org.apache.isis.objectstore.sql.common.SqlIntegrationTestFixtures.State;

/**
 * @author Kevin kevin@kmz.co.za
 * 
 *         This common class is used by the datatype tests (values, objects, collections) to ensure proper creation and
 *         reloading of domain objects.
 * 
 *         There are two "tests", with the framework re-initialised each time (to flush any objectstore memory of any
 *         created domain objects).
 * 
 *         The Singleton class {@link SqlIntegrationTestFixtures} is used to preserve values between tests.
 * 
 * @version $Rev$ $Date$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class SqlIntegrationTestData extends SqlIntegrationTestCommonBase {

    private static final Logger LOG = LoggerFactory.getLogger(SqlIntegrationTestData.class);

    private static List<SimpleClass> simpleClassList1 = new ArrayList<SimpleClass>();
    private static List<SimpleClass> simpleClassList2 = new ArrayList<SimpleClass>();

    private static SimpleClassTwo simpleClassTwoA;
    private static SimpleClassTwo simpleClassTwoB;

    private static PrimitiveValuedEntity pve1;
    private static PrimitiveValuedEntity pve2;

    
    @Before
    public void setUpXactn() throws Exception {
        IsisContext.getTransactionManager().startTransaction();
    }

    @After
    public void tearDownXactn() throws Exception {
        IsisContext.getTransactionManager().endTransaction();
        assertThat(IsisContext.getTransactionManager().getTransaction().getState().isComplete(), is(true));
        
    }

    
    /**
     * Uses factory methods within the Isis framework to create the test data,
     * thus exercising the "create data" portion of the object store.
     * 
     * The Isis framework will be again be re-created in the next test unless the 
     * object store is "in-memory" (this is required since "in-memory" has to be
     * left alone for created data to still be present in the next test).
     */
    @Test
    public void testSetupStore() throws Exception {
        testSetup();
        setUpFactory();
        testCreate();
    }

    protected void testCreate() throws Exception {

        sqlDataClass = factory.newDataClass();

        sqlDataClass.setString("Test String");
        sqlDataClass.setDate(Data.applibDate);
        sqlDataClass.setSqlDate(Data.sqlDate);
        sqlDataClass.setMoney(Data.money);
        sqlDataClass.setDateTime(Data.dateTime);
        sqlDataClass.setTimeStamp(Data.timeStamp);
        sqlDataClass.setTime(Data.time);
        sqlDataClass.setColor(Data.color);
        sqlDataClass.setImage(Data.image);
        sqlDataClass.setPassword(Data.password);
        sqlDataClass.setPercentage(Data.percentage);

        // Setup SimpleClassTwo
        simpleClassTwoA = factory.newSimpleClassTwo();
        simpleClassTwoA.setText("A");
        simpleClassTwoA.setIntValue(999);
        simpleClassTwoA.setBooleanValue(true);

        simpleClassTwoB = factory.newSimpleClassTwo();
        simpleClassTwoB.setText("B");

        sqlDataClass.setSimpleClassTwo(simpleClassTwoA);

        // NumericClasses
        // standard min types
        pve2 = factory.newPrimitiveValuedEntity();
        LOG.info( "Bits to represent Double: " + Double.SIZE);
        pve2.setIntProperty(Data.intMinValue);
        pve2.setShortProperty(Data.shortMinValue);
        pve2.setLongProperty(Data.longMinValue);
        pve2.setDoubleProperty(Data.doubleMinValue);
        pve2.setFloatProperty(Data.floatMinValue);
        pve2.setCharProperty((char) (32)); // temporary work around: See ISIS-269

        sqlDataClass.setPrimitiveValuedEntityMin(pve2);

        // standard max types
        pve1 = factory.newPrimitiveValuedEntity();
        pve1.setIntProperty(Data.intMaxValue);
        pve1.setShortProperty(Data.shortMaxValue);
        pve1.setLongProperty(Data.longMaxValue);
        pve1.setDoubleProperty(Data.doubleMaxValue);
        pve1.setFloatProperty(Data.floatMaxValue);
        pve1.setCharProperty((char) (255));

        sqlDataClass.setPrimitiveValuedEntityMax(pve1);

        // Initialise collection1
        boolean bMustAdd = false;
        if (simpleClassList1.size() == 0) {
            bMustAdd = true;
        }
        for (final String string : Data.stringList1) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoA);
            sqlDataClass.addToSimpleClasses1(simpleClass);
            if (bMustAdd) {
                simpleClassList1.add(simpleClass);
            }
        }

        // Initialise collection2
        for (final String string : Data.stringList2) {
            final SimpleClass simpleClass = factory.newSimpleClass();
            simpleClass.setString(string);
            simpleClass.setSimpleClassTwoA(simpleClassTwoB);
            sqlDataClass.addToSimpleClasses2(simpleClass);
            if (bMustAdd) {
                simpleClassList2.add(simpleClass);
            }
        }

        // Initialise Image
        Image image = new Image(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
        sqlDataClass.setImage(image);

        // Save
        factory.save(sqlDataClass);

        setFixtureInitializationState(State.DONT_INITIALIZE, "in-memory");
    }

    /**
     * The actual "tests". Unless the test is using the "in-memory" object store 
     * the Isis framework is re-created, thus ensuring that no domain objects are
     * left over from the previous "create" step, forcing the objects to be created
     * via the object store.
     * 
     * Exercises the "restore data" portion of the object store.
     * 
     * Confirms that values and objects (single and collections) are loaded as expected.
     * Especially, it confirms that dates, times, etc, do not suffer from differences in
     * time zones between the database and the Isis framework.
     */
    @Test
    public void testTestAll() throws Exception {
        
        testLoad();

        setUpFactory();

        testString();
        setStringToDifferentValue();
        testSimpleClassCollection1Lazy();

        testMoney();
        testColor();
        testPassword();
        testPercentage();
        
        testImage();
        
        testStandardValueTypesMaxima();
        testStandardValueTypesMinima();

        testSingleReferenceLazy();
        testSimpleClassTwoReferenceLazy();

        testSimpleClassCollection1();
        testSimpleClassCollection2();

        testSingleReferenceResolve();
        testSimpleClassTwoReferenceResolve();
        testSimpleClassTwo();
        testUpdate1();
        testUpdate2();
        testUpdateCollectionIsDirty();
        testFindByMatchString();
        testFindByMatchEntity();

        testApplibDate();
        testSqlDate();
        testTime();
        testTimeStamp();
        testDateTimezoneIssue();
        testDateTime();
        
        // Test LIMIT statement
        testLimitCount();

        // Must be here so that the Isis framework is initialised for the next test package.
        setFixtureInitializationState(State.INITIALIZE);
    }

    private void testLoad() throws Exception {
        final List<SqlDataClass> dataClasses = factory.allDataClasses();
        IsisContext.getTransactionManager().flushTransaction();
        
        assertEquals(1, dataClasses.size());
        final SqlDataClass sqlDataClass = dataClasses.get(0);
        getSqlIntegrationTestFixtures().setSqlDataClass(sqlDataClass);

        setFixtureInitializationState(State.DONT_INITIALIZE);
    }

    private void testString() {
        assertEquals("Test String", sqlDataClass.getString());
    }

    private void setStringToDifferentValue() {
        sqlDataClass.setString("String 2");
    }

    private void testSimpleClassCollection1Lazy() {
        final List<SimpleClass> collection = sqlDataClass.simpleClasses1;

        assertEquals("collection size is not equal!", collection.size(), simpleClassList1.size());
    }

    /**
     * Test {@link SqlDataClass} {@link Date} field.
     * 
     * @throws Exception
     */
    private void testApplibDate() {

        LOG.info("Test: testDate() '2010-3-5' = 1267747200000");

        // 2010-3-5 = 1267747200000
        LOG.info( "applibDate.dateValue() as String: " + Data.applibDate);
        LOG.info( "applibDate.dateValue() as Long: " + Data.applibDate.getMillisSinceEpoch());

        // 2010-3-5 = 1267747200000
        LOG.info( "sqlDataClass.getDate() as String: " + sqlDataClass.getDate());
        LOG.info( "sqlDataClass.getDate().getTime() as Long: " + sqlDataClass.getDate().getMillisSinceEpoch());

        if (!Data.applibDate.isEqualTo(sqlDataClass.getDate())) {
            fail("Applib date: Test '2010-3-5', expected " + Data.applibDate.toString() + ", but got "
                + sqlDataClass.getDate().toString() + ". Check log for more info.");
        } else {
            // LOG.log(Level.INFO,
            // "SQL applib.value.date: test passed! Woohoo!");
        }

    }

    /**
     * Test {@link SqlDataClass} {@link java.sql.Date} field.
     * 
     * @throws Exception
     */
    private void testSqlDate() {

        LOG.info( "Test: testSqlDate() '2011-4-8' == 1302220800000");

        // 2011-4-8 = 1302220800000
        LOG.info( "sqlDate.toString() as String:" + Data.sqlDate); // shows
        // as
        // 2011-04-07
        LOG.info( "sqlDate.getTime() as Long:" + Data.sqlDate.getTime());

        // 2011-4-8 = 1302220800000
        LOG.info( "sqlDataClass.getSqlDate() as String:" + sqlDataClass.getSqlDate()); // shows
                                                                                                 // as
        // 2011-04-07
        LOG.info( "sqlDataClass.getSqlDate().getTime() as Long:" + sqlDataClass.getSqlDate().getTime());

        if (Data.sqlDate.compareTo(sqlDataClass.getSqlDate()) != 0) {
            fail("SQL date: Test '2011-4-8', expected " + Data.sqlDate.toString() + ", but got "
                + sqlDataClass.getSqlDate().toString() + ". Check log for more info.");
        } else {
            // LOG.log(Level.INFO, "SQL date: test passed! Woohoo!");
        }

    }/**/

    private void testDateTimezoneIssue() {
        /*
         * At the moment, applib Date and java.sql.Date are restored from ValueSemanticsProviderAbstractTemporal with an
         * explicit hourly offset that comes from the timezone. I.e. in South Africa, with TZ +2h00, they have an
         * implicit time of 02h00 (2AM). This can potentially seriously screw up GMT-X dates, which, I suspect, will
         * actually be set to the dat BEFORE.
         * 
         * This test is a simple test to confirm that date/time before and after checks work as expected.
         */

        DateTime dateTime = sqlDataClass.getDateTime(); // new DateTime(2010, 3, 5, 1, 23);
        Date date = sqlDataClass.getDate(); // new Date(2010, 3, 5);

        // java.sql.Date sqlDate = sqlDataClass.getSqlDate(); // "2010-03-05"
        // assertTrue("dateTime's value (" + dateTime.dateValue() + ") should be after java.sql.date's (" + sqlDate +
        // ")",
        // dateTime.dateValue().after(sqlDate));

        assertTrue("dateTime's value (" + dateTime.dateValue() + ") should be after date's (" + date + ")", dateTime
            .dateValue().after(date.dateValue()));

    }

    /**
     * Test {@link Money} type.
     */
    private void testMoney() {
        assertEquals(Data.money, sqlDataClass.getMoney());
    }

    /**
     * Test {@link DateTime} type.
     */
    private void testDateTime() {

        LOG.info( "Test: testDateTime()");
        LOG.info( "sqlDataClass.getDateTime() as String:" + sqlDataClass.getDateTime());
        LOG.info( "dateTime.toString() as String:" + Data.dateTime);

        LOG.info( "sqlDataClass.getDateTime().getTime() as Long:"
            + sqlDataClass.getDateTime().millisSinceEpoch());
        LOG.info( "dateTime.getTime() as Long:" + Data.dateTime.millisSinceEpoch());

        if (!Data.dateTime.equals(sqlDataClass.getDateTime())) {
            fail("DateTime " + Data.dateTime.toString() + " is not expected " + sqlDataClass.getDateTime().toString());
        }
    }

    /**
     * Test {@link TimeStamp} type.
     */
    private void testTimeStamp() {
        assertTrue(
            "TimeStamp " + sqlDataClass.getTimeStamp().toString() + " does not equal expected "
                + Data.timeStamp.toString(), Data.timeStamp.isEqualTo(sqlDataClass.getTimeStamp()));
    }

    /**
     * Test {@link Time} type.
     */
    /**/
    private void testTime() {
        assertNotNull("sqlDataClass is null", sqlDataClass);
        assertNotNull("getTime() is null", sqlDataClass.getTime());
        assertTrue("Time 14h56: expected " + Data.time.toString() + ", but got " + sqlDataClass.getTime().toString(),
            Data.time.isEqualTo(sqlDataClass.getTime()));
    }

    /**
     * Test {@link Color} type.
     */
    private void testColor() {
        assertEquals(Data.color, sqlDataClass.getColor());
    }

    /**
     * Test {@link Image} type.
     */
    private void testImage() {
        Image image1 = new Image(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
        Image image2 = sqlDataClass.getImage();
        // REVIEW: Only XML persistor fails here...
        if (!persistenceMechanismIs("xml")) {
            assertImagesEqual(image1, image2);
        }
    }

    private void assertImagesEqual(Image image2, Image image3) {
        assertEquals(image2.getHeight(), image3.getHeight());
        assertEquals(image2.getWidth(), image3.getWidth());
        boolean same = true;
        int i = 0, j = 0;
        int p1 = 0, p2 = 0;
        String error = "";
        int[][] i1 = image2.getImage(), i2 = image3.getImage();
        for (i = 0; same && i < image2.getHeight(); i++) {
            int[] r1 = i1[i], r2 = i2[i];
            for (j = 0; same && j < image2.getWidth(); j++) {
                p1 = r1[j];
                p2 = r2[j];
                if (p1 != p2) {
                    same = false;
                    error = "Images differ at i = " + i + ", j = " + j + ", " + p1 + " is not " + p2 + "!";
                    break;
                }
            }
        }
        assertTrue(error, same);
    }

    /**
     * Test {@link Password} type.
     */
    private void testPassword() {
        assertEquals(Data.password, sqlDataClass.getPassword());
    }

    /**
     * Test {@link Percentage} type.
     */
    private void testPercentage() {
        assertEquals(Data.percentage, sqlDataClass.getPercentage());
    }

    private void testStandardValueTypesMaxima() {
        final PrimitiveValuedEntity pveMax = sqlDataClass.getPrimitiveValuedEntityMax();

        assertEquals(Data.shortMaxValue, pveMax.getShortProperty());
        assertEquals(Data.intMaxValue, pveMax.getIntProperty());
        assertEquals(Data.longMaxValue, pveMax.getLongProperty());
        assertEquals(Data.doubleMaxValue, pveMax.getDoubleProperty(), 0.00001f); // fails
        // in
        assertEquals(Data.floatMaxValue, pveMax.getFloatProperty(), 0.00001f);
    }

    private void testStandardValueTypesMinima() {
        final PrimitiveValuedEntity pveMin = sqlDataClass.getPrimitiveValuedEntityMin();

        assertEquals(Data.shortMinValue, pveMin.getShortProperty());
        assertEquals(Data.intMinValue, pveMin.getIntProperty());
        assertEquals(Data.longMinValue, pveMin.getLongProperty());
        assertEquals(Data.doubleMinValue, pveMin.getDoubleProperty(), 0.00001f); // fails
        // in
        // MySQL
        // =
        // infinity
        assertEquals(Data.floatMinValue, pveMin.getFloatProperty(), 0.00001f);
    }

    /**
     * Test {@link StringCollection} type.
     */
    /*
     * public void testStringCollection(){ SqlDataClass sqlDataClass = SqlIntegrationTestSingleton.getPerson();
     * List<String> collection = sqlDataClass.getStringCollection(); int i = 0; for (String string : collection) {
     * assertEquals(SqlIntegrationTestCommon.stringList.get(i++), string); } }
     */

    private void testSingleReferenceLazy() {
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        if (!persistenceMechanismIs("in-memory")) {
            assertEquals(null, a.text); // must check direct value, as
            // framework can auto-resolve, if you use getText()
        }
    }

    /**
     * Test a collection of {@link SimpleClass} type.
     */
    private void testSimpleClassCollection1() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();

        assertEquals("collection size is not equal!", simpleClassList1.size(), collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : simpleClassList1) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    /**
     * Test another collection of {@link SimpleClass} type.
     */
    private void testSimpleClassCollection2() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses2();

        assertEquals("collection size is not equal!", simpleClassList2.size(), collection.size());

        int i = 0;
        for (final SimpleClass simpleClass : simpleClassList2) {
            assertEquals(simpleClass.getString(), collection.get(i++).getString());
        }
    }

    private void testSimpleClassTwoReferenceLazy() {
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

    private void testSingleReferenceResolve() {
        final SimpleClassTwo a = sqlDataClass.getSimpleClassTwo();
        factory.resolve(a);
        assertEquals(simpleClassTwoA.getText(), a.getText());
    }

    private void testSimpleClassTwoReferenceResolve() {
        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        for (final SimpleClass simpleClass : collection) {
            final SimpleClassTwo a = simpleClass.getSimpleClassTwoA();
            factory.resolve(a);
            assertEquals(simpleClassTwoA.getText(), a.getText());
            assertEquals(simpleClassTwoA.getIntValue(), a.getIntValue());
            assertEquals(simpleClassTwoA.getBooleanValue(), a.getBooleanValue());
        }
    }

    private void testSimpleClassTwo() {
        final SqlDomainObjectRepository factory = getSqlIntegrationTestFixtures().getSqlDataClassFactory();
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());
        for (final SimpleClassTwo simpleClass : classes) {
            // assertEquals(simpleClassTwoA.getText(), simpleClass.getText());
            assertTrue("AB".contains(simpleClass.getText()));
        }
    }

    private void testUpdate1() {
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        simpleClass.setText("XXX");
        simpleClass.setBooleanValue(false);
        simpleClassTwoA.setBooleanValue(false);

        setFixtureInitializationStateIfNot(State.INITIALIZE, "in-memory");
    }

    private void testUpdate2() {
        final List<SimpleClassTwo> classes = factory.allSimpleClassTwos();
        assertEquals(2, classes.size());

        final SimpleClassTwo simpleClass = classes.get(0);
        assertEquals("XXX", simpleClass.getText());
        assertEquals(simpleClassTwoA.getBooleanValue(), simpleClass.getBooleanValue());

        setFixtureInitializationState(State.DONT_INITIALIZE);
    }

    private void testUpdateCollectionIsDirty() {

        final List<SqlDataClass> sqlDataClasses = factory.allDataClasses();
        final SqlDataClass sqlDataClass = sqlDataClasses.get(0);

        final List<SimpleClass> collection = sqlDataClass.getSimpleClasses1();
        final SimpleClass simpleClass1 = collection.get(0);

        collection.remove(simpleClass1);

        // REVIEW: I'm very doubtful about this...
        // what exactly is meant by updating an internal collection?
        if (!persistenceMechanismIs("xml")) {
            factory.update(collection);
        }

        factory.update(sqlDataClass);
    }
    
    private void testLimitCount() {
        final List<SimpleClass> subset = factory.someSimpleClasses(0, 2);
        assertEquals(2, subset.size());
        // TODO: This test does not confirm that the *right* 2 were returned. 
    }

    private void testFindByMatchString() {
        final SimpleClass simpleClassMatch = new SimpleClass();
        simpleClassMatch.setString(Data.stringList1.get(1));

        final List<SimpleClass> classes = factory.allSimpleClassesThatMatch(simpleClassMatch);
        assertEquals(1, classes.size());

    }

    private void testFindByMatchEntity() {
        final List<SimpleClassTwo> classTwos = factory.allSimpleClassTwos();

        final SimpleClass simpleClassMatch = new SimpleClass();
        simpleClassMatch.setSimpleClassTwoA(classTwos.get(0));

        final List<SimpleClass> classes = factory.allSimpleClassesThatMatch(simpleClassMatch);

        // TODO: Why is this hack required?
        if (!getProperties().getProperty("isis.persistor").equals("in-memory")) {
            assertEquals(Data.stringList1.size(), classes.size());
        } else {
            assertEquals(Data.stringList1.size() + 2, classes.size());
        }
    }
}
