package org.nakedobjects.xat.performance;

import org.nakedobjects.utility.Profiler;
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
        Profiler timer = start("associate " + draggedView.getTitle() + " in " + fieldName);
        super.associate(fieldName, draggedView);
        stop(timer);
    }

    public void clearAssociation(String fieldName) {
        Profiler timer = start("clear " + fieldName);
        super.clearAssociation(fieldName);
        stop(timer);
    }

    public void clearAssociation(String fieldName, String title) {
        Profiler timer = start("clear " + fieldName);
        super.clearAssociation(fieldName, title);
        stop(timer);
    }

    public void fieldEntry(final String fieldName, final String value) {
        Profiler timer = start("field entry '" + value + "' into " + fieldName);
        super.fieldEntry(fieldName, value);
        stop(timer);
    }

    public TestObject invokeAction(final String name, final TestNaked[] parameters) {
        String parameterList = "";
        for (int i = 0, l = parameters.length; i < l; i++) {
            parameterList += (i > 0 ? "," : "") + parameters[i].getTitle();
        }
        Profiler timer = start("action '" + name + "' with " + parameters);
        TestObject result = super.invokeAction(name, parameters);
        stop(timer);
        return result;
    }

    private Profiler start(String name) {
        Profiler profile = new Profiler(name);
        Delay.userDelay(4, 8);
        profile.start();
        return profile;
    }

    private void stop(Profiler profiler) {
        profiler.stop();
        doc.record(profiler);
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