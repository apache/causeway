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
package org.apache.isis.commons.internal.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import org.apache.isis.commons.functional.Result;

import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for the YAML format.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public class _Yaml {

    // -- FROM INPUT STREAM
    
    /**
     * Deserialize YAML content from given YAML content InputStream into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> T readYaml(final Class<T> clazz, InputStream content) {
        val yaml = new Yaml(new Constructor(clazz));
        return yaml.load(content);
    }

    /**
     * Either deserialize YAML content from given YAML content InputStream into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> Result<T> tryReadYaml(final Class<T> clazz, InputStream content) {
        return Result.of(()->readYaml(clazz, content));
    }
    
    // -- FROM STRING
    
    /**
     * Deserialize YAML content from given YAML content String into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> T readYaml(final Class<T> clazz, String content) {
        val yaml = new Yaml(new Constructor(clazz));
        return yaml.load(content);
    }

    /**
     * Either deserialize YAML content from given YAML content String into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> Result<T> tryReadYaml(final Class<T> clazz, String content) {
        return Result.of(()->readYaml(clazz, content));
    }
    
    // -- FROM FILE
    
    /**
     * Deserialize YAML content from given YAML content File into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static <T> T readYaml(final Class<T> clazz, File content) throws FileNotFoundException, IOException {
        try(val fis = new FileInputStream(content)) {
            val yaml = new Yaml(new Constructor(clazz));
            return yaml.load(fis);
        }
    }

    /**
     * Either deserialize YAML content from given YAML content File into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> Result<T> tryReadYaml(final Class<T> clazz, File content) {
        return Result.of(()->readYaml(clazz, content));
    }
    
    // -- FROM BYTE ARRAY
    
    /**
     * Deserialize YAML content from given YAML content byte[] into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws IOException 
     */
    public static <T> T readYaml(final Class<T> clazz, byte[] content) throws IOException {
        try(val bais = new ByteArrayInputStream(content)) {
            val yaml = new Yaml(new Constructor(clazz));
            return yaml.load(bais);
        }
    }

    /**
     * Either deserialize YAML content from given YAML content byte[] into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> Result<T> tryReadYaml(final Class<T> clazz, byte[] content) {
        return Result.of(()->readYaml(clazz, content));
    }
    
}
