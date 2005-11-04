package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.reflect.internal.about.InternalAbout;
import org.nakedobjects.utility.ExceptionHelper;

import org.apache.log4j.Logger;


public class Error implements NakedError {
    private static final Logger LOG = Logger.getLogger(Error.class);
    private String error;
    private String exception;
    private String trace;

    public Error(String msg) {
        error = msg;
        LOG.error(error);
    }

    public Error(String msg, Throwable e) {
        error = msg;        
        exception = e.getMessage();
        trace = ExceptionHelper.exceptionTraceAsString(e);
    }

    public void aboutError(InternalAbout about, String entry) {
        about.unusable();
    }

    public void aboutException(InternalAbout about, String entry) {
        about.unusable();
    }

    public void aboutTrace(InternalAbout about, String entry) {
        about.unusable();
    }

    public String getError() {
        return error;
    }

    public String getException() {
        return exception;
    }

    public String getTrace() {
        return trace;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String titleString() {
        return error;
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
