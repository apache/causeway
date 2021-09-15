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
package org.apache.isis.applib.value;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.function.UnaryOperator;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Value;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a local resource path, typically a relative path originating at this web-app's
 * root or context-root.
 *
 * <p>
 * Action results of type {@link LocalResourcePath} are interpreted as
 * browser/client redirects, if applicable.
 * </p>
 *
 * <p>
 * {@link OpenUrlStrategy} gives control on whether the redirect URL should open in the same
 * or a new window/tap.
 * </p>
 *
 * @since 2.0 {@index}
 * @see OpenUrlStrategy
 */
@Value(logicalTypeName = IsisModuleApplib.NAMESPACE + ".value.LocalResourcePath")
@XmlJavaTypeAdapter(LocalResourcePath.JaxbToStringAdapter.class)   // for JAXB view model support
public final class LocalResourcePath implements Serializable {

    private static final long serialVersionUID = 1L;
    @NonNull private final String path;
    @Getter @NonNull private final OpenUrlStrategy openUrlStrategy;

    public LocalResourcePath(final @Nullable String path) throws IllegalArgumentException {
        this(path, null);
    }

    public LocalResourcePath(
            final @Nullable String path,
            final @Nullable OpenUrlStrategy openUrlStrategy) throws IllegalArgumentException {

        validate(path); // may throw IllegalArgumentException

        this.path = path != null
                ? path
                : "";
        this.openUrlStrategy = openUrlStrategy != null
                ? openUrlStrategy
                : OpenUrlStrategy.NEW_WINDOW; // default
    }

    @NonNull
    public String getValue() {
        return path;
    }

    @NonNull
    public String getEffectivePath(final @NonNull UnaryOperator<String> contextPathPrepender) {
        return contextPathPrepender.apply(path);
    }

    @Override
    public String toString() {
        return "LocalResourcePath [path=" + path + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj==null) {
            return false;
        }
        return (obj instanceof LocalResourcePath) && isEqualTo((LocalResourcePath) obj);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public boolean isEqualTo(final LocalResourcePath other) {
        if(other==null) {
            return false;
        }
        return this.getValue().equals(other.getValue());
    }

    // -- HELPER

    private void validate(final String path) throws IllegalArgumentException {
        if(path==null) {
            return;
        }
        try {
            // used for syntax testing
            new java.net.URI("http://localhost/"+path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("the given local path has an invalid syntax: '%s'", path), e);
        }
    }


    public static class JaxbToStringAdapter extends XmlAdapter<String, LocalResourcePath> {
        @Override
        public LocalResourcePath unmarshal(final String path) {
            return path != null ? new LocalResourcePath(path) : null;
        }

        @Override
        public String marshal(final LocalResourcePath localResourcePath) {
            return localResourcePath != null ? localResourcePath.getValue() : null;
        }
    }
}
