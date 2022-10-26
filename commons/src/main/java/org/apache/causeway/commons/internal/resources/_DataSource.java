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
package org.apache.causeway.commons.internal.resources;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * Data Source (as opposed to Data Sink)
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public interface _DataSource extends Supplier<InputStream>  {

    // -- INTERFACE

    /**
     * Can be used as the key, when collecting {@link _DataSource}(s) into a {@link Map}.
     */
    Serializable identifier();

    default byte[] asBytes() {
        try(val is = get()){
            return _Bytes.of(is);
        } catch (Exception e) {
            return _Constants.emptyBytes;
        }
    }

    // -- UTILITIES

    default boolean isPresent() {
        try(val is = get()){
            return is!=null;
        } catch (Exception e) {
            return false;
        }
    }

    // -- FACTORIES

    static _DataSource classPathResource(
            final @NonNull Class<?> contextClass,
            final @NonNull String resourceName) {

        @Value
        class Key implements Serializable {
            private static final long serialVersionUID = 1L;

            final @NonNull Class<?> contextClass;
            final @NonNull String resourceName;
        }

        val key = new Key(contextClass, resourceName);

        return new _DataSource() {

            @Override
            public Serializable identifier() {
                return key;
            }

            @Override
            public InputStream get() {
                return _Resources.load(contextClass, resourceName);
            }

        };
    }

    static _DataSource classPathResource(
            final @NonNull URL resourceUrl) {

        return new _DataSource() {

            @Override
            public Serializable identifier() {
                return resourceUrl;
            }

            @Override @SneakyThrows
            public InputStream get() {
                return resourceUrl.openStream();
            }

        };
    }

    static _DataSource classPathResource(
            final @NonNull String absoluteResourceName) {
        if(!absoluteResourceName.startsWith("/")) {
            throw _Exceptions
            .illegalArgument("invalid absoluteResourceName %s", absoluteResourceName);
        }

        val resourceUrl = _Context.getDefaultClassLoader().getResource(absoluteResourceName);
        if(resourceUrl==null) {
            throw _Exceptions
            .noSuchElement("resource not found %s", absoluteResourceName);
        }

        return classPathResource(resourceUrl);

    }

}
