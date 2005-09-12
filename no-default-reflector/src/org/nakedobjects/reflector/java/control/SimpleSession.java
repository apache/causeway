package org.nakedobjects.reflector.java.control;

import org.nakedobjects.application.control.Role;
import org.nakedobjects.application.control.User;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;

import org.apache.log4j.Logger;

public class SimpleSession extends Session {
    private static final Logger LOG = Logger.getLogger(Session.class);
    
    private Role roles[];

    private User user;

  public String getDebugData() {
       StringBuffer sb = new StringBuffer();

       sb.append("User\n");
       sb.append("  Name:     ");
       if (user == null) {
           sb.append("none");

       } else {
           sb.append(user.getName().stringValue());
           sb.append("\n");

           sb.append("  Roles:     ");
           if (user.getRoles() == null) {
               sb.append("     none");
           } else {
               Enumeration fields = user.getRoles().elements();
               while (fields.hasMoreElements()) {
                   Role role = (Role) fields.nextElement();
                   sb.append("           ");
                   sb.append(role);
                   sb.append("\n");
               }
           }
           sb.append("\n");

           sb.append("Root object\n");
           sb.append("  Root object: ");
           Object rootObject = user.getRootObject();
           sb.append(rootObject);
           if (rootObject instanceof DebugInfo) {
               sb.append("\n");
               sb.append(((DebugInfo) rootObject).getDebugData());
           }
       }
       sb.append("\n\n");

       return sb.toString();
   }
  
    public User getName() {
        return user;
    }

    
    public boolean hasRole(Role role) {
        if (user == null) {
            return true;
//            throw new IllegalStateException("no user set up");
        }
        String roleName = role.getName().stringValue();
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].getName().isSameAs(roleName)) {
                LOG.debug("role " + roleName + " matches for " + this);
                return true;
            }
        }
        LOG.debug("role " + roleName + " not matched for " + this);
        return false;
    }

    public boolean isCurrentUser(User user) {
        if (user == null) {
            throw new IllegalStateException("no user set up");
        }
        return this.user == user;
    }

    public void setUser(User user) {
        this.user = user;

        if (user == null) {
            roles = null;
        } else {
            roles = new Role[user.getRoles().size()];
            Enumeration e = user.getRoles().elements();
            int i = 0;
            while (e.hasMoreElements()) {
                roles[i++] = (Role) e.nextElement();
            }
        }
    }

    public String toString() {
        String rls = "";
        for (int i = 0; roles != null && i < roles.length; i++) {
            rls += roles[i].getName().stringValue() + " ";
        }
        return "Session [user=" + (user == null ? "none" : user.getName().stringValue()) + ",roles=" + rls + "]";
    }

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