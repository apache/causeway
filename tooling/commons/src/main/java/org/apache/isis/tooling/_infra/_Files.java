package org.apache.isis.tooling._infra;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lombok.val;

public class _Files {

    public static Set<File> searchFiles(
            final File dir, 
            final Predicate<File> dirFilter, 
            final Predicate<File> fileFilter) throws IOException {
        
        final Set<File> fileList = new HashSet<>();
        searchFiles(dir, dirFilter, fileFilter, fileList::add);
        return fileList;
    }
    
    /** recursive file search */
    public static void searchFiles(
            final File dir, 
            final Predicate<File> dirFilter, 
            final Predicate<File> fileFilter,
            final Consumer<File> onFileFound) throws IOException {
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath())) {
            for (Path path : stream) {
                val file = path.toFile();
                if (Files.isDirectory(path)) {
                    if(dirFilter.test(file)) {
                        // go deeper
                        searchFiles(file, dirFilter, fileFilter, onFileFound);
                    }
                } else {
                    if(fileFilter.test(file)) {
                        onFileFound.accept(file);
                    }
                }
            }
        }
    }
    
}
