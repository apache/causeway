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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Table;

import org.apache.isis.tooling._infra._Strings;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class AsciiDocWriter {

    /**
     * the inverse of {@link Asciidoctor#load(String, java.util.Map)}}
     * @throws IOException 
     */
    public static String toString(Document doc) throws IOException {
        if(doc==null) {
            return "";
        }
        val adocWriter = new AsciiDocWriter();
        val stringWriter = new StringWriter();
        adocWriter.write(doc, stringWriter);
        return stringWriter.toString();
    }
  
    /**
     * the inverse of {@link Asciidoctor#load(String, java.util.Map)}}
     * @throws IOException 
     */
    public void write(Document doc, Writer writer) throws IOException {
        
        if(doc==null) {
            return;
        }
        
        val formatWriter = new FormatWriter(writer);
        
        formatWriter.ifNonEmpty("= %s\n", doc.getTitle());
        
        for(val block : doc.getBlocks()) {
            if(block instanceof Table) {
                write((Table)block, formatWriter);
                continue;
            }
            //TODO handle other types of blocks as well
            log.warn("unknown block type detected (possibly not implemented yet) {}", block.getClass().getName());
        }
        
    }

    // -- TABLE

//  |===
//  |Name of Column 1 |Name of Column 2 |Name of Column 3 
//
//  |Cell in column 1, row 1
//  |Cell in column 2, row 1
//  |Cell in column 3, row 1
//
//  |Cell in column 1, row 2
//  |Cell in column 2, row 2
//  |Cell in column 3, row 2
//  |===
    private void write(Table table, FormatWriter writer) throws IOException {
        writer.ifNonEmpty(".%s\n", table.getTitle());
        
        writer.appendMap("[%s]\n", formatedAttrMap(table), "%s=\"%s\"");
        
        writer.append("|===\n");
        
        // table.getColumns() ... styles
        
        for(val headRow : table.getHeader()) {
            for(val cell : headRow.getCells()) {
                writer.append("|%s ", cell.getSource());    
            }
            writer.append("\n");
        }
        
        for(val row : table.getBody()) {
            writer.append("\n"); // empty line before each row
            for(val cell : row.getCells()) {
                writer.append("|%s\n", cell.getSource());    
            }
        }
        
        writer.append("|===\n");
    }
    
    private static Map<String, String> formatedAttrMap(Table table) {
        final Map<String, String> formatedAttrMap = new LinkedHashMap<>();
        
        if(table.hasAttribute("cols")) {
            formatedAttrMap.put("cols", (String)table.getAttribute("cols"));
        }
        
        val options = table.getAttributes().entrySet()
                .stream()
                .filter(entry->entry.getKey().endsWith("-option"))
                .map(entry->entry.getKey().substring(0, entry.getKey().length()-7))
                .collect(Collectors.joining(","));
        
        if(!options.isEmpty()) {
            formatedAttrMap.put("options", options);
        }
        
        return formatedAttrMap;
    }
    
    
    @RequiredArgsConstructor
    private static class FormatWriter {
        private final Writer writer;
    
        void append(String string) throws IOException {
            writer.append(string);
        }
        
        void append(String format, Object ...args) throws IOException {
            writer.append(String.format(format, args));
        }

        void ifNonEmpty(String format, String string) throws IOException {
            if(_Strings.isNullOrEmpty(string)) {
                return;
            }
            writer.append(String.format(format, string));
        }
        
        void appendMap(String mapFormat, Map<String, String> map, String entryFormat) throws IOException {
            if(map.isEmpty()) {
                return;
            }
            val elements = map.entrySet()
            .stream()
            .map(entry->String.format(entryFormat, entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(", "));
            append(mapFormat, elements);
        }
        
    }
    
    
    
}
