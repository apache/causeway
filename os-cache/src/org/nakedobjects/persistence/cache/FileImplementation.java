package org.nakedobjects.persistence.cache;

import org.nakedobjects.NakedObjects;

import java.io.File;
import java.io.FilenameFilter;

public class FileImplementation {
    private String directoryPath = "tmp/test-cache";
    private String PADDING = "00000000";
    private String journalFilename = "journal";
    private String snapshotFilename = "snapshot";
    private String suffix = ".data";
    private int version = 0;

    public FileImplementation() {
        String dir = NakedObjects.getConfiguration().getString("cache-object-store.directory", "tmp/cache-data");
        setDirectory(dir);
    }

    public void setDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        directoryPath = directory;
    }


    private File file(String filenameBase, int version, boolean temp) {
        File directory = new File(directoryPath);
        String number = PADDING + version;
        String filepath = filenameBase + number.substring(number.length() - PADDING.length()) + (temp ? ".tmp" : suffix);

        return new File(directory, filepath);
    }


    private String latestSnapshot(File directory) {
        String[] snapshots = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(snapshotFilename) && name.endsWith(suffix);
            }
        });

        String max = snapshotFilename + PADDING + suffix;

        for (int i = 0; i < snapshots.length; i++) {
            if (max.compareTo(snapshots[i]) < 0) {
                max = snapshots[i];
            }
        }

        String number = max.substring(snapshotFilename.length(), max.length() - suffix.length());
        version = Integer.valueOf(number).intValue() + 1;

        String filepath = snapshotFilename + number + suffix;
        return filepath;
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