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

package org.apache.isis.applib.internal.exceptions;

import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.internal.base._NullSafe;

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
	
	// -- STACKTRACE UTILITITIES
	
	public static final Stream<String> streamStacktraceLines(@Nullable Throwable ex, int maxLines) {
		if(ex==null) {
			return Stream.empty();
		}
		return _NullSafe.stream(ex.getStackTrace())
				.map(StackTraceElement::toString)
				.limit(maxLines);
	}

	

	
}
