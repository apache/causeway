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

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.core.io.FileSystemResource;

class DataSourceTest {

    @Test
    void empty() {
        var ds = DataSource.empty();
        assertEquals("Empty-Resource", ds.getDescription());
        assertEquals(Optional.empty(), ds.getFile());
    }

    @Test
    void file() {
        var file = new File("/path/to/file");
        var ds = DataSource.ofFile(new File("/path/to/file"));
        assertEquals(String.format("File-Resource[%s]", file.toString()), ds.getDescription());
        assertEquals(Optional.of(file), ds.getFile());
    }
    
    @Test
    void bytes() {
        var ds = DataSource.ofBytes(new byte[] {1, 2, 3, 9});
        assertEquals(String.format("Byte-Resource[01 02 03 09]"), ds.getDescription());
        assertEquals(Optional.empty(), ds.getFile());
    }
    
    @Test
    void string() {
        var ds = DataSource.ofStringUtf8("Hello World!");
        assertEquals("String-Resource[Hello World!]", ds.getDescription());
        assertEquals(Optional.empty(), ds.getFile());
    }
    
    @Test
    void classResource() {
        var ds = DataSource.ofResource(getClass(), "/path/to/resource");
        assertEquals(String.format("Class-Resource[org.apache.causeway.commons.io.DataSourceTest, /path/to/resource]"), ds.getDescription());
        assertEquals(Optional.empty(), ds.getFile());
    }
    
    @Test
    void springResource() {
        var file = new File("/path/to/file");
        var resource = new FileSystemResource(file);
        var ds = DataSource.ofSpringResource(resource);
        assertEquals(String.format("file [%s]", file.getAbsolutePath()), ds.getDescription());
        assertEquals(Optional.of(file), ds.getFile());
    }
    
    @Test
    void mapped() {
        var file = new File("/path/to/file");
        var ds = DataSource.ofFile(new File("/path/to/file"))
                .map(is->is);
        assertEquals(String.format("File-Resource[%s] mapped", file.toString()), ds.getDescription());
        assertEquals(Optional.empty(), ds.getFile());
    }

}
