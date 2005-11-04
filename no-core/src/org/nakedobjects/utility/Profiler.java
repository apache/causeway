package org.nakedobjects.utility;

import java.text.NumberFormat;
import java.util.Hashtable;


public class Profiler {
    private final static String DELIMITER = "\t";
    private static NumberFormat floatFormat = NumberFormat.getNumberInstance();
    private static NumberFormat integerFormat = NumberFormat.getNumberInstance();
    private static int nextId = 0;
    private static int nextThread = 0;
    protected static ProfilerSystem profilerSystem = new ProfilerSystem();
    private static Hashtable threads = new Hashtable();

    public static String memoryLog() {
        long free = memory();
        return integerFormat.format(free) + " bytes";
    }

    private static long memory() {
        return profilerSystem.memory();
    }

    private static long time() {
        return profilerSystem.time();
    }

    public static void setProfilerSystem(ProfilerSystem profilerSystem) {
        Profiler.profilerSystem = profilerSystem;
    }
    
    private long elapsedTime = 0;
    private final int id;
    private long memory;
    private final String name;
    private long start = 0;
    private final String thread;
    private boolean timing = false;

    public Profiler(final String name) {
        this.name = name;
        synchronized (Profiler.class) {
            id = nextId++;
        }
        Thread t = Thread.currentThread();
		String thread = (String) threads.get(t);
        if (thread != null) {
            this.thread = thread;
        } else {
            this.thread = "t" + nextThread++;
            threads.put(t, this.thread);
        }
        memory = memory();
    }

    public long getElapsedTime() {
        return timing ? time() - start : elapsedTime;
    }

    public long getMemoryUsage() {
        return memory() - memory;
    }

    public String getName() {
        return name;
    }

    public String log() {
        return id + DELIMITER + thread + DELIMITER + getName() + DELIMITER + getMemoryUsage()
                + DELIMITER + getElapsedTime();
    }

    public void reset() {
        elapsedTime = 0;
        start = time();
        memory = memory();
    }

    public void start() {
        start = time();
        timing = true;
    }

    public void stop() {
        timing = false;
        long end = time();
        elapsedTime += end - start;
    }


    public String memoryUsageLog() {
        return integerFormat.format(getMemoryUsage()) + " bytes";
    }
    
    public String timeLog() {
        return floatFormat.format(getElapsedTime() / 1000.0) + " secs";
    }

    public String toString() {
        return getElapsedTime() + "ms - " + name;
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