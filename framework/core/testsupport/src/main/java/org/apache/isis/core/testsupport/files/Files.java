package org.apache.isis.core.testsupport.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public final class Files {
    
    Files(){}

    
    ///////////////////////////////////////////////////////
    // delete files
    ///////////////////////////////////////////////////////
    
    
    public enum Recursion {
        DO_RECURSE,
        DONT_RECURSE
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
    interface Deleter {
        void deleteFile(File f);
    }
    
    static void deleteFiles(final File directory, final FilenameFilter filter, Recursion recursion, Deleter deleter) {
        for (final File file : directory.listFiles(filter)) {
            deleter.deleteFile(file);
        }
        
        if(recursion == Recursion.DO_RECURSE) {
            for (final File subdir: directory.listFiles(filterDirectory())) {
                deleteFiles(subdir, filter, recursion, deleter);
            }
        }
    }

    
    ///////////////////////////////////////////////////////
    // filters
    ///////////////////////////////////////////////////////

    public static FilenameFilter and(final FilenameFilter... filters) {
        return new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                for(FilenameFilter filter: filters) {
                    if(!filter.accept(dir, name)) {
                        return false;
                    }
                }
                return true;
            }};
    }

    public static FilenameFilter filterFileNameExtension(final String fileExtension) {
        return new FilenameFilter() {

            @Override
            public boolean accept(final File arg0, final String arg1) {
                return arg1.endsWith(fileExtension);
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
