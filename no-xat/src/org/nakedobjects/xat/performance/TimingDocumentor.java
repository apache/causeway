package org.nakedobjects.xat.performance;

import org.nakedobjects.xat.AbstractDocumentor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;


public class TimingDocumentor extends AbstractDocumentor {
    private final String directory;
    private boolean isList = false;
    private final Vector timings = new Vector(250, 250);
  
    public TimingDocumentor(final String directory) {
        this.directory = directory;
        Timer.reset();
    }

    public void close() {
        save();
    }

    public void doc(String text) {}

    public void docln(String text) {}

    public void flush() {}

    public void record(Timer timer) {
       // System.out.println(timer);
        timings.addElement(timer);
    }

    private void save() {
        File dir = new File(directory);
        System.out.println(dir.getAbsolutePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(directory , "timing"+ "-xat.data"))));
            
            for (int i = 0, max = timings.size(); i < max; i++) {
                Timer timer = (Timer) timings.elementAt(i);
                writer.println(timer.logRecord());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public void step(String string) {
        
    }

    public void subtitle(String text) {}

    public void title(String text) {
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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