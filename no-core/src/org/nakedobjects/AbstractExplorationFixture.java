package org.nakedobjects;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.security.Role;
import org.nakedobjects.security.User;

import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class AbstractExplorationFixture implements ExplorationFixture {
    private static final Logger LOG = Logger.getLogger(Exploration.class);
    private ExplorationSetUp container;
    private Vector newInstances = new Vector();

    protected final NakedObject createInstance(Class type) {
        return container.createInstance(type);
    }

    protected final NakedObject createInstance(String className) {
        return container.createInstance(className);
    }

    protected ExplorationSetUp getContainer() {
        return container;
    }

    /**
     * @deprecated use needsInstances
     */
    protected final boolean hasNoInstances(Class cls) {
        return container.needsInstances(cls);
    }

    /**
     * @deprecated use needsInstances
     */
    protected final boolean hasNoInstances(String className) {
        return container.needsInstances(className);
    }

    protected final boolean needsInstances(Class cls) {
        return container.needsInstances(cls);
    }

    protected final boolean needsInstances(String className) {
        return container.needsInstances(className);
    }

    protected void registerClass(Class cls) {
        registerClass(cls.getName());
    }

    protected void registerClass(String className) {
        container.registerClass(className);
    }

    public void setContainer(ExplorationSetUp container) {
        this.container = container;
    }

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