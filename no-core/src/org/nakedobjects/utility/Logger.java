package org.nakedobjects.utility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class Logger {
    private String fileName;
    private DateFormat format;
    private boolean logAlso;
    private org.apache.log4j.Logger logger;
    private boolean showTime;
    private final long start;
    private PrintStream stream;

    public Logger() {
        start = time();
        logAlso = true;
        showTime = true;
        format = new SimpleDateFormat("yyyyMMdd-hhmm-ss.SSS");
    }

    public Logger(final String fileName, boolean logAlso) {
        this();
        this.fileName = fileName;
        this.logAlso = logAlso;
    }

    public void close() {
        if (stream != null) {
            stream.close();
        }
    }

    protected abstract Class getDecoratedClass();

    public boolean isLogToFile() {
        return fileName != null;
    }

    public boolean isLogToLog4j() {
        return logAlso;
    }

    public void log(String message) {
        if (logAlso) {
            logger().info(message);
        }
        if (fileName != null) {
            if (stream == null) {
                try {
                    if (fileName == null) {
                        return;
                    }
                    stream = new PrintStream(new FileOutputStream(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (showTime) {
                stream.print(format.format(new Date(time())));
            } else {
                stream.print(time() - start);
            }
            stream.print("  ");
            stream.println(message);
            stream.flush();
        }
    }

    public void log(String request, Object result) {
        log(request + "  -> " + result);
    }

    private org.apache.log4j.Logger logger() {
        if (logger == null) {
            logger = org.apache.log4j.Logger.getLogger(getDecoratedClass());
        }
        return logger;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLogAlso(boolean logAlso) {
        this.logAlso = logAlso;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public void setTimeFormat(String format) {
        this.format = new SimpleDateFormat(format);
    }

    private long time() {
        return System.currentTimeMillis();
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