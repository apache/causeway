package org.nakedobjects.persistence.file.utility;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class CopyXmlObjectStore {
    public static void main(String[] args) {
        String workingDirectory = args[0];
        String testDirectory = args[1];

        copyAllFiles(testDirectory, workingDirectory);
    }

    private static void copyAllFiles(String testDirectory, String workingDirectory) {
        File from = new File(testDirectory);
        File to = new File(workingDirectory);

        if (!to.exists()) {
            to.mkdirs();
        }
        if (to.isFile()) { throw new NakedObjectRuntimeException("To directory is actually a file " + to.getAbsolutePath()); }

        String list[] = from.list();
        for (int i = 0; i < list.length; i++) {
            copyFile(new File(from, list[i]), new File(to, list[i]));
        }
    }

    private static void copyFile(File from, File to) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            bos = new BufferedOutputStream(new FileOutputStream(to));

            byte buffer[] = new byte[2048];

            int len = 0;
            while ((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new NakedObjectRuntimeException("Error copying file " + from.getAbsolutePath() + " to " + to.getAbsolutePath(),
                    e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ignore) {
                    ;
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ignore) {
                    ;
                }
            }
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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