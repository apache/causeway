package org.nakedobjects.xat;



public interface TestClass extends TestNaked {
    /**
     * Finds the instance whose title matched the one specified. A match is any
     * substring matching the specified text, and the result is the first object
     * found that gives such a match, i.e. only one object is returned even
     * though more than one match might occur.
     */
    TestObject findInstance(String title);
    
    /**
     * Get the instances of this class.
     */
    TestObject instances();

    /**
     * Creates a new instance of this class.
     */
    TestObject newInstance();
    
    
    
    

    public void assertActionExists(String name);

    public void assertActionExists(String name, TestNaked[] parameters);

    public void assertActionExists(String name, TestNaked parameter);

    /**
     * Check that the specified object menu item is currently invisible. If it
     * is visible the test fails.
     */
    void assertActionInvisible(String name);

    void assertActionInvisible(String name, TestNaked[] parameters);

    /**
     * Check that the specified object menu item is currently invisible. If it
     * is visible the test fails.
     */
    void assertActionInvisible(String name, TestNaked parameter);

    /**
     * Check that the specified object menu item is currently disabled. If it is
     * enabled the test fails.
     */
    void assertActionUnusable(String name);

    void assertActionUnusable(String name, TestNaked[] parameters);

    /**
     * Check that dragged object cannot be dropped on this object. If it can be
     * dropped the test fails.
     */
    void assertActionUnusable(String name, TestNaked parameter);

    /**
     * Check that the specified object menu item is currently available. If it
     * is disabled the test fails.
     */
    void assertActionUsable(String name);

    void assertActionUsable(String name, TestNaked[] parameter);

    /**
     * Check that dragged object can be dropped on this object. If it cannot be
     * dropped the test fails.
     */
    void assertActionUsable(String name, TestNaked parameter);

    /**
     * Check that the specified object menu item is currently visible. If it is
     * invisible the test fails.
     */
    void assertActionVisible(String name);

    void assertActionVisible(String name, TestNaked[] parameters);

    /**
     * Check that the specified object menu item is currently visible. If it is
     * invisible the test fails.
     */
    void assertActionVisible(String name, TestNaked parameter);


    /**
     * Invokes this object's zero-parameter action method of the the given name.
     * This mimicks the right-clicking on an object and subsequent selection of
     * a menu item.
      */
    TestObject invokeAction(String name);

    TestObject invokeAction(String name, TestNaked[] parameter);

    /**
     * Drop the specified view (object) onto this object and invoke the
     * corresponding <code>action</code> method. A new view representing the
     * returned object, if any is returned, from the invoked <code>action</code>
     * method is returned by this method.
     * 
     * @group action
     */
    TestObject invokeAction(String name, TestNaked parameter);


}

/*
 Naked Objects - a framework that exposes behaviourally complete
 business objects directly to the user.
 Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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