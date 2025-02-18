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
package org.apache.causeway.applib.value;

import java.io.Serializable;
import java.util.function.UnaryOperator;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.commons.internal.resources._Resources;

/**
 * Represents a local resource path, typically a relative path originating at this web-app's
 * root or context-root.
 * <p>
 * Action results of type {@link LocalResourcePath} are interpreted as
 * browser/client redirects, if applicable.
 * <p>
 * {@link OpenUrlStrategy} gives control on whether the redirect URL should open in the same
 * or a new window/tap.
 *
 * @since 2.0 {@index}
 * @see OpenUrlStrategy
 */
@Named(LocalResourcePath.LOGICAL_TYPE_NAME)
@Value
@XmlJavaTypeAdapter(LocalResourcePath.JaxbToStringAdapter.class)   // for JAXB view model support
public record LocalResourcePath(
    @NonNull String path, 
    @NonNull OpenUrlStrategy openUrlStrategy) implements Serializable {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".value.LocalResourcePath";

    public LocalResourcePath(final @Nullable String path) {
        this(path, null);
    }

    // canonical constructor
    public LocalResourcePath(
            final @Nullable String path,
            final @Nullable OpenUrlStrategy openUrlStrategy) {

        validate(path); // may throw IllegalArgumentException

        this.path = path != null
                ? path
                : "";
        this.openUrlStrategy = openUrlStrategy != null
                ? openUrlStrategy
                : OpenUrlStrategy.NEW_WINDOW; // default
    }
    
    /**
     * use {@link #openUrlStrategy()} instead
     */
    @Deprecated public OpenUrlStrategy getOpenUrlStrategy() { return openUrlStrategy; }

    public String getValue() {
        return path;
    }

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
            // path syntax check
            _Resources.url("http://localhost/"+path);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("the given local path has an invalid syntax: '%s'", path), e);
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
