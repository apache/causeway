package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;


public class TestClassDecorator implements TestClass {
    private final TestClass wrappedObject;

    public TestClassDecorator(TestClass wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public TestObject findInstance(String title) {
        return wrappedObject.findInstance(title);
    }

    public Naked getForNaked() {
        return wrappedObject.getForNaked();
    }

    public String getTitle() {
        return wrappedObject.getTitle();
    }

    public TestCollection instances() {
        return wrappedObject.instances();
    }

    public TestNaked invokeAction(String name, TestNaked[] parameters) {
        return wrappedObject.invokeAction(name, parameters);
    }

    public TestCollection invokeActionReturnCollection(String name, TestNaked[] parameters) {
        return wrappedObject.invokeActionReturnCollection(name, parameters);
    }
    
    public TestObject invokeActionReturnObject(String name, TestNaked[] parameters) {
        return wrappedObject.invokeActionReturnObject(name, parameters);
    }
    
    public TestObject newInstance() {
        return wrappedObject.newInstance();
    }

    public void setForNaked(Naked object) {
        wrappedObject.setForNaked(object);
    }

    public void assertActionExists(String name) {
        wrappedObject.assertActionExists(name);
    }

    public void assertActionExists(String name, TestNaked[] parameters) {
        wrappedObject.assertActionExists(name, parameters);
    }

    public void assertActionExists(String name, TestNaked parameter) {
        wrappedObject.assertActionExists(name, parameter);
    }

    public void assertActionInvisible(String name) {
        wrappedObject.assertActionInvisible(name);
    }

    public void assertActionInvisible(String name, TestNaked[] parameters) {
        wrappedObject.assertActionInvisible(name, parameters);
    }

    public void assertActionInvisible(String name, TestNaked parameter) {
        wrappedObject.assertActionInvisible(name, parameter);
    }

    public void assertActionUnusable(String name) {
        wrappedObject.assertActionUnusable(name);
    }

    public void assertActionUnusable(String name, TestNaked[] parameters) {
        wrappedObject.assertActionUnusable(name, parameters);
    }

    public void assertActionUnusable(String name, TestNaked parameter) {
        wrappedObject.assertActionUnusable(name, parameter);
    }

    public void assertActionUsable(String name) {
        wrappedObject.assertActionUsable(name);
    }

    public void assertActionUsable(String name, TestNaked[] parameters) {
        wrappedObject.assertActionUsable(name, parameters);
    }

    public void assertActionUsable(String name, TestNaked parameter) {
        wrappedObject.assertActionUsable(name, parameter);
    }

    public void assertActionVisible(String name) {
        wrappedObject.assertActionVisible(name);
    }

    public void assertActionVisible(String name, TestNaked[] parameters) {
        wrappedObject.assertActionVisible(name, parameters);
    }

    public void assertActionVisible(String name, TestNaked parameter) {
        wrappedObject.assertActionVisible(name, parameter);
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