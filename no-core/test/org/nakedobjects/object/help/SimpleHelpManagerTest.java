package org.nakedobjects.object.help;

import org.nakedobjects.object.reflect.MemberIdentifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.StringReader;

import junit.framework.TestCase;


public class SimpleHelpManagerTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimpleHelpManagerTest.class);
    }

    private TestHelpManager manager;

    protected void setUp() throws Exception {
        manager = new TestHelpManager();
    }

    public void testNoLines() {
        MemberIdentifier identifier = new MemberIdentifier("cls", "mth");
        String s = manager.help(identifier);
        assertEquals("", s);
    }
    
    public void testClass() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        
        MemberIdentifier identifier = new MemberIdentifier("cls");
        String s = manager.help(identifier);
        assertEquals("Help about class\n", s);
    }
    
    public void testClassWithNoText() {
        manager.addLine("C:cls");
        manager.addLine("C:cls2");
        manager.addLine("Help about class");
        
        MemberIdentifier identifier = new MemberIdentifier("cls");
        String s = manager.help(identifier);
        assertEquals("", s);
    }


    public void testClassTextStopsAtNextClass() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("C:cls2");
        manager.addLine("Different text");
        
        MemberIdentifier identifier = new MemberIdentifier("cls");
        String s = manager.help(identifier);
        assertEquals("Help about class\n", s);
    }
   
    public void testMethodTextStopsAtNextClass() {
        manager.addLine("C:cls");
        manager.addLine("M:fld");
        manager.addLine("Help about method");
        manager.addLine("C:cls2");
        manager.addLine("Different text");
        
        MemberIdentifier identifier = new MemberIdentifier("cls", "fld");
        String s = manager.help(identifier);
        assertEquals("Help about method\n", s);
    }
   

    public void testMethodTextStopsAtNextMethod() {
        manager.addLine("C:cls");
        manager.addLine("M:fld");
        manager.addLine("Help about method");
        manager.addLine("M:fld2");
        manager.addLine("Different text");
        
        MemberIdentifier identifier = new MemberIdentifier("cls", "fld");
        String s = manager.help(identifier);
        assertEquals("Help about method\n", s);
    }
   

    public void testClassTextStopsAtFirstMethod() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("M:method");
        manager.addLine("Different text");
        
        MemberIdentifier identifier = new MemberIdentifier("cls");
        String s = manager.help(identifier);
        assertEquals("Help about class\n", s);
    }
    

    public void testEntryWithMultipleLines() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("line 2");
        manager.addLine("line 3");
        
        MemberIdentifier identifier = new MemberIdentifier("cls");
        String s = manager.help(identifier);
        assertEquals("Help about class\nline 2\nline 3\n", s);
    }
    

    public void testFieldWithNoEntry() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        
        MemberIdentifier identifier = new MemberIdentifier("cls", "fld2");
        String s = manager.help(identifier);
        assertEquals("", s);
    }


    public void testMessageForFileError() {
        MemberIdentifier identifier = new MemberIdentifier("cls", "fld2");
        SimpleHelpManager manager = new SimpleHelpManager() {
            protected BufferedReader getReader() throws FileNotFoundException {
                throw new FileNotFoundException("not found");
            }
        } ;
        String s = manager.help(identifier);
        assertEquals("Failure opening help file: not found", s);
    }
    
    public void testField() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("M:fld1");
        manager.addLine("Help about field");
        manager.addLine("M:fld2");
        manager.addLine("Help about second field");
        
        MemberIdentifier identifier = new MemberIdentifier("cls", "fld2");
        String s = manager.help(identifier);
        assertEquals("Help about second field\n", s);
    }

}

class TestHelpManager extends SimpleHelpManager {
    private StringBuffer file = new StringBuffer();

    public void addLine(String string) {
        file.append(string);
        file.append('\n');
    }

    protected BufferedReader getReader() throws FileNotFoundException {
        return new BufferedReader(new StringReader(file.toString()));
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