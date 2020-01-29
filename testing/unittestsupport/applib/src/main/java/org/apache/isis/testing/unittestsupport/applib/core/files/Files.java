/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testing.unittestsupport.applib.core.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * <p>
 *     Used by domain apps only.
 * </p>
 */
public final class Files {

    Files() {
    }

    // /////////////////////////////////////////////////////
    // delete files
    // /////////////////////////////////////////////////////

    public enum Recursion {
        DO_RECURSE, DONT_RECURSE
    }

    /**
     *
     * @param directoryName
     *            directory to start deleting from
     * @param filePrefix
     *            file name prefix (no wild cards)
     * @param recursion
     */
    public static void deleteFilesWithPrefix(final String directoryName, final String filePrefix, Recursion recursion) {
        deleteFiles(directoryName, filterFileNamePrefix(filePrefix), recursion);
    }

    public static void deleteFiles(final String directoryName, final String fileExtension, Recursion recursion) {
        deleteFiles(directoryName, filterFileNameExtension(fileExtension), recursion);
    }

    public static void deleteFiles(final File directory, final String fileExtension, Recursion recursion) {
        deleteFiles(directory, filterFileNameExtension(fileExtension), recursion);
    }

    public static void deleteFiles(final String directoryName, final FilenameFilter filter, Recursion recursion) {
        final File dir = new File(directoryName);
        deleteFiles(dir, filter, recursion);
    }

    public static void deleteFiles(final File directory, final FilenameFilter filter, Recursion recursion) {
        deleteFiles(directory, filter, recursion, new Deleter() {
            @Override
            public void deleteFile(File f) {
                f.delete();
            }
        });
    }

    // introduced for testing of this utility class.
    public interface Deleter {
        void deleteFile(File f);
    }

    static void deleteFiles(final File directory, final FilenameFilter filter, Recursion recursion, Deleter deleter) {
        try {
            for (final File file : directory.listFiles(filter)) {
                deleter.deleteFile(file);
            }
        } catch (NullPointerException e) {
        }

        if (recursion == Recursion.DO_RECURSE) {
            try {
                for (final File subdir : directory.listFiles(filterDirectory())) {
                    deleteFiles(subdir, filter, recursion, deleter);
                }
            } catch (NullPointerException e) {
            }
        }
    }

    // /////////////////////////////////////////////////////
    // filters
    // /////////////////////////////////////////////////////

    public static FilenameFilter and(final FilenameFilter... filters) {
        return new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                for (FilenameFilter filter : filters) {
                    if (!filter.accept(dir, name)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static FilenameFilter filterFileNameExtension(final String fileExtension) {
        return new FilenameFilter() {

            @Override
            public boolean accept(final File arg0, final String arg1) {
                return arg1.endsWith(fileExtension);
            }
        };
    }

    public static FilenameFilter filterFileNamePrefix(final String filePrefix) {
        return new FilenameFilter() {

            @Override
            public boolean accept(final File arg0, final String arg1) {
                return arg1.startsWith(filePrefix);
            }
        };
    }

    public static FileFilter filterDirectory() {
        return new FileFilter() {
            @Override
            public boolean accept(File arg0) {
                return arg0.isDirectory();
            }
        };
    }

}
