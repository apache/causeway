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
package org.apache.causeway.commons.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.ThrowingFunction;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities related to the <i>Java</i> {@link File} type.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class FileUtils {

    /**
     * Opens an {@link InputStream} for give {@link File}
     * and passes it to given {@link Function} for application,
     * then finally closes it.
     * @return either a successful or failed {@link Try} (non-null),
     *      where in the success case, the Try is holding the returned value from the given {@link Function inputStreamMapper};
     *      if the file is null or not readable, the failure may hold a {@link NoSuchFileException} or other i/o related exceptions
     * @see DataSource#ofFile(File)
     */
    public <T> Try<T> tryReadAndApply(final @Nullable File file, final @NonNull ThrowingFunction<InputStream, T> inputStreamMapper) {
        return Try.call(()->{
            try(var inputStream = new FileInputStream(existingFileElseFail(file))){
                return inputStreamMapper.apply(inputStream);
            }
        });
    }

    /**
     * Opens an {@link InputStream} for give {@link File}
     * and passes it to given {@link Consumer} for consumption,
     * then finally closes it.
     * @return either a successful or failed {@link Try} (non-null);
     *     if the file is null or not readable, the failure may hold a {@link NoSuchFileException} or other i/o related exceptions
     * @see DataSource#ofFile(File)
     */
    public Try<Void> tryReadAndAccept(final @Nullable File file, final @NonNull ThrowingConsumer<InputStream> inputStreamConsumer) {
        return Try.run(()->{
            try(var inputStream = new FileInputStream(existingFileElseFail(file))){
                inputStreamConsumer.accept(inputStream);
            }
        });
    }

    /**
     * Recursive file search, starting at {@code dir}, going deeper based on predicate
     * {@code dirFilter}, collecting files (not directories) based on predicate
     * {@code fileFilter}.
     * @param dir
     * @param dirFilter
     * @param fileFilter
     * @return set of matching files
     */
    public Set<File> searchFiles(
            final File dir,
            final Predicate<File> dirFilter,
            final Predicate<File> fileFilter) {
        final Set<File> fileList = new LinkedHashSet<>();
        searchFiles(dir, dirFilter, fileFilter, fileList::add);
        return fileList;
    }

    /**
     * Recursive file search, starting at {@code dir}, going deeper based on predicate
     * {@code dirFilter}, consuming files (not directories) based on predicate
     * {@code fileFilter}.
     * @param dir
     * @param dirFilter
     * @param fileFilter
     * @param onFileFound
     */
    @SneakyThrows
    public void searchFiles(
            final File dir,
            final Predicate<File> dirFilter,
            final Predicate<File> fileFilter,
            final Consumer<File> onFileFound) {
        if(!dir.exists()) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath())) {
            for (Path path : stream) {
                var file = path.toFile();
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

    /**
     *
     * @param file
     * @return optionally {@code file.getCanonicalPath()} based on whether {@code file}
     * is not {@code null} and the 'file I/O system' can handle this call without
     * throwing an exception.
     */
    public Optional<String> canonicalPath(@Nullable final File file) {
        if(file==null) {
            return Optional.empty();
        }
        try {
            return Optional.of(file.getCanonicalPath());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     *
     * @param commonPath
     * @param absolutePath
     * @return prefix removed from {@code absolutePath}, if {@code commonPath} appears to be a prefix
     * of {@code absolutePath}, otherwise returns the {@code absolutePath} unmodified.
     */
    public String toRelativePath(@NonNull final String commonPath, @NonNull final String absolutePath) {
        if(absolutePath.startsWith(commonPath)) {
            return absolutePath.substring(commonPath.length());
        }
        return absolutePath;
    }

    /**
     * Deletes given {@link File}. This operation is ignored if the file is a directory or does not exist.
     * @param file - the file to be deleted (null-able)
     */
    @SneakyThrows
    public void deleteFile(@Nullable final File file) {
        if(file==null
                || !file.exists()
                || file.isDirectory()) {
            return; // silently ignore if not an existing file
        }
        Files.delete(file.toPath());
    }

    public static boolean deleteDirectory(final File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * Returns a temp directory with delete-on-exit policy.
     */
    @SneakyThrows
    public File tempDir(final String name) {
        var tempDir =  Files.createTempDirectory(name).toFile();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static String realtiveFileName(final File root, final File file) {
        return file.getAbsolutePath().substring(root.getAbsolutePath().length()+1);
    }

    public static Function<File, String> realtiveFileName(final File root) {
        return file->realtiveFileName(root, file);
    }

    /**
     * Creates the given directory if it does not already exist.
     * If directory is null acts as a no-op.
     * @throws IllegalArgumentException if any pre-existing file is in conflict
     */
    public File makeDir(final @Nullable File directory) {
        if(directory==null) {
            return directory; // no-op
        }
        if(directory.exists()) {
            if(directory.isDirectory()) {
                return directory; // nothing to do
            }
            throw _Exceptions.illegalArgument(
                    "cannot create directory over pre-existing file of same name %s",
                    directory.getAbsolutePath());
        }
        if(!directory.mkdirs()) {
            throw _Exceptions.unrecoverable(
                    "failed to create directory %s",
                    directory.getAbsolutePath());
        }
        return directory;
    }

    /**
     * Optionally given file, based on whether non-null and exists and is a file (not a directory).
     */
    public Optional<File> existingFile(final @Nullable File file) {
        return file!=null
                && file.isFile()
                ? Optional.of(file)
                : Optional.empty();
    }

    /**
     * Guard given file against null, non-existence and not representing a file.
     */
    @SneakyThrows
    public File existingFileElseFail(final @Nullable File file) {
        if(file==null) {
            throw new NoSuchFileException("<null>");
        }
        if(!file.exists()) {
            throw new NoSuchFileException(file.getAbsolutePath());
        }
        if(!file.isFile()) {
            throw new NoSuchFileException(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Optionally given file, based on whether non-null and exists and is a directory (not a file).
     */
    public Optional<File> existingDirectory(final @Nullable File file) {
        return file!=null
                && file.isDirectory()
                ? Optional.of(file)
                : Optional.empty();
    }

    /**
     * Guard given file against null, non-existence and not representing a directory (not a file).
     */
    @SneakyThrows
    public File existingDirectoryElseFail(final @Nullable File file) {
        if(file==null) {
            throw new NoSuchFileException("<null>");
        }
        if(!file.exists()) {
            throw new NoSuchFileException(file.getAbsolutePath());
        }
        if(!file.isDirectory()) {
            throw new NotDirectoryException(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Copy {@code from} file {@code to} file, replacing existing.
     * @param from
     * @param to
     */
    @SneakyThrows
    public void copy(final @NonNull File from, final @NonNull File to) {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copy all lines {@code from} file {@code to} file, using given {@link Charset}
     * and processing each line before writing using {@code lineProcessor}.
     *
     * @param from - the file to read lines from
     * @param to - the file to write the processed lines to
     * @param openOptions - If no options are present then this method works as if the {@link
     * StandardOpenOption#CREATE CREATE}, {@link
     * StandardOpenOption#TRUNCATE_EXISTING TRUNCATE_EXISTING}, and {@link
     * StandardOpenOption#WRITE WRITE} options are present. In other words, it
     * opens the file for writing, creating the file if it doesn't exist, or
     * initially truncating an existing regular-file to
     * a size of {@code 0} if it exists.
     */
    @SneakyThrows
    public void copyLines(
            final @NonNull File from,
            final @NonNull File to,
            final @NonNull Charset charset,
            final @NonNull UnaryOperator<String> lineProcessor,
            final @NonNull OpenOption... openOptions) {

        try (final BufferedReader reader = Files.newBufferedReader(from.toPath(), charset);
             final BufferedWriter writer = Files.newBufferedWriter(to.toPath(), charset, openOptions)) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(lineProcessor.apply(line));
                writer.write("\n");
            }
        }
    }

}
