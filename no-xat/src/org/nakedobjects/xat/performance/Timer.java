package org.nakedobjects.xat.performance;

import java.util.Hashtable;


public class Timer {
    private final static String DELIMITER = "\t";
    private static boolean addDelay = false;
    private static long creationTime;
    private static long memoryBase;
    private static int nextId = 0;
    private static int nextThread = 0;
    private static Hashtable threads = new Hashtable();
    private final String name;
    private long start = 0;
    private long end = 0;
    private final int id;
    private final String thread;
    private final long memory;

    public static void reset() {
        creationTime = time();
        memoryBase = memory();
    }

    public Timer(final String name) {
        this.name = name;
        synchronized (Timer.class) {
            id = nextId++;
        }
        Thread t = Thread.currentThread();
        if (threads.containsKey(t)) {
            thread = (String) threads.get(t);
        } else {
            thread = "t" + nextThread++;
            threads.put(t, thread);
        }
        memory = memory();
    }

    private static long memory() {
        return Runtime.getRuntime().freeMemory();
    }

    public String getName() {
        return name;
    }

    public void start() {
        start = time();
    }

    private static long time() {
        return System.currentTimeMillis();
    }

    public void stop() {
        end = time();
    }

    private long getTimeInMilliseconds() {
        return end - start;
    }

    public long getStart() {
        return start - creationTime;
    }

    public long getEnd() {
        return end - creationTime;
    }

    public long getMemoryUsage() {
        return memoryBase - memory;
    }

    public String logRecord() {
        return id + DELIMITER + thread + DELIMITER + getName() + DELIMITER + getMemoryUsage() + DELIMITER + getStart()
                + DELIMITER + getEnd() + DELIMITER + getTimeInMilliseconds();
    }

    public String toString() {
        return getTimeInMilliseconds() + "ms - " + name;
    }

    public void userDelay(int min, int max) {
        if(addDelay) {
	        int delay = min * 1000 + (int)((max - min) * Math.random() * 1000);
	        try {
	            Thread.sleep(delay);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
        }
    }

    public static void setAddDelay(boolean on) {
        addDelay = on;
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