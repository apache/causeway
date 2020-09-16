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
package org.apache.isis.core.commons.internal.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import lombok.NonNull;
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
        
        final Set<File> fileList = new HashSet<>();
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
    public static Optional<String> canonicalPath(@Nullable File file) {
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
    public static String toRelativePath(@NonNull String commonPath, @NonNull String absolutePath) {
        if(absolutePath.startsWith(commonPath)) {
            return absolutePath.substring(commonPath.length());
        }
        return absolutePath;
    }
    
    
}
