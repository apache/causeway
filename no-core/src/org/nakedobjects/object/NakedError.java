package org.nakedobjects.object;

import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.value.MultilineTextString;
import org.nakedobjects.object.value.TextString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;


public class NakedError extends AbstractNakedObject {
    private static final Logger LOG = Logger.getLogger(NakedError.class);
    private TextString error;
    private TextString exception;
    private MultilineTextString trace;

    public NakedError(String msg) {
        error = new TextString(msg);
        LOG.error(error);
    }

    public NakedError(String msg, Throwable e) {
        error = new TextString(msg + " " + e.getMessage());
        exception = new TextString(e.getMessage());

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            trace = new MultilineTextString(baos.toString());
            baos.close();
        } catch (IOException ex) {
            LOG.error(ex);
        }

        LOG.error(error, e);
    }
    
    public void aboutFieldDefault(FieldAbout about) {
        about.unmodifiable();
    }
    
    public TextString getError() {
        return error;
    }

    public TextString getException() {
        return exception;
    }

    public MultilineTextString getTrace() {
        return trace;
    }

    public String iconName() {
		return "error";
	}

    public void makePersistent() {
        throw new NotPersistableException("Can't make an error object persistent.");
    }

    public Title title() {
        return error.title();
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
