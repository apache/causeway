package org.nakedobjects.xat;


public abstract class AbstractDocumentor implements Documentor {
    private boolean isGenerating = true;
    
    public boolean isGenerating() {
        return isGenerating;
    }

    public void start() {
        isGenerating = true;
    }

    public void stop() {
        isGenerating = false;
    }

    protected String makeTitle(String name) {
        int pos = 0;

        // find first upper case character
        while ((pos < name.length()) && Character.isLowerCase(name.charAt(pos))) {
            pos++;
        }

        if (pos == name.length()) {
            return "invalid name";
        } else {
            StringBuffer s = new StringBuffer(name.length() - pos); // remove is/get/set
            for (int j = pos; j < name.length(); j++) { // process english name - add spaces
                if ((j > pos) && Character.isUpperCase(name.charAt(j))) {
                    s.append(' ');
                }
                s.append(name.charAt(j));
            }
            return s.toString();
        }
    }
}


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