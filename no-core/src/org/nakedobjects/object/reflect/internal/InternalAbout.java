package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.AbstractConsent;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;


public class InternalAbout implements Hint {

    private String description;
    private boolean invisible;
    private String name;
    private StringBuffer unusableReason = new StringBuffer();
    private boolean unusable;

    public Consent canAccess() {
        return AbstractConsent.allow(!invisible);
    }

    public Consent canUse() {
        if (unusable) {
            return new Veto(unusableReason.toString());
        } else {
            return new Allow();
        }
    }

    public String debug() {
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void invisible() {
        invisible = true;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void unusableOnCondition(boolean condition, String reason) {
        if(condition) {
            unusable = true;
	        if (unusableReason.length() > 0) {
	            unusableReason.append("; ");
	        }
	        unusableReason.append(reason);
        }
    }

    public void unusable() {
        unusable = true;
    }

    public Consent isValid() {
        return Allow.DEFAULT;
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