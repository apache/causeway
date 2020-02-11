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
package org.apache.isis.core.commons.internal.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.isis.core.commons.internal.base._Either;

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
public class _Json {

    // -- STREAM CONTENT

    /**
     * Deserialize JSON content from given JSON content InputStream into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T readJson(final Class<T> clazz, InputStream content) 
            throws JsonParseException, JsonMappingException, IOException {

        return (T) new ObjectMapper().readValue(content, clazz);
    }

    /**
     * Either deserialize JSON content from given JSON content InputStream into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> _Either<T, Exception> tryReadJson(final Class<T> clazz, InputStream content) {
        try {
            return _Either.left(readJson(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

    /**
     * Deserialize JSON content from given JSON content InputStream into an instance of List 
     * with given {@code elementType}.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> List<T> readJsonList(final Class<T> elementType, InputStream content) 
            throws JsonParseException, JsonMappingException, IOException {

        val mapper = new ObjectMapper();
        val listFactory = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(content, listFactory);    
    }

    /**
     * Either deserialize JSON content from given JSON content InputStream into an instance of List
     * with given {@code elementType}, or any exception that occurred during parsing.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     */
    public static <T> _Either<List<T>, Exception> tryReadJsonList(final Class<T> clazz, InputStream content) {
        try {
            return _Either.left(readJsonList(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }


    // -- STRING CONTENT

    /**
     * Deserialize JSON content from given JSON content String into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T readJson(final Class<T> clazz, String content) 
            throws JsonParseException, JsonMappingException, IOException {

        return (T) new ObjectMapper().readValue(content, clazz);
    }

    /**
     * Either deserialize JSON content from given JSON content String into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> _Either<T, Exception> tryReadJson(final Class<T> clazz, String content) {
        try {
            return _Either.left(readJson(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

    /**
     * Deserialize JSON content from given JSON content String into an instance of List 
     * with given {@code elementType}.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> List<T> readJsonList(final Class<T> elementType, String content) 
            throws JsonParseException, JsonMappingException, IOException {

        val mapper = new ObjectMapper();
        val listFactory = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(content, listFactory);    
    }

    /**
     * Either deserialize JSON content from given JSON content String into an instance of List
     * with given {@code elementType}, or any exception that occurred during parsing.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     */
    public static <T> _Either<List<T>, Exception> tryReadJsonList(final Class<T> clazz, String content) {
        try {
            return _Either.left(readJsonList(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }


    // -- FILE CONTENT

    /**
     * Deserialize JSON content from given JSON content File into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T readJson(final Class<T> clazz, File content) 
            throws JsonParseException, JsonMappingException, IOException {

        return (T) new ObjectMapper().readValue(content, clazz);
    }

    /**
     * Either deserialize JSON content from given JSON content File into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> _Either<T, Exception> tryReadJson(final Class<T> clazz, File content) {
        try {
            return _Either.left(readJson(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

    /**
     * Deserialize JSON content from given JSON content File into an instance of List 
     * with given {@code elementType}.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> List<T> readJsonList(final Class<T> elementType, File content) 
            throws JsonParseException, JsonMappingException, IOException {

        val mapper = new ObjectMapper();
        val listFactory = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(content, listFactory);    
    }

    /**
     * Either deserialize JSON content from given JSON content File into an instance of List
     * with given {@code elementType}, or any exception that occurred during parsing.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     */
    public static <T> _Either<List<T>, Exception> tryReadJsonList(final Class<T> clazz, File content) {
        try {
            return _Either.left(readJsonList(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

    // -- BYTE CONTENT

    /**
     * Deserialize JSON content from given JSON content byte[] into an instance of 
     * given {@code clazz} type.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> T readJson(final Class<T> clazz, byte[] content) 
            throws JsonParseException, JsonMappingException, IOException {

        return (T) new ObjectMapper().readValue(content, clazz);
    }

    /**
     * Either deserialize JSON content from given JSON content byte[] into an instance of 
     * given {@code clazz} type, or any exception that occurred during parsing.
     * @param <T>
     * @param clazz
     * @param content
     * @return
     */
    public static <T> _Either<T, Exception> tryReadJson(final Class<T> clazz, byte[] content) {
        try {
            return _Either.left(readJson(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

    /**
     * Deserialize JSON content from given JSON content byte[] into an instance of List 
     * with given {@code elementType}.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> List<T> readJsonList(final Class<T> elementType, byte[] content) 
            throws JsonParseException, JsonMappingException, IOException {

        val mapper = new ObjectMapper();
        val listFactory = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(content, listFactory);    
    }

    /**
     * Either deserialize JSON content from given JSON content byte[] into an instance of List
     * with given {@code elementType}, or any exception that occurred during parsing.
     * @param <T>
     * @param elementType
     * @param content
     * @return
     */
    public static <T> _Either<List<T>, Exception> tryReadJsonList(final Class<T> clazz, byte[] content) {
        try {
            return _Either.left(readJsonList(clazz, content));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }
    
    // -- WRITING

    public static String toString(Object pojo) throws JsonProcessingException {
        val objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(pojo);
    }
    
    public static <T> _Either<String, Exception> tryToString(Object pojo) {
        try {
            return _Either.left(toString(pojo));
        } catch (Exception e) {
            return _Either.right(e);
        }
    }

}
