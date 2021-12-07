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
package org.apache.isis.commons.internal.exceptions;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.NonNull;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * A collection of framework internal exceptions and exception related idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Exceptions {

    private _Exceptions(){}

    // -- FRAMEWORK INTERNAL ERRORS

    /**
     * Most likely to be used in switch statements to handle the default case.
     * @param _case the unmatched case to be reported
     * @return new IllegalArgumentException
     */
    public static final IllegalArgumentException unmatchedCase(final @Nullable Object _case) {
        return new IllegalArgumentException("internal error: unmatched case in switch statement: "+_case);
    }

    // -- ILLEGAL ARGUMENT

    /**
     * @param format like in {@link java.lang.String#format(String, Object...)}
     * @param args
     * @return new IllegalArgumentException
     */
    public static final IllegalArgumentException illegalArgument(
            final @NonNull String format,
            final @Nullable Object ... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    // -- ILLEGAL STATE

    public static IllegalStateException illegalState(
            final @NonNull String format,
            final @Nullable Object ... args) {
        return new IllegalStateException(String.format(format, args));
    }

    public static IllegalStateException illegalState(
            final @NonNull Throwable cause,
            final @NonNull String format,
            final @Nullable Object ... args) {
        return new IllegalStateException(String.format(format, args), cause);
    }

    // -- ILLEGAL ACCESS

    public static IllegalAccessException illegalAccess(
            final @NonNull String format,
            final @Nullable Object ... args) {
        return new IllegalAccessException(String.format(format, args));
    }

    // -- NO SUCH ELEMENT

    public static final NoSuchElementException noSuchElement() {
        return new NoSuchElementException();
    }

    public static final NoSuchElementException noSuchElement(final String msg) {
        return new NoSuchElementException(msg);
    }

    public static final NoSuchElementException noSuchElement(
            final @NonNull String format,
            final @Nullable Object ...args) {
        return noSuchElement(String.format(format, args));
    }

    // -- UNEXPECTED CODE REACH

    public static final IllegalStateException unexpectedCodeReach() {
        return new IllegalStateException("internal error: code was reached, that is expected unreachable");
    }

    // -- NOT IMPLEMENTED

    public static IllegalStateException notImplemented() {
        return new IllegalStateException("internal error: code was reached, that is not implemented yet");
    }

    // -- UNRECOVERABLE

    public static RuntimeException unrecoverable(final Throwable cause) {
        return new RuntimeException("unrecoverable error: with cause ...", cause);
    }

    public static RuntimeException unrecoverable(final String msg) {
        return new RuntimeException(String.format("unrecoverable error: '%s'", msg));
    }

    public static RuntimeException unrecoverable(final String msg, final Throwable cause) {
        return new RuntimeException(String.format("unrecoverable error: '%s' with cause ...", msg), cause);
    }

    public static RuntimeException unrecoverableFormatted(final String format, final Object ...args) {
        return new RuntimeException(String.format("unrecoverable error: '%s'",
                String.format(format, args)));
    }

    // -- UNSUPPORTED

    public static UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("unrecoverable error: method call not allowed/supported");
    }

    public static UnsupportedOperationException unsupportedOperation(final String msg) {
        return new UnsupportedOperationException(msg);
    }

    public static UnsupportedOperationException unsupportedOperation(final String format, final Object ...args) {
        return new UnsupportedOperationException(String.format(format, args));
    }

    // -- ASSERT

    public static AssertionError assertionError(final String msg) {
        return new AssertionError(msg);
    }

    // -- MESSAGE

    public static String getMessage(final Throwable ex) {
        if(ex==null) {
            return "no exception present";
        }
        if(_Strings.isNotEmpty(ex.getMessage())) {
            return ex.getMessage();
        }
        val sb = new StringBuilder();
        val nestedMsg = streamCausalChain(ex)
                .peek(throwable->{
                    sb.append(throwable.getClass().getSimpleName()).append("/");
                })
                .map(Throwable::getMessage)
                .filter(_NullSafe::isPresent)
                .findFirst();

        if(nestedMsg.isPresent()) {
            sb.append(nestedMsg.get());
        } else {

            Can.ofArray(ex.getStackTrace())
            .stream()
            .limit(20)
            .forEach(trace->sb.append("\n").append(trace));
        }

        return sb.toString();
    }

    // -- THROWING

    /**
     * Used to hide from the compiler the fact, that this call always throws.
     *
     * <pre>{
     *    throw unexpectedCodeReach();
     *    return 0; // won't compile: unreachable code
     *}</pre>
     *
     * hence ...
     *
     * <pre>{
     *    throwUnexpectedCodeReach();
     *    return 0;
     *}</pre>
     *
     */
    public static void throwUnexpectedCodeReach() {
        throw unexpectedCodeReach();
    }

    /**
     * Used to hide from the compiler the fact, that this call always throws.
     *
     * <pre>{
     *    throw notImplemented();
     *    return 0; // won't compile: unreachable code
     *}</pre>
     *
     * hence ...
     *
     * <pre>{
     *    throwNotImplemented();
     *    return 0;
     *}</pre>
     *
     */
    public static void throwNotImplemented() {
        dumpStackTrace();
        throw notImplemented();
    }

    // -- SELECTIVE ERROR SUPPRESSION

    //	/**
    //	 * Allows to selectively ignore unchecked exceptions. Most likely used framework internally
    //	 * for workarounds, not properly dealing with the root cause. This way at least we know, where
    //	 * we placed such workarounds.
    //	 *
    //	 * @param runnable that might throw an unchecked exception
    //	 * @param suppress predicate that decides whether to suppress an exception
    //	 */
    //	public static void catchSilently(
    //			Runnable runnable,
    //			Predicate<RuntimeException> suppress) {
    //
    //		try {
    //			runnable.run();
    //		} catch (RuntimeException cause) {
    //			if(suppress.test(cause)) {
    //				return;
    //			}
    //			throw cause;
    //		}
    //	}

    // -- SELECTIVE THROW

    public static <E extends Exception> void throwWhenTrue(final E cause, final Predicate<E> test) throws E {
        if(test.test(cause)) {
            throw cause;
        }
    }

    // -- STACKTRACE UTILITITIES

    public static final Stream<String> streamStacktraceLines(final @Nullable Throwable ex, final int maxLines) {
        if(ex==null) {
            return Stream.empty();
        }
        return _NullSafe.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(maxLines);
    }

    public static final String asStacktrace(final @Nullable Throwable ex, final int maxLines, final String delimiter) {
        return _Exceptions.streamStacktraceLines(ex, maxLines)
                .collect(Collectors.joining(delimiter));
    }

    public static final String asStacktrace(final @Nullable Throwable ex, final int maxLines) {
        return asStacktrace(ex, maxLines, "\n");
    }

    public static final String asStacktrace(final @Nullable Throwable ex) {
        return asStacktrace(ex, 1000);
    }

    /**
     * Dumps the current thread's stack-trace onto the given {@code writer}.
     * @param writer
     * @param skipLines
     * @param maxLines
     */
    public static void dumpStackTrace(final PrintStream writer, final int skipLines, final int maxLines) {
        streamStackTrace()
        .map(StackTraceElement::toString)
        .skip(skipLines)
        .limit(maxLines)
        .forEach(writer::println);
    }

    public static void dumpStackTrace() {
        dumpStackTrace(System.err, 0, 1000);
    }

    // -- CAUSAL CHAIN

    public static List<Throwable> getCausalChain(final @Nullable Throwable ex) {
        if(ex==null) {
            return Collections.emptyList();
        }
        final List<Throwable> chain = _Lists.newArrayList();
        Throwable t = ex;
        while(t!=null) {
            chain.add(t);
            t = t.getCause();
        }
        return chain;
    }

    public static Stream<StackTraceElement> streamStackTrace() {
        return _NullSafe.stream(Thread.currentThread().getStackTrace());
    }

    public static Stream<Throwable> streamCausalChain(final @Nullable Throwable ex) {
        if(ex==null) {
            return Stream.empty();
        }
        val chain = getCausalChain(ex);
        return chain.stream();
    }

    public static Throwable getRootCause(final @Nullable Throwable ex) {
        return _Lists.lastElementIfAny(getCausalChain(ex));
    }

    // -- SWALLOW

    public static void silence(final Runnable runnable) {

        val currentThread = Thread.currentThread();
        val silencedHandler = currentThread.getUncaughtExceptionHandler();

        currentThread.setUncaughtExceptionHandler((final Thread t, final Throwable e)->{/*noop*/});

        try {
            runnable.run();
        } finally {
            currentThread.setUncaughtExceptionHandler(silencedHandler);
        }

    }

    // -- PREDICATES

    public static boolean containsAnyOfTheseMessages(
            final @Nullable Throwable throwable,
            final @Nullable String ... messages) {

        if(throwable==null) {
            return false;
        }
        val throwableMessage = throwable.getMessage();
        if(throwableMessage == null || _NullSafe.isEmpty(messages)) {
            return false;
        }
        for (String message : messages) {
            if(_Strings.isNotEmpty(message)
                    && throwableMessage.contains(message)) {
                return true;
            }
        }
        return false;
    }

    // -- STACKTRACE FORMATTING UTILITY

    private final static Map<String, String> packageReplacements = Map.of(
            //"org.apache.isis", "", // unfortunately no IDE support for this (click on StackTraceElement links)
            "org.apache.wicket", "{wkt}",
            "org.springframework", "{spring}",
            "org.apache.tomcat", "{tomcat}",
            "org.apache.catalina", "{catalina}",
            "org.apache.coyote", "{coyote}"
            );

    public static String abbreviate(final String className, final String...compress) {
        val str = className;
        return Stream.concat(
                    _NullSafe.stream(compress).map(prefix->Map.entry(prefix, "")),
                    packageReplacements.entrySet().stream()
                )
                .filter(entry->str.startsWith(entry.getKey()))
                .map(entry->{
                    val replacement = entry.getValue();
                    var s = str;
                    s = s.replace(entry.getKey() + ".", replacement.isEmpty() ? "{" : replacement + ".");
                    val ref = _Refs.stringRef(s);
                    val left = ref.cutAtIndexOfAndDrop(".");
                    val right = ref.getValue();
                    s = replacement.isEmpty()
                            ? left + "}." + right
                            : left + "." + right;
                    return s;
                })
                .findFirst()
                .orElse(str);
    }

    // -- FLUENT EXCEPTION

    /**
     * [ahuber] Experimental, remove if it adds no value. Otherwise expand.
     */
    public static class FluentException<E extends Exception> {

        public static <E extends Exception> FluentException<E> of(final E cause) {
            return new FluentException<>(cause);
        }

        private final E cause;

        private FluentException(final @NonNull E cause) {
            this.cause = cause;
        }

        public E getCause() {
            return cause;
        }

        public Optional<String> getMessage() {
            return Optional.ofNullable(cause.getMessage());
        }

        // -- RE-THROW IDIOMS

        public void rethrow() throws E {
            throw cause;
        }

        public void rethrowIf(final @NonNull Predicate<E> condition) throws E {
            if(condition.test(cause)) {
                throw cause;
            }
        }

        public void suppressIf(final @NonNull Predicate<E> condition) throws E {
            if(!condition.test(cause)) {
                throw cause;
            }
        }

        public void rethrowIfMessageContains(final @NonNull String string) throws E {
            final boolean containsMessage = getMessage().map(msg->msg.contains(string)).orElse(false);
            if(containsMessage) {
                throw cause;
            }
        }

        public void suppressIfMessageContains(final @NonNull String string) throws E {
            final boolean containsMessage = getMessage().map(msg->msg.contains(string)).orElse(false);
            if(!containsMessage) {
                throw cause;
            }
        }

    }




}
