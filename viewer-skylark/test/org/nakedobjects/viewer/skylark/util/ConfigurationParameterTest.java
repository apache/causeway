package org.nakedobjects.viewer.skylark.util;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import org.nakedobjects.configuration.Configuration;

import junit.framework.TestCase;


public class ConfigurationParameterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConfigurationParameterTest.class);
    }
    private Configuration params;

    public ConfigurationParameterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF);
        params = Configuration.getInstance();
        Properties p = new Properties();
        p.put("nakedobjects.bool", "on");
        p.put("nakedobjects.str", "string");
        p.put("nakedobjects.group.one", "1");
        p.put("nakedobjects.group.two", "2");
        params.load(p);
    }

    public void testBoolean() {
        assertEquals(true, params.getBoolean("bool"));
    }

    public void testHas() {
        assertTrue(params.hasProperty("str"));
        assertFalse(params.hasProperty("none"));
    }

    public void testMissingString() {
        assertEquals(null, params.getString("none"));
    }

    public void testMissingStringWithDefault() {
        assertEquals("default", params.getString("none", "default"));
    }

    public void testPropertySubset() {
        Properties p = params.getPropertySubset("group");
        assertTrue(p.containsKey("one"));
        assertTrue(p.containsKey("two"));
        assertEquals(2, p.size());
    }

    public void testPropertiesWithPrefix() {
        Properties p = params.getProperties("nakedobjects.group");
        assertTrue(p.containsKey("nakedobjects.group.one"));
        assertTrue(p.containsKey("nakedobjects.group.two"));
        assertEquals(2, p.size());
    }

    public void testString() {
        assertEquals("string", params.getString("str"));
    }

    public void testStringWithDefault() {
        assertEquals("string", params.getString("str", "default"));
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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