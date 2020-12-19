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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.isis.commons.functional.Result;

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

    private static <T> T _readJson(final Class<T> clazz, InputStream content) 
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
    public static <T> Result<T> readJson(final Class<T> clazz, InputStream content) {
        return Result.of(()->_readJson(clazz, content));
    }

    private static <T> List<T> _readJsonList(final Class<T> elementType, InputStream content) 
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
    public static <T> Result<List<T>> readJsonList(final Class<T> clazz, InputStream content) {
        return Result.of(()->_readJsonList(clazz, content));
    }


    // -- STRING CONTENT

    private static <T> T _readJson(final Class<T> clazz, String content) 
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
    public static <T> Result<T> readJson(final Class<T> clazz, String content) {
        return Result.of(()->_readJson(clazz, content));
    }

    private static <T> List<T> _readJsonList(final Class<T> elementType, String content) 
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
    public static <T> Result<List<T>> readJsonList(final Class<T> clazz, String content) {
        return Result.of(()->_readJsonList(clazz, content));
    }


    // -- FILE CONTENT

    private static <T> T _readJson(final Class<T> clazz, File content) 
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
    public static <T> Result<T> readJson(final Class<T> clazz, File content) {
        return Result.of(()->_readJson(clazz, content));
    }

    private static <T> List<T> _readJsonList(final Class<T> elementType, File content) 
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
    public static <T> Result<List<T>> readJsonList(final Class<T> clazz, File content) {
        return Result.of(()->_readJsonList(clazz, content));
    }

    // -- BYTE CONTENT

    private static <T> T _readJson(final Class<T> clazz, byte[] content) 
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
    public static <T> Result<T> readJson(final Class<T> clazz, byte[] content) {
        return Result.of(()->_readJson(clazz, content));
    }

    private static <T> List<T> _readJsonList(final Class<T> elementType, byte[] content) 
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
    public static <T> Result<List<T>> readJsonList(final Class<T> clazz, byte[] content) {
        return Result.of(()->_readJsonList(clazz, content));
    }
    
    // -- WRITING

    public static String toString(ObjectMapper objectMapper, Object pojo) throws JsonProcessingException {
        return objectMapper.writeValueAsString(pojo);
    }
    
    
    public static String toString(Object pojo) throws JsonProcessingException {
        val objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        return toString(objectMapper, pojo);
    }
    
    public static <T> Result<String> tryToString(ObjectMapper objectMapper, Object pojo) {
        return Result.of(()->toString(objectMapper, pojo));
    }
    
    public static <T> Result<String> tryToString(Object pojo) {
        return Result.of(()->toString(pojo));
    }

}
