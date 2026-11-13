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
package org.apache.causeway.viewer.commons.prism;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.context._Context;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
class PrismUtils {

    static {
        suppressPolyglotFallbackWarning();
    }

    /**
     * Returns the Prism main JS source
     */
    public Optional<String> jsResourceMain() {
        return lookup("prism/prism.js");
    }

    /**
     * Returns the Prism grammar JS source for selected language
     */
    public Optional<String> jsResource(final PrismLanguage prismLanguage) {
        return lookup(prismLanguage.jsFile());
    }

    @Deprecated
    String mostCommonGrammerAsJs() {
        return PrismLanguage.mostCommon()
            .stream()
            .map(PrismUtils::jsResource)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.joining("\n\n"));
    }

    @SneakyThrows
    Context createPrismContext() {
        var context = Context.newBuilder().engine(ENGINE.get()).build();
        context.eval(PRISM_SOURCE.get());
        return context;
    }

    // -- HELPER

    private static final _Lazy<Engine> ENGINE = _Lazy.threadSafe(Engine::create);
    private static final _Lazy<Source> PRISM_SOURCE = _Lazy.threadSafe(()->Source.create("js", PrismUtils.jsResourceMain().orElseThrow()));

    private static final Map<String, Optional<String>> resourceCache = new ConcurrentHashMap<>();
    private Optional<String> lookup(final String jsRef) {
        return resourceCache.computeIfAbsent(jsRef, PrismUtils::read);
    }

    @SneakyThrows
    private Optional<String> read(final String jsRef) {
        String resourcePath = "META-INF/resources/webjars/" + jsRef;

        InputStream inputStream = _Context.getDefaultClassLoader().getResourceAsStream(resourcePath);
        return inputStream!=null
            ? Optional.of(new String(inputStream.readAllBytes()))
            : Optional.empty();
    }

    private void suppressPolyglotFallbackWarning() {
        //The polyglot engine uses a fallback runtime that does not support runtime compilation to native code.
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

}
