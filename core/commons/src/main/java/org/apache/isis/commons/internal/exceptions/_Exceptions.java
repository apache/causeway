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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.functions._Functions;

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
 * @since 2.0.0
 */
public final class _Exceptions {

	private _Exceptions(){}
	
	// -- FRAMEWORK INTERNAL ERRORS 
	
	/**
	 * Most likely to be used in switch statements to handle the default case.  
	 * @param _case the unmatched case to be reported
	 * @return
	 */
	public static final IllegalArgumentException unmatchedCase(@Nullable Object _case) {
		return new IllegalArgumentException("internal error: unmatched case in switch statement: "+_case);
	}
	
	/**
	 * Most likely to be used in switch statements to handle the default case.
	 * @param format like in {@link java.lang.String#format(String, Object...)}
	 * @param _case the unmatched case to be reported
	 * @return
	 */
	public static final IllegalArgumentException unmatchedCase(String format, @Nullable Object _case) {
		Objects.requireNonNull(format);
		return new IllegalArgumentException(String.format(format, _case));
	}
	
	public static final IllegalStateException unexpectedCodeReach() {
		return new IllegalStateException("internal error: code was reached, that is expected unreachable");
	}	
	
	public static IllegalStateException notImplemented() {
		return new IllegalStateException("internal error: code was reached, that is not implemented yet");
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
	 * @return
	 */
	public static IllegalStateException throwNotImplemented() {
		throw notImplemented();
	}
	
	
	// -- STACKTRACE UTILITITIES
	
	public static final Stream<String> streamStacktraceLines(@Nullable Throwable ex, int maxLines) {
		if(ex==null) {
			return Stream.empty();
		}
		return _NullSafe.stream(ex.getStackTrace())
				.map(StackTraceElement::toString)
				.limit(maxLines);
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
