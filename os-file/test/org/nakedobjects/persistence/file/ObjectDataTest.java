package org.nakedobjects.persistence.file;

import org.nakedobjects.object.LocalReflectionFactory;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.SerialOid;
import org.nakedobjects.object.ValueObjectExample;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.DateTime;
import org.nakedobjects.object.value.FloatingPointNumber;
import org.nakedobjects.object.value.Label;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.object.value.Percentage;
import org.nakedobjects.object.value.TestClock;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.URLString;
import org.nakedobjects.object.value.WholeNumber;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;


public class ObjectDataTest extends NakedObjectTestCase {
   private ObjectData data;
	
	public ObjectDataTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ObjectDataTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        PropertyConfigurator.configure("log4j.testing.properties");
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        MockObjectManager manager = MockObjectManager.setup();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
        
        NakedObjectSpecification type = NakedObjectSpecification.getNakedClass(ValueObjectExample.class.getName());
        data = new ObjectData(type, new SerialOid(1));
        
        new TestClock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test() {
        NakedObjectSpecification roleType = NakedObjectSpecification.getNakedClass(Role.class.getName());
        ObjectData roleData = new ObjectData(roleType, new SerialOid(1));

        roleData.set("Name", "supervisor");
        roleData.set("Person", new SerialOid(2));

        assertEquals("supervisor", roleData.get("Name"));
        assertEquals(new SerialOid(2), roleData.get("Person"));
    }

    public void testText() {
    	String fieldName = "Text String";
    	TextString original = new TextString("supervisor");
    	data.saveValue(fieldName, original);

    	TextString restored = new TextString();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    	
    }

    public void testLabel() {
    	String fieldName = "Label";
    	Label original = new Label("supervisor");
    	data.saveValue(fieldName, original);

    	Label restored = new Label();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    	
    }

    public void testURLString() {
    	String fieldName = "Url String";
    	URLString original = new URLString("http://www.nakedobjects.org/discuss.html");
    	data.saveValue(fieldName, original);

    	URLString restored = new URLString();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    	
    }

    public void testOption() {
    	String fieldName = "Option";
    	String[] options = new String[] {"one", "two", "three"};
		Option original = new Option(options, 1);
    	data.saveValue(fieldName, original);

    	Option restored = new Option(options);
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    	assertEquals(1, restored.getSelectionIndex());
    	assertEquals("two", restored.getSelection());
    	
    }

    public void testLogical() {
    	String fieldName = "Label";
    	Logical original = new Logical(true);
    	data.saveValue(fieldName, original);

    	Logical restored = new Logical();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    	
    }

    
    public void testMoney() {
    	String fieldName = "Money";
    	Money original = new Money(18.33);
    	data.saveValue(fieldName, original);

    	Money restored = new Money();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }

    public void testFloatingPoint() {
    	String fieldName = "Money";
    	FloatingPointNumber original = new FloatingPointNumber(18.33);
    	data.saveValue(fieldName, original);

    	FloatingPointNumber restored = new FloatingPointNumber();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }

    public void testWholeNumber() {
    	String fieldName = "Money";
    	WholeNumber original = new WholeNumber(17);
    	data.saveValue(fieldName, original);

    	WholeNumber restored = new WholeNumber();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }

    public void testPercentage() {
    	String fieldName = "Money";
    	Percentage original = new Percentage(18.33f);
    	data.saveValue(fieldName, original);

    	Percentage restored = new Percentage();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }

    public void testDate() {
        String fieldName = "Date";
        Date original = new Date(2004, 3, 12);
		data.saveValue(fieldName, original);

        Date restored = new Date();
        data.restoreValue(fieldName, restored);
        assertEquals(original, restored);
    }

    public void testTime() {
    	String fieldName = "Date";
    	Time original = new Time(10, 35);
    	data.saveValue(fieldName, original);

    	Time restored = new Time();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }

    public void testDateTime() {
    	String fieldName = "Date";
    	DateTime original = new DateTime(2004, 3, 12, 10, 35, 30);
    	data.saveValue(fieldName, original);

    	DateTime restored = new DateTime();
    	data.restoreValue(fieldName, restored);
    	assertEquals(original, restored);
    }
    
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
