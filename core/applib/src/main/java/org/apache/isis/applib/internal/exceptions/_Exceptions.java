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

import java.util.stream.Stream;

import org.apache.isis.applib.internal.base._NullSafe;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * A collection of framework internal exceptions.
 * <p>
 * WARNING: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * Public access will be removed once we migrate to Java 9+ modules.
 * 
 * @since 2.0.0
 */
public final class _Exceptions {

	private _Exceptions(){}
	
	public static final RuntimeException unmatchedCase(Object _case) {
		return new RuntimeException("internal error: unmatched case in switch statement: "+_case);
	}
	
	public static final RuntimeException unexpectedCodeReach() {
		return new RuntimeException("internal error: code was reached, that is expected unreachable");
	}	
	
	public static final Stream<String> streamStacktraceLines(Throwable e, int maxLines) {
		return _NullSafe.stream(e.getStackTrace())
				.map(StackTraceElement::toString)
				.limit(maxLines);
	}

	
}
