package org.nakedobjects.xat.performance;

import org.nakedobjects.xat.TestClass;
import org.nakedobjects.xat.TestClassDecorator;
import org.nakedobjects.xat.TestNaked;
import org.nakedobjects.xat.TestObject;


public class TimingTestClass extends TestClassDecorator {
    private final TimingDocumentor doc;

    public TimingTestClass(final TestClass wrappedObject, final TimingDocumentor documentor) {
        super(wrappedObject);
        this.doc = documentor;
    }

    public TestObject findInstance(final String title) {
        Timer timer = new Timer("Find instance of " + getTitle());
        timer.userDelay(4, 8);
        timer.start();
        TestObject result = super.findInstance(title);
        timer.stop();
        doc.record(timer);
        return result;
    }

    public TestObject newInstance() {
        Timer timer = new Timer("New instance of " + getTitle());
        timer.userDelay(4, 8);
        timer.start();
        TestObject instance = super.newInstance();
        instance.getForNaked();
        timer.stop();
       doc.record(timer);
        return instance;
    }
    

    public TestNaked invokeAction(final String name, final TestNaked[] parameters) {
        StringBuffer parameterList = new StringBuffer();
        for (int i = 0; i < parameters.length; i++) {
            if(i > 0) {
                parameterList.append(", ");
            }
            parameterList.append(parameters[i].getTitle());
        }
        
        Timer timer = new Timer("class action '" + name + "' with " + parameterList);
        timer.userDelay(4, 8);
       timer.start();
        TestNaked result = super.invokeAction(name, parameters);
        timer.stop();
        doc.record(timer);
        return result;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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