package org.nakedobjects.xat.html;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.xat.TestNaked;
import org.nakedobjects.xat.TestObject;
import org.nakedobjects.xat.TestObjectDecorator;
import org.nakedobjects.xat.TestValue;


public class HtmlTestObject extends TestObjectDecorator {
    private HtmlDocumentor doc;

    public HtmlTestObject(TestObject wrappedObject, HtmlDocumentor documentor) {
        super(wrappedObject);
        this.doc = documentor;
    }

    public void assertActionUnusable(String name) {
        super.assertActionUnusable(name);
        doc("Right clicking on the " + doc.objectString(getForObject()));
        doc(" shows that <strong>" + name);
        doc(getAction(name).hasReturn() ? "..." : "");
        doc("</strong> is not currently available. ");
    }

    public void assertActionUsable(String name, TestNaked parameter) {
        super.assertActionUsable(name, parameter);
        doc("note that it can't be dropped onto the ");
        doc(doc.objectString(getForObject()) + ". ");
    }

    public void assertFieldContains(String fieldName, String expectedValue) {
        super.assertFieldContains(fieldName, expectedValue);
        if (getField(fieldName) instanceof TestValue) {
            doc("<p>Note that the field <em>" + fieldName + "</em> in the " + doc.objectString(getForObject())
                    + " is now set to '" + getField(fieldName).getForObject().titleString() + "'.");
        } else {
            doc("<p>Note that object in the field <em>" + fieldName + "</em> of " + doc.objectString(getForObject())
                    + " now has a title of '" + getField(fieldName).getForObject().titleString() + "'.");
        }
    }

    public void assertFieldContains(String message, String fieldName, NakedValue expectedValue) {
        super.assertFieldContains(message, fieldName, expectedValue);
        doc("<p>Note that the field <em>" + fieldName + "</em> in the " + doc.objectString(getForObject()) + " is now set to '"
                + getField(fieldName).getForObject().titleString() + "'.");
    }

    public void assertFieldContains(String fieldName, TestObject expectedView) {
        super.assertFieldContains(fieldName, expectedView);
        NakedObject actualValue = (NakedObject) getForObject();
        if (actualValue instanceof NakedCollection) {
            doc("<em>" + fieldName + "</em> contains the " + doc.objectString(expectedView.getForObject()) + "; ");
        } else {
            doc("<em>" + fieldName + "</em> contains the " + doc.objectString(actualValue) + "; ");
        }
    }

    public void assertFieldDoesNotContain(String fieldName, String testValue) {
        super.assertFieldDoesNotContain(fieldName, testValue);
        if (getField(fieldName) instanceof TestValue) {
            doc("<p>Note that the field <em>" + fieldName + "</em> in the " + doc.objectString(getForObject())
                    + " does not contain a value of '" + testValue + "'.");
        } else {
            doc("<p>Note that object in the field <em>" + fieldName + "</em> of " + doc.objectString(getForObject())
                    + " does not contains an object titled of '" + getField(fieldName).getForObject().titleString() + "'.");
        }
    }

    public void assertFieldDoesNotContain(String message, String fieldName, NakedValue expectedValue) {
        super.assertFieldDoesNotContain(message, fieldName, expectedValue);
        doc("<p>Note that the field <em>" + fieldName + "</em> in the " + doc.objectString(getForObject())
                + " does not contain '" + getField(fieldName).getForObject().titleString() + "'.");
    }

    public void assertFieldDoesNotContain(String fieldName, TestObject testView) {
        super.assertFieldDoesNotContain(fieldName, testView);
        NakedObject actualValue = (NakedObject) getForObject();
        if (actualValue instanceof NakedCollection) {
            doc("<em>" + fieldName + "</em> does not contain an instance of " + doc.objectString(testView.getForObject()) + "; ");
        } else {
            doc("<em>" + fieldName + "</em> does not contains " + doc.objectString(actualValue) + "; ");
        }
    }

    public void assertNoOfElements(String collectionName, int noOfElements) {
        super.assertNoOfElements(collectionName, noOfElements);
        doc("<em>" + collectionName + "</em> contains " + noOfElements + " elements; ");
    }

    public void assertNoOfElementsNotEqual(String collectionName, int noOfElements) {
        super.assertNoOfElementsNotEqual(collectionName, noOfElements);
        doc("<em>" + collectionName + "</em> does not contain " + noOfElements + " elements; ");
    }

    public void assertTitleEquals(String expectedTitle) {
        super.assertTitleEquals(expectedTitle);
        doc("Note the new  title: " + doc.objectString(getForObject()) + " .");
    }

    public void associate(String fieldName, TestObject draggedView) {
        super.associate(fieldName, draggedView);
        doc("Drop it into the ");
        docln("<em>" + fieldName + "</em> field within the " + doc.objectString(getForObject()) + ". ");
    }

    private void doc(String text) {
        doc.doc(text);
    }

    private void docln(String text) {
        doc.docln(text);
    }

    public void fieldEntry(String name, String value) {
        doc("Set the <em>" + name + "</em> field within the " + doc.objectString(getForObject()));
        docln(" to <code>" + value + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>. ");
        super.fieldEntry(name, value);
    }

    public TestObject getAssociation(String title) {
        NakedCollection collection = (NakedCollection) getForObject();
        doc("Select the instance within " + doc.objectString(collection)
                + " (<img width=\"16\" height=\"16\" align=\"Center\" src=\"images/Collection16.gif\">) "
                + " class whose title matches <strong>" + title + "</strong>");
        TestObject result = super.getAssociation(title);
        docln(", which returns " + doc.objectString(result.getForObject()) + ". ");
        return result;
    }

    public TestObject invokeAction(String name) {
        TestObject result = super.invokeAction(name);
        doc("Right click on the " + doc.objectString(getForObject()));
        doc(" and select the <strong>" + name);
        doc(getAction(name).hasReturn() ? "..." : "");
        doc("</strong> action");
        doc((result == null) ? "." : ", which returns " + objectString(result.getForObject()) + ". ");
        return result;
    }

    public TestObject invokeAction(String name, TestNaked parameter) {
        doc("drop it onto the ");
        doc(doc.objectString(this.getForObject()));
        TestObject result = super.invokeAction(name, parameter);
        NakedObject object = (NakedObject) result.getForObject();
        if (object == null) {
            docln(". ");
        } else {
            docln(", which returns " + objectString(object) + ". ");
        }
        return result;
    }

    private String objectString(Naked object) {
        return doc.objectString(object);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
 */