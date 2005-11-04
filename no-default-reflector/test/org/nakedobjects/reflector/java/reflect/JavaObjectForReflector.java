package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.control.Validity;



public class JavaObjectForReflector implements Interface1, Interface2 {
    static ClassAbout about;
    
    public static void aboutDummyReflectorTestObject(ClassAbout about) {
        JavaObjectForReflector.about = about;
    }
    
    public static String pluralName() {
        return "Plural";
    }

    public static String singularName() {
        return "Singular";
    }
    
    public String getOne() {
        return "";
    }
    
    public static String getFour() {
        return "";
    }
    
    public static void setFour(String value) {
    }
    
    public void setOne(String value) {
    }


    private JavaReferencedObject object;

    public JavaReferencedObject getTwo() {
        return object;
    }

    public void setTwo(JavaReferencedObject object) {
        this.object = object;
    }

    public JavaReferencedObject getThree() {
        return null;
    }

    public void setThree(JavaReferencedObject object) {
    }

    
    public void setValue(String value) {}
    
    public void aboutValue(FieldAbout about, String value) {}
    
    public void validValue(Validity validity) {}

    public void actionStop() {}

    public void actionStart() {}

    public void aboutStart(ActionAbout about) {    }


    public static void actionTop() {
    }
    public static void actionBottom() {
    }

    public static String actionOrder() {
        return "start, stop";
    }

    public static String classActionOrder() {
        return "top, bottom";
    }

    
    public static String fieldOrder() {
        return "one, two ,three";
    }
}


interface Interface2 {}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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