package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectApplicationException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.ExceptionHelper;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.UserActionSet;


public class ExceptionMessageContent implements MessageContent {

    protected String message;
    protected String name;
    protected String trace;
    protected String title;
    private final String icon;

    public ExceptionMessageContent(Throwable error) {
        String name = error.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        this.name = NameConvertor.naturalName(name);
        this.message = error.getMessage();
        this.trace = ExceptionHelper.exceptionTraceAsString(error);

        if (this.name == null) {
            this.name = "";
        }
        if (this.message == null) {
            this.message = "";
        }
        if (this.trace == null) {
            this.trace = "";
        }

        if (error instanceof NakedObjectApplicationException) {
            title = "Application Exception";
            icon = "application-exception";
        } else if (error instanceof ConcurrencyException) {
            title = "Concurrency Exception";
            icon = "concurrency-exception";
        } else {
            title = "System Error";
            icon = "system-error";
        }

    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return trace;
    }

    public String getIconName() {
        return icon;
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
        return name;
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