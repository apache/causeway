package org.nakedobjects.utility.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SnapshotWriter {
    private static final Format FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SnapshotWriter.class);
    private final PrintStream os;
    
    public SnapshotWriter(String directoryPath, String baseFileName, String extension,String message) throws IOException {
        File dir = new File(directoryPath == null || directoryPath.length() == 0 ? "." : directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File indexFile = new File(dir, "index.txt");
        Date date = new Date();
        extension = extension == null || extension.length() == 0 ? "log" : extension;
        File logFile = new File(dir, baseFileName + FORMAT.format(date) + "." + extension);

        RandomAccessFile index = new RandomAccessFile(indexFile, "rw");
        index.seek(index.length());
        index.writeBytes(logFile.getName() + ": " + message + "\n");
        index.close();
     
        os = new PrintStream(new FileOutputStream(logFile));
    }
    
    public void appendLog(String details) {
        os.println(details);
    }

    public void close() {
        if (os != null) {
            os.close();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */