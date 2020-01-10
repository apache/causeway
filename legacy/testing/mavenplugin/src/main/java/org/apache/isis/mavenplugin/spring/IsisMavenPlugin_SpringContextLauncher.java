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
package org.apache.isis.mavenplugin.spring;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Lists;

import lombok.extern.log4j.Log4j2;

/**
 * Creates a Spring Context from scratch, adding the
 * {@link IsisMavenPlugin_SpringContextConfig}
 * and all project's base packages to scan for configuration beans.
 * 
 * @since 2.0
 *
 */
@Log4j2
public class IsisMavenPlugin_SpringContextLauncher {

    private final static String exclude_from_scan = "org.apache.isis.core";

    public static ConfigurableApplicationContext getContext(/*MavenProject project, Log log*/) {

        Set<String> packages = scanForPackages(/*project*/);

        log.info("packages from package-scan: " + packages);

        String[] basePackages = packages.stream()
                .collect(_Arrays.toArray(String.class));

        AnnotationConfigApplicationContext springContext =  new AnnotationConfigApplicationContext();
        springContext.register(IsisMavenPlugin_SpringContextConfig.class);
        if(basePackages.length>0) {
            springContext.scan(basePackages);
        }
        springContext.refresh();

        return springContext;
    }

    // -- COLLECT PACKAGE NAMES

    private static Set<String> scanForPackages(/*MavenProject topProject*/) {
        Set<String> packageNames = new HashSet<String>();

        //        Stream.concat(Stream.of(topProject), topProject.getCollectedProjects().stream())
        //        .forEach(project->{
        //            
        //            for(String className : scanForClassNames(project)) {
        //                if(className.contains(".")) {
        //                    int endIndex = className.lastIndexOf('.');
        //                    String packageName = className.substring(0, endIndex);
        //                    if(
        //                            !packageName.equals(exclude_from_scan) && 
        //                            !packageName.startsWith(exclude_from_scan + ".") ) {
        //                    
        //                        packageNames.add(packageName);
        //                    }
        //                     
        //                }
        //            }
        //            
        //        });
        return packageNames;
    }

    // -- COLLECT ALL CLASSNAMES

    private static Set<String> scanForClassNames(/*MavenProject project*/) {
        Set<String> classNames = new HashSet<String>();

        List<String> roots = _Lists.newArrayList();
        //(List<String>)(project.getCompileSourceRoots()); 
        for (String root : roots) {

            toPath(root).ifPresent(rootPath->{

                for (String path : getFiles(rootPath)) {
                    String relPath = rootPath.relativize(Paths.get(path)).toString();
                    if (!relPath.endsWith(".java")) {
                        continue;
                    }

                    String name = relPath.replaceFirst("\\.java$", "").replace("/", ".");
                    classNames.add(name);
                }

            });

        }
        return classNames;
    }

    private static Optional<Path> toPath(String name) {
        try {

            Path path = Paths.get(name);
            File file = path.toFile();
            if(!file.exists()) {

                System.out.println("not found: " + name);

                return Optional.empty();
            }
            return Optional.of(path);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static List<String> getFiles(Path root) {
        final List<String> paths = new ArrayList<String>();

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    paths.add(file.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("While class-name scanning: Unable to walk directory: " + root, ex);
        }

        return paths;
    }

}
