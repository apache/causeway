package org.nakedobjects.xat.performance;

import org.nakedobjects.xat.TestNaked;
import org.nakedobjects.xat.TestObject;
import org.nakedobjects.xat.TestObjectDecorator;


public class TimingTestObject extends TestObjectDecorator {
    private final TimingDocumentor doc;

    public TimingTestObject(final TestObject wrappedObject, final TimingDocumentor documentor) {
        super(wrappedObject);
        this.doc = documentor;
    }

    
    public void associate(final String fieldName, final TestObject draggedView) {
        Timer timer = new Timer("associate " + draggedView.getTitle() + " in " + fieldName);
        timer.userDelay(4, 8);
        timer.start();
        super.associate(fieldName, draggedView);
        timer.stop();
        doc.record(timer);
    }

    public void fieldEntry(final String fieldName, final String value) {
        Timer timer = new Timer("field entry '" + value + "' into " + fieldName);
        timer.userDelay(4, 8);
        timer.start();
        super.fieldEntry(fieldName, value);
        timer.stop();
        doc.record(timer);
    }

    public TestObject invokeAction(final String name) {
        Timer timer = new Timer("action '" + name + "'");
        timer.userDelay(1, 4);
        timer.start();
        TestObject result = super.invokeAction(name);
        timer.stop();
        doc.record(timer);
        return result;
    }

    public TestObject invokeAction(final String name, final TestObject parameter) {
        Timer timer = new Timer("action '" + name + "' with " + parameter.getTitle());
        timer.userDelay(2, 4);
        timer.start();
        TestObject result = super.invokeAction(name, parameter);
        timer.stop();
        doc.record(timer);
        return result;
    }
    
    public TestObject invokeAction(final String name, final TestNaked[] parameters) {
        String parameterList = "";
        for (int i = 0, l = parameters.length; i < l; i++) {
            parameterList += (i > 0 ? "," : "") + parameters[i].getTitle();
        }
        Timer timer = new Timer("action '" + name + "' with " + parameters);
        timer.userDelay(2, 4);
        timer.start();
        TestObject result = super.invokeAction(name, parameters);
        timer.stop();
        doc.record(timer);
        return result;
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