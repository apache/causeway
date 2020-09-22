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

import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import org.apache.isis.commons.internal.base._Either;

import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for the JSON format.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public class _Yaml {

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
    public static <T> _Either<T, Exception> tryReadYaml(final Class<T> clazz, InputStream content) {
        try {
            return _Either.left(readYaml(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }
    
}
