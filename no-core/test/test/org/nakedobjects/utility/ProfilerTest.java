package test.org.nakedobjects.utility;

import org.nakedobjects.utility.Profiler;

import junit.framework.TestCase;


public class ProfilerTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProfilerTest.class);
    }

    private Profiler profiler;

    public void setUp() {
        Profiler.setProfilerSystem(new ProfilerTestSystem());
        profiler = new Profiler("name");
    }

    public void testFreeMemory() {
        assertEquals("20,300 bytes", Profiler.memoryLog());
    }
    
    public void testMemoryUsage() {
        assertEquals(10300, profiler.getMemoryUsage());
        assertEquals(20000, profiler.getMemoryUsage());
    }
    
    public void testMemoryUsageLog() {
        assertEquals("10,300 bytes", profiler.memoryUsageLog());
    }
    
    public void testTiming() {
        profiler.start();
        profiler.stop();
        assertEquals(100, profiler.getElapsedTime());
    }

    public void testTimingLog() {
        profiler.start();
        profiler.stop();
        assertEquals("0.1 secs", profiler.timeLog());
    }

    public void testContinueWithStartAndStopPausesTiming() {
        profiler.start();
        profiler.stop();

        profiler.start();
        profiler.stop();
        assertEquals(400, profiler.getElapsedTime());
    }


    public void testResetDuringTiming() {
        profiler.start();

        profiler.reset();
        assertEquals(200, profiler.getElapsedTime());
    }

    
    public void testResetAfterStopResetsToZero() {
        profiler.start();
        profiler.stop();

        profiler.reset();
        assertEquals(0, profiler.getElapsedTime());

        profiler.start();
        profiler.stop();
        assertEquals(400, profiler.getElapsedTime());
}

    public void testZero() {
        assertEquals(0, profiler.getElapsedTime());
    }

    public void testRepeatedElapseTimeAfterStopGivesSameTime() {
        profiler.start();
        profiler.stop();
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(100, profiler.getElapsedTime());
    }

    public void testRepeatedElapseTimeGivesLatestTime() {
        profiler.start();
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(300, profiler.getElapsedTime());
        assertEquals(600, profiler.getElapsedTime());
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