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
package org.apache.isis.tooling.model4adoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.springframework.lang.Nullable;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;

import lombok.SneakyThrows;
import lombok.val;

/**
 * Writes an (AsciiDoc) Document Model to a given {@link Writer}.
 * @since Sep 10, 2020
 * @apiNote The <i>AsciiDoc<i> name is trademarked by the <i>Eclipse Foundation</i>.
 * <p>
    This project is <b>not</b> part of the specification effort for <i>AsciiDoc<i> under the
    <i>AsciiDoc Working Group</i>. See https://projects.eclipse.org/proposals/asciidoc-language
    and https://accounts.eclipse.org/mailing-list/asciidoc-wg. However, we are happy to
    help with transfer of source code, if any project (under the umbrella of the
    <i>AsciiDoc Working Group</i>) is willing to take over.
    </p>
 */
public class AsciiDocWriter {

    /**
     * the inverse of {@link Asciidoctor#load(String, java.util.Map)}}
     */
    @SneakyThrows
    public static String toString(final @Nullable Document doc) {
        if(doc==null) {
            return "";
        }
        val adocWriter = new AsciiDocWriter();
        val stringWriter = new StringWriter();
        adocWriter.write(doc, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Print to given {@link File}
     * @param doc
     * @param file
     */
    @SneakyThrows
    public static void writeToFile(final @Nullable Document doc, final @Nullable File file) {
        if(doc==null
                || file==null) {
            return;
        }
        val adocWriter = new AsciiDocWriter();
        try(FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            adocWriter.write(doc, writer);
        }
    }

    /**
     * Print to given {@link PrintStream}
     * @param doc
     * @param out
     */
    @SneakyThrows
    public static void print(final @Nullable Document doc, final @Nullable PrintStream out) {
        if(doc==null
                || out==null) {
            return;
        }
        val adocWriter = new AsciiDocWriter();
        try(val writer = new PrintWriter(out)) {
            adocWriter.write(doc, writer);
        }
    }

    /**
     * Print to {@link System#out}
     * @param doc
     */
    @SneakyThrows
    public static void print(final @Nullable Document doc) {
        print(doc, System.out);
    }

    /**
     * the inverse of {@link Asciidoctor#load(String, java.util.Map)}}
     */
    @SneakyThrows
    public void write(final @Nullable Document doc, final @Nullable Writer writer) throws IOException {
        if(doc==null
                || writer==null) {
            return;
        }

        val nodeWriter = new NodeWriter(writer);
        StructuralNodeTraversor.depthFirst(nodeWriter, doc);
        writer.flush();
    }


}
