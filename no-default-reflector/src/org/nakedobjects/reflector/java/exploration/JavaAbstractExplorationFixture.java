package org.nakedobjects.reflector.java.exploration;

import org.nakedobjects.application.control.Role;
import org.nakedobjects.application.control.User;
import org.nakedobjects.object.exploration.AbstractExplorationFixture;
import org.nakedobjects.reflector.java.SimpleExplorationSetup;


public abstract class JavaAbstractExplorationFixture extends AbstractExplorationFixture {
    protected void addRole(Role role, User user) {
        user.getRoles().addElement(role);
    }

    protected Role addRole(String roleName, User user) {
        Role role = new Role();
        role.getName().setValue(roleName);
        user.getRoles().addElement(role);
        return role;    
    }

    protected User addUser(String userName) {
        User user = new User();
        user.getName().setValue(userName);
        
        ExplorationUser es = (ExplorationUser) createInstance(ExplorationUser.class);
        es.setUser(user);
        
        return user;
    }
    
    public void setUser(String name) {
        ((SimpleExplorationSetup) getContainer()).setUser(name);
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