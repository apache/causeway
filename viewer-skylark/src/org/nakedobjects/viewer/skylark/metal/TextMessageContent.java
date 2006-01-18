package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.UserActionSet;


public class TextMessageContent implements MessageContent {
    protected final String message;
    protected final String heading;
    protected final String detail;
    protected final String title;

    public TextMessageContent(String title, String message) {
        int pos = message.indexOf(':');
        if(pos > 2) {
            this.heading = message.substring(0, pos).trim();
            this.message = message.substring(pos + 1).trim();
        } else {
            this.heading = "";
            this.message = message;
        }
        this.title = title;
        this.detail = null;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public String getIcon() {
        return "message";
    }
    
    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public void contentMenuOptions(UserActionSet options) {}

    public void debugDetails(DebugString debug) {}

    public Naked drop(Content sourceContent) {
        return null;
    }

    public String getDescription() {
        return "";
    }

    public String getIconName() {
        return "";
    }

    public Image getIconPicture(int iconHeight) {
        return null;
    }

    public String getId() {
        return "message-exception";
    }

    public Naked getNaked() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isDerived() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isPersistable() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public void parseTextEntry(String entryText) throws InvalidEntryException {}

    public String title() {
        return heading;
    }

    public void viewMenuOptions(UserActionSet options) {}

    public String windowTitle() {
        return title;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */