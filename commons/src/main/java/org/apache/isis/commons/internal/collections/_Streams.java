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

package org.apache.isis.commons.internal.collections;

import java.util.stream.Stream;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Stream extensions.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Streams {

    private _Streams(){}

    // -- CONCATENATION

    /**
     * 3 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c) {
        return Stream.concat(Stream.concat(a, b), c);
    }

    /**
     * 4 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(
            Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c, Stream<? extends T> d) {

        return Stream.concat(Stream.concat(a, b), Stream.concat(c, d));
    }

    /**
     * 5 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(
            Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c, Stream<? extends T> d,
            Stream<? extends T> e) {

        return Stream.concat(
                Stream.concat(Stream.concat(a, b), Stream.concat(c, d)),
                e);
    }

    /**
     * 6 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(
            Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c, Stream<? extends T> d,
            Stream<? extends T> e, Stream<? extends T> f) {

        return Stream.concat(
                Stream.concat(Stream.concat(a, b), Stream.concat(c, d)),
                Stream.concat(e, f));
    }

    /**
     * 7 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(
            Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c, Stream<? extends T> d,
            Stream<? extends T> e, Stream<? extends T> f, Stream<? extends T> g) {

        return Stream.concat(
                Stream.concat(Stream.concat(a, b), Stream.concat(c, d)),
                Stream.concat(Stream.concat(e, f), g));
    }

    /**
     * 8 param variant of {@link Stream#concat(Stream, Stream)}
     */
    public static <T> Stream<T> concat(
            Stream<? extends T> a, Stream<? extends T> b, Stream<? extends T> c, Stream<? extends T> d,
            Stream<? extends T> e, Stream<? extends T> f, Stream<? extends T> g, Stream<? extends T> h) {

        return Stream.concat(
                Stream.concat(Stream.concat(a, b), Stream.concat(c, d)),
                Stream.concat(Stream.concat(e, f), Stream.concat(g, h)));
    }
}
