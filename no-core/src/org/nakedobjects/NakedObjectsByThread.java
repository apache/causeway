package org.nakedobjects;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public class NakedObjectsByThread extends NakedObjectsServer {
    private static final Logger LOG = Logger.getLogger(NakedObjectsByThread.class);

    public static NakedObjects createInstance() {
        if (getInstance() == null) {
            return new NakedObjectsByThread();
        } else {
            return getInstance();
        }
    }

    private Hashtable threads = new Hashtable();

    private NakedObjectsByThread() {}

    protected NakedObjectsData getLocal() {
        Thread thread = Thread.currentThread();
        NakedObjectsData local;
        synchronized (threads) {
            local = (NakedObjectsData) threads.get(thread);

            if (local == null) {
                local = new NakedObjectsData();
                synchronized (threads) {
                    threads.put(thread, local);
                }
                LOG.info("  creating local " + local + "; now have " + threads.size() + " locals");
            }
        }
        return local;
    }

    public String getDebugTitle() {
        return "Naked Objects Repository " + Thread.currentThread().getName();
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