package org.nakedobjects.object.exploration;



public abstract class AbstractExplorationFixture implements ExplorationFixture {
    private Object container;
    
    /**
     * @deprecated
     */
    protected final Object createInstance(Class type) {
        return ((ExplorationSetUp) container).createInstance(type);
    }

    /**
     * @deprecated
     */
    protected final Object createInstance(String className) {
        return ((ExplorationSetUp) container).createInstance(className);
    }
    
    /**
     * @deprecated
     */
    protected void resetClock() {
 //       getContainer().resetClock();
    }

    /**
     * @deprecated
     */
    public void setTime(int hour, int minute) {
   //     getContainer().setTime(hour, minute);
    }

    /**
     * @deprecated
     */
    public void setDate(int year, int month, int day) {
   //     getContainer().setDate(year, month, day);
    }

    protected Object getContainer() {
        return container;
    }

    /** @deprecated */
    protected final boolean needsInstances(Class cls) {
        return true;
    }

    /** @deprecated */
    protected final boolean needsInstances(String className) {
        return true;
    }

    /**
     * @deprecated
     */
   protected void registerClass(Class cls) {
        registerClass(cls.getName());
    }

   /**
    * @deprecated
    */
    protected void registerClass(String className) {
        ((ExplorationSetUp) container).registerClass(className);
    }

    public void setContainer(Object container) {
        this.container = container;
    }
/*
    protected void addRole(Role role, User user) {
        user.getRoles().add(role);
    }

    protected Role addRole(String roleName, User user) {
        Role role = (Role) createInstance(Role.class);
        role.getName().setValue(roleName);
        user.getRoles().add(role);
        return role;    
    }

    protected User addUser(String userName) {
        User user = (User) createInstance(User.class);
        user.getName().setValue(userName);
        
        ExplorationUser es = (ExplorationUser) createInstance(ExplorationUser.class);
        es.setUser(user);
        
        return user;
    }
    
    public void setUser(String name) {
        getContainer().setUser(name);
     }
    */

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