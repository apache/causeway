package org.nakedobjects.object;




public abstract class NakedObjectStoreFieldsTestCase extends NakedObjectStoreTestCase {

    public NakedObjectStoreFieldsTestCase(String name) {
        super(name);
    }
     
    public void testSave() throws Exception {
        Oid oid = nextOid();

        // extend to test associations
        Person person = new Person();
        person.setOid(oid);
        person.getName().setValue("Samuel");
        person.getSalary().setValue(10.0);
        
        objectStore.save(person);

        restartObjectStore();
        NakedObjectSpecification spec = person.getSpecification();
        
        assertEquals("Samuel", ((Person) objectStore.getObject(oid, spec)).name.stringValue());
        assertEquals(10.0f, ((Person) objectStore.getObject(oid, spec)).salary.floatValue(), 0.0f);
    }
    
    public void testTextStringValue() throws Exception {
        ObjectContainingTextString object = new ObjectContainingTextString();
        Oid oid = nextOid();
        NakedObjectSpecification spec = object.getSpecification();
        object.setOid(oid);
        objectStore.save(object);

        restartObjectStore();

        ObjectContainingTextString restored = (ObjectContainingTextString) objectStore.getObject(oid, spec);

        assertEquals(restored.getTextString(), restored.getTextString());
    }

/*
    public void testValues() throws Exception {
        ValueObjectExample e1 = new ValueObjectExample();
        e1.setOid(nextOid());

        // get each value object
        Date date = e1.getDate();
        FloatingPointNumber floatingPoint = e1.getFloatingPoint();
        Label label = e1.getLabel();

        //		label.setValue("Abc");
        Logical logical = e1.getLogical();
        Money money = e1.getMoney();
        Option option = e1.getOption();
        Percentage percentage = e1.getPercentage();
        TextString textString = e1.getTextString();
        textString.setValue("Abcd");

        Time time = e1.getTime();
        TimeStamp timestamp = e1.getTimeStamp();
        URLString urlString = e1.getUrlString();
        WholeNumber wholeNumber = e1.getWholeNumber();
        wholeNumber.setValue(198218);

        // make persistent
        objectStore.createObject(e1);

        restartObjectStore();

        ValueObjectExample e2 = (ValueObjectExample) objectStore.getObject(e1.getOid(), e1.getSpecification());

        // check each value
        assertTrue("Dates differ", date.isSameAs(e2.getDate()));
        assertTrue("Floating points differ", floatingPoint.isSameAs(e2.getFloatingPoint()));
        assertTrue("Labels differ '" + label.stringValue() + "' '" + e2.getLabel().stringValue() + "'", label.isSameAs(e2
                .getLabel()));
        assertTrue("Logicals differ", logical.isSameAs(e2.getLogical()));
        assertTrue("Moneys differ", money.isSameAs(e2.getMoney()));
        assertTrue("Options differ", option.isSameAs(e2.getOption()));
        assertTrue("Percentages differ", percentage.isSameAs(e2.getPercentage()));
        assertTrue("TextStrings differ", textString.isSameAs(e2.getTextString()));
        assertTrue("Times differ " + time + " " + e2.getTime(), time.isSameAs(e2.getTime()));
        assertTrue("Timestamps differ " + timestamp + " " + e2.getTimeStamp(), timestamp.isSameAs(e2.getTimeStamp()));
        assertTrue("URLStrings differ", urlString.isSameAs(e2.getUrlString()));
        assertTrue("WholeNumbers differ", wholeNumber.isSameAs(e2.getWholeNumber()));
    }
    */
    
 }

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 S*/