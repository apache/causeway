package org.nakedobjects.utility;

import java.io.PrintStream;
import java.io.PrintWriter;


public class NakedObjectRuntimeException extends RuntimeException {
    boolean IN_JAVA2 = false;

    private final Throwable cause;

    public NakedObjectRuntimeException() {
        super();
        cause = null;
    }

    public NakedObjectRuntimeException(String msg) {
        super(msg);
        cause = null;
    }

    public NakedObjectRuntimeException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    public NakedObjectRuntimeException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    public Throwable getCause() {
        return (cause == this ? null : cause);
    }

    public void printStackTrace(PrintStream s) {
        if (IN_JAVA2) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                s.println(" Exception: " + this);
                super.printStackTrace(s);
                
                // TODO remove common trace elements between two stacks
                Throwable c = getCause();
                if (c != null) {
                    s.println("Caused by: " + this);
                    c.printStackTrace(s);
                }
            }
        }
    }

    public void printStackTrace(PrintWriter s) {
        if (IN_JAVA2) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                s.println(" Exception: " + this);
                super.printStackTrace(s);
                
                // TODO remove common trace elements between two stacks
                Throwable c = getCause();
                if (c != null) {
                    s.println("Caused by: " + this);
                    c.printStackTrace(s);
                }
            }
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */