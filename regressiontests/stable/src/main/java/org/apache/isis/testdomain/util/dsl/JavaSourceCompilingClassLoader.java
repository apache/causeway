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
package org.apache.isis.testdomain.util.dsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.functions._Functions;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JavaSourceCompilingClassLoader extends ClassLoader  {

    public static JavaSourceCompilingClassLoader newInstance() {
        return new JavaSourceCompilingClassLoader();
    }

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final Path root;
    private final Map<String, ClassHandle> classHandles = _Maps.newHashMap();

    @SneakyThrows
    private JavaSourceCompilingClassLoader() {
        // purely for the purpose of communicating with the compiler
        this.root = Files.createTempDirectory("isis-dsl-cl");
        this.root.toFile().deleteOnExit();
        log.info("tmp dir created in {}", root);

        Thread printingHook = new Thread(() -> {
            try {
                _Files.deleteDirectory(root.toFile());
                System.out.println("JavaSourceCompilingClassLoader: Cleaning up temp files done.");
            } catch (Exception e) {
                System.err.println("JavaSourceCompilingClassLoader: Cleaning up temp files FAILED.");
                e.printStackTrace();
            }
        });
        Runtime.getRuntime().addShutdownHook(printingHook);

    }

    @SneakyThrows
    public void writeJavaSource(final String className, final _Functions.CheckedConsumer<Appendable> writer) {
        var classHandle = classHandles.computeIfAbsent(className, ClassHandle::new);
        File sourceFile = new File(root.toFile(), classHandle.releativeFilePath + ".java");
        sourceFile.getParentFile().mkdirs();

        var sb = new StringBuilder();
        writer.accept(sb);
        Files.writeString(sourceFile.toPath(), sb, StandardCharsets.UTF_8);
        compiler.run(null, null, null, sourceFile.getPath());
    }

    @Override
    public Class<?> findClass(final String className) throws ClassNotFoundException {

        var classHandle = classHandles.get(className);
        if(classHandle==null) {
            throw new ClassNotFoundException(className);
        }

        byte[] b;
        try {
            b = loadClass(classHandle);
        } catch (Exception e) {
            throw new ClassNotFoundException(className, e);
        }
        return defineClass(className, b, 0, b.length);
    }

    private class ClassHandle {
        final String name;
        final String releativeFilePath;
        final ClassLoader parentLoader;
        final AtomicBoolean isCompiled = new AtomicBoolean();

        public ClassHandle(final String className) {
            super();
            this.name = className;
            this.releativeFilePath = className.replace('.', File.separatorChar);
            this.parentLoader = JavaSourceCompilingClassLoader.class.getClassLoader();
        }

        File classFile() {
            return new File(root.toFile(), releativeFilePath + ".class");
        }
        File sourceFile() {
            return new File(root.toFile(), releativeFilePath + ".java");
        }
    }

    private byte[] loadClass(final ClassHandle classHandle) throws Exception {
        if(!classHandle.isCompiled.get()) {

            var requireCompile =
            classHandles.values().stream()
            .filter(ch->!ch.isCompiled.get())
            .collect(Can.toCan());

            compile(requireCompile);
        }
        return _Bytes.of(new FileInputStream(classHandle.classFile()));
    }

    private void compile(final Can<ClassHandle> requireCompile) {

        var fileNames =
        requireCompile
        .map(classHandle->classHandle.sourceFile().getPath())
        .toArray(new String[0]);

        compiler.run(null, null, null, fileNames);

        // set the flag regardless of success, so does not run again
        requireCompile.forEach(classHandle->classHandle.isCompiled.set(true));

    }

}
