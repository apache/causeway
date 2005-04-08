package org.nakedobjects.object.persistence;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public abstract class Logger {
    private PrintStream stream;
    private final long start;
    private final String fileName;
    
    public Logger(final String fileName) {
        this.fileName = fileName;
        start = time();
    }

    private long time() {
        return System.currentTimeMillis();
    }

    public void log(String message) {
        if (stream == null) {
            try {
                stream = new PrintStream(new FileOutputStream(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stream.print(time() - start);
        stream.print("  ");
        stream.println(message);
        stream.flush();
    }

    public void close() {
        stream.close();
    }
    
    public void log(String request, Object result) {
        log(request + "  -> " + result);
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