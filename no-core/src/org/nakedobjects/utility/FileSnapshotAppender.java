package org.nakedobjects.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FileSnapshotAppender extends SnapshotAppender {
    private static final Format FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FileSnapshotAppender.class);
    private String directoryPath;
    private String extension;
    private String fileName = "log-snapshot-";

    public String getDirectory() {
        return directoryPath;
    }

    public String getExtension() {
        return extension;
    }
    public String getFileName() {
        return fileName;
    }

    public void setDirectory(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected synchronized void writeSnapshot(String message, String details) {
        File dir = new File(directoryPath == null || directoryPath.length() == 0 ? "." : directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File indexFile = new File(dir, "index.txt");
        Date date = new Date();
        extension = extension == null || extension.length() == 0 ? "log" : extension;
        File logFile = new File(dir, fileName + FORMAT.format(date) + "." + extension);

        try {
            RandomAccessFile index = new RandomAccessFile(indexFile, "rw");
            index.seek(index.length());
            index.writeBytes(logFile.getName() + ": " + message + "\n");
            index.close();
        } catch (FileNotFoundException e) {
            LOG.info("failed to open index file", e);
        } catch (IOException e) {
            LOG.info("failed to write to index file", e);
        }

        PrintStream os = null;
        try {
            os = new PrintStream(new FileOutputStream(logFile));
            os.println(details);
        } catch (IOException e) {
            LOG.info("failed to write log file", e);
        } finally {
            if (os != null) {
                os.close();
            }
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