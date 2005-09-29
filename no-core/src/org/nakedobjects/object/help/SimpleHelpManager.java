package org.nakedobjects.object.help;

import org.nakedobjects.object.reflect.MemberIdentifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class SimpleHelpManager implements HelpManager {
    private static final Logger LOG = Logger.getLogger(SimpleHelpManager.class);
    private static final String CLASS_PREFIX = "C:";
    private static final String NAME_PREFIX = "M:";
    private String fileName = "help.txt";
    
    
    public String help(MemberIdentifier identifier) {
        BufferedReader reader = null;
        try {
            reader = getReader();
            
            if(reader == null) {
                return "No help available";
            }

            String className = CLASS_PREFIX  + identifier.getClassName();
            String name = NAME_PREFIX + identifier.getName();

            StringBuffer str = new StringBuffer();
            String line;
            
            boolean lookingForClass = true;
            boolean lookingForName = identifier.getName().length() > 0;
            /*
             * Read through each line in file.  
             */
            while((line = reader.readLine()) != null) {
                // Skip comments - lines begining with hash
                if(line.length() > 0 && line.charAt(0) == '#') {
                    continue;
                }
                
                /*
                 * Look for class.
                 */
                if(line.equals(className)) {
                    lookingForClass = false;
                    continue;
                }
            
                if(lookingForClass) {
                    continue;
                } else if(line.startsWith(CLASS_PREFIX)) {
                    break;
                }
                
                /*
                 * Look for field/method.
                 */
                if(line.equals(name)) {
                    lookingForName = false;
                    continue;
                }
                
                if(lookingForName) {
                    continue;
                } else if(line.startsWith(NAME_PREFIX)) {
                    break;
                }
                
                str.append(line);
                str.append('\n');
            }
        
            return str.toString();

            
            
        } catch (FileNotFoundException e) {
            LOG.error("opening help file", e);
            return "Failure opening help file: " + e.getMessage();
        } catch (IOException e) {
            LOG.error("reading help file", e);
            return "Failure reading help file: " + e.getMessage();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }


    protected BufferedReader getReader() throws FileNotFoundException {
        File file = new File(fileName);
        if(! file.exists()) {
            String message = "No help file found: " + file.getAbsolutePath();
            LOG.warn(message);
            return null;
        }

        return new BufferedReader(new FileReader(file));
    }    
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
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