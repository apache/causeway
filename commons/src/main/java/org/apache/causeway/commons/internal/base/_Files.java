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
package org.apache.causeway.commons.internal.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class _Files {

    /**
     * Recursive file search, starting at {@code dir}, going deeper based on predicate
     * {@code dirFilter}, collecting files (not directories) based on predicate
     * {@code fileFilter}.
     * @param dir
     * @param dirFilter
     * @param fileFilter
     * @return set of matching files
     * @throws IOException
     */
    public static Set<File> searchFiles(
            final File dir,
            final Predicate<File> dirFilter,
            final Predicate<File> fileFilter) throws IOException {
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
     * @throws IOException
     */
    public static void searchFiles(
            final File dir,
            final Predicate<File> dirFilter,
            final Predicate<File> fileFilter,
            final Consumer<File> onFileFound) throws IOException {
        if(!dir.exists()) {
            return;
        }
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

    /**
     *
     * @param file
     * @return optionally {@code file.getCanonicalPath()} based on whether {@code file}
     * is not {@code null} and the 'file I/O system' can handle this call without
     * throwing an exception.
     */
    public static Optional<String> canonicalPath(@Nullable final File file) {
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
    public static String toRelativePath(@NonNull final String commonPath, @NonNull final String absolutePath) {
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
    public static void deleteFile(@Nullable final File file) {
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
    public static File tempDir(final String name) {
        val tempDir =  Files.createTempDirectory(name).toFile();
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
     * @return
     * @throws IllegalArgumentException if any pre-existing file is in conflict
     */
    public static File makeDir(final @Nullable File directory) {
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
    public static Optional<File> existingFile(final @Nullable File file) {
        return file!=null
                && file.isFile()
                ? Optional.of(file)
                : Optional.empty();
    }

    /**
     * Optionally given file, based on whether non-null and exists and is a directory (not a file).
     */
    public static Optional<File> existingDirectory(final @Nullable File file) {
        return file!=null
                && file.isDirectory()
                ? Optional.of(file)
                : Optional.empty();
    }

    /**
     * Copy {@code from} file {@code to} file, replacing existing.
     * @param from
     * @param to
     */
    @SneakyThrows
    public static void copy(final @NonNull File from, final @NonNull File to) {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}
