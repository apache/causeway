package test.org.nakedobjects.utility.configuration;

import org.nakedobjects.utility.NakedObjectConfiguration;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class ConfigurationTest extends TestCase {
    private PropertiesConfiguration configuration;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConfigurationTest.class);
    }


    public ConfigurationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.OFF);

        configuration = new PropertiesConfiguration();
        Properties p = new Properties();
        p.put("nakedobjects.bool", "on");
        p.put("nakedobjects.str", "string");
        p.put("nakedobjects.group.one", "1");
        p.put("nakedobjects.group.two", "2");
        configuration.add(p);
    }

    public void testBoolean() {
        assertEquals(true, configuration.getBoolean("nakedobjects.bool"));
    }

    public void testHas() {
        assertTrue(configuration.hasProperty("nakedobjects.str"));
        assertFalse(configuration.hasProperty("nakedobjects.none"));
    }

    public void testMissingString() {
        assertEquals(null, configuration.getString("nakedobjects.none"));
    }

    public void testMissingStringWithDefault() {
        assertEquals("default", configuration.getString("nakedobjects.none", "default"));
    }

    public void testSubsetWithPrefix() {
        NakedObjectConfiguration p = configuration.createSubset("nakedobjects.group");
        assertTrue(p.hasProperty("one"));
        assertTrue(p.hasProperty("two"));
        assertEquals(2, p.size());
    }

    public void testSubsetWithTrailingDot() {
        NakedObjectConfiguration p = configuration.createSubset("nakedobjects.group.");
        assertTrue(p.hasProperty("one"));
        assertTrue(p.hasProperty("two"));
        assertEquals(2, p.size());
    }

    public void testString() {
        assertEquals("string", configuration.getString("nakedobjects.str"));
    }

    public void testStringWithDefault() {
        assertEquals("string", configuration.getString("nakedobjects.str", "default"));
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