package org.nakedobjects.utility;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;


public class FileSnapshotAppender extends SnapshotAppender {
    private static final Logger LOG = Logger.getLogger(FileSnapshotAppender.class);
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
        SnapshotWriter s;
        try {
            s = new SnapshotWriter(directoryPath, fileName, extension, message);
            s.appendLog(details);
            s.close();
        } catch (FileNotFoundException e) {
            LOG.info("failed to open log file", e);
        } catch (IOException e) {
            LOG.info("failed to write log file", e);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */