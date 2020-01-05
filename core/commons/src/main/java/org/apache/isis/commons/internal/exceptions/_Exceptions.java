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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.functions._Functions;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

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
    public static final IllegalArgumentException unmatchedCase(@Nullable Object _case) {
        return new IllegalArgumentException("internal error: unmatched case in switch statement: "+_case);
    }

    /**
     * @param format like in {@link java.lang.String#format(String, Object...)}
     * @param args
     * @return new IllegalArgumentException
     */
    public static final IllegalArgumentException illegalArgument(
            final String format,
            final @Nullable Object ... args) {
        requires(format, "format");
        return new IllegalArgumentException(String.format(format, args));
    }

    public static IllegalStateException illegalState(
            final String format,
            final @Nullable Object ... args) {
        requires(format, "format");
        return new IllegalStateException(String.format(format, args));
    }

    public static final NoSuchElementException noSuchElement(String msg) {
        return new NoSuchElementException(msg);
    }

    public static final NoSuchElementException noSuchElement(String format, Object ...args) {
        requires(format, "format");
        return noSuchElement(String.format(format, args));
    }

    public static final IllegalStateException unexpectedCodeReach() {
        return new IllegalStateException("internal error: code was reached, that is expected unreachable");
    }

    public static IllegalStateException notImplemented() {
        return new IllegalStateException("internal error: code was reached, that is not implemented yet");
    }

    // -- UNRECOVERABLE
    
    public static RuntimeException unrecoverable(Throwable cause) {
        return new RuntimeException("unrecoverable error: with cause ...", cause);
    }

    public static RuntimeException unrecoverable(String msg) {
        return new RuntimeException(String.format("unrecoverable error: '%s'", msg));
    }

    public static RuntimeException unrecoverable(String msg, Throwable cause) {
        return new RuntimeException(String.format("unrecoverable error: '%s' with cause ...", msg), cause);
    }
    
    public static RuntimeException unrecoverableFormatted(String format, Object ...args) {
        return new RuntimeException(String.format("unrecoverable error: '%s'", 
                String.format(format, args)));
    }
    
    // -- UNSUPPORTED

    public static UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("unrecoverable error: method call not allowed/supported");
    }
    
    
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

    public static <E extends Exception> void throwWhenTrue(E cause, Predicate<E> test) throws E {
        if(test.test(cause)) {
            throw cause;
        }
    }

    // -- STACKTRACE UTILITITIES

    public static final Stream<String> streamStacktraceLines(@Nullable Throwable ex, int maxLines) {
        if(ex==null) {
            return Stream.empty();
        }
        return stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(maxLines);
    }

    /**
     * Dumps the current thread's stack-trace onto the given {@code writer}.
     * @param writer
     * @param skipLines
     * @param maxLines
     */
    public static void dumpStackTrace(PrintStream writer, int skipLines, int maxLines) {
        stream(Thread.currentThread().getStackTrace())
        .map(StackTraceElement::toString)
        .skip(skipLines)
        .limit(maxLines)
        .forEach(writer::println);
    }

    public static void dumpStackTrace() {
        dumpStackTrace(System.out, 0, 1000); 
    }

    // -- CAUSAL CHAIN

    public static List<Throwable> getCausalChain(@Nullable Throwable ex) {
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

    public static Stream<Throwable> streamCausalChain(@Nullable Throwable ex) {
        if(ex==null) {
            return Stream.empty();
        }
        return getCausalChain(ex).stream();
    }

    public static Throwable getRootCause(@Nullable Throwable ex) {
        return _Lists.lastElementIfAny(getCausalChain(ex));
    }
    
    // -- SWALLOW
    
    public static void silence(Runnable runnable) {
        
        val currentThread = Thread.currentThread();
        val silencedHandler = currentThread.getUncaughtExceptionHandler();
        
        currentThread.setUncaughtExceptionHandler((Thread t, Throwable e)->{/*noop*/});
        
        try {
            runnable.run();
        } finally {
            currentThread.setUncaughtExceptionHandler(silencedHandler);
        }
        
    }
    
    // -- PREDICATES
    
    public static boolean containsAnyOfTheseMessages(@Nullable Throwable throwable, @Nullable String ... messages) {
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


    // -- FLUENT EXCEPTION

    /**
     * [ahuber] Experimental, remove if it adds no value. Otherwise expand.
     */
    public static class FluentException<E extends Exception> {

        public static <E extends Exception> FluentException<E> of(E cause) {
            return new FluentException<>(cause);
        }

        private final E cause;

        private FluentException(E cause) {
            requires(cause, "cause");
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

        public void rethrowIf(Predicate<E> condition) throws E {
            requires(condition, "condition");
            if(condition.test(cause)) {
                throw cause;
            }
        }

        public void suppressIf(Predicate<E> condition) throws E {
            requires(condition, "condition");
            if(!condition.test(cause)) {
                throw cause;
            }
        }

        public void rethrowIfMessageContains(String string) throws E {
            requires(string, "string");
            final boolean containsMessage = getMessage().map(msg->msg.contains(string)).orElse(false);
            if(containsMessage) {
                throw cause;
            }
        }

        public void suppressIfMessageContains(String string) throws E {
            requires(string, "string");
            final boolean containsMessage = getMessage().map(msg->msg.contains(string)).orElse(false);
            if(!containsMessage) {
                throw cause;
            }
        }

    }

    // --

    /**
     * [ahuber] Experimental, remove if it adds no value.
     */
    public static class TryContext {

        private final Function<Exception, ? extends RuntimeException> toUnchecked;

        public TryContext(Function<Exception, ? extends RuntimeException> toUnchecked) {
            this.toUnchecked = toUnchecked;
        }

        // -- SHORTCUTS (RUNNABLE)

        public Runnable uncheckedRunnable(_Functions.CheckedRunnable checkedRunnable) {
            return checkedRunnable.toUnchecked(toUnchecked);
        }

        public void tryRun(_Functions.CheckedRunnable checkedRunnable) {
            uncheckedRunnable(checkedRunnable).run();
        }

        // -- SHORTCUTS (FUNCTION)

        public <T, R> Function<T, R> uncheckedFunction(_Functions.CheckedFunction<T, R> checkedFunction) {
            return checkedFunction.toUnchecked(toUnchecked);
        }

        public <T, R> R tryApply(T obj, _Functions.CheckedFunction<T, R> checkedFunction) {
            return uncheckedFunction(checkedFunction).apply(obj);
        }

        // -- SHORTCUTS (CONSUMER)

        public <T> Consumer<T> uncheckedConsumer(_Functions.CheckedConsumer<T> checkedConsumer) {
            return checkedConsumer.toUnchecked(toUnchecked);
        }

        public <T> void tryAccept(T obj, _Functions.CheckedConsumer<T> checkedConsumer) {
            uncheckedConsumer(checkedConsumer).accept(obj);
        }
    }



}
