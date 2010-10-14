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


package org.apache.isis.commons.lang;


public final class Maybe<T> {
	
	public final static <T> Maybe<T> setTo(T t) {
		return new Maybe<T>(t);
	}

	/**
	 * A {@link Maybe} that is not set, with the type specified using a class object. 
     *
     * <p>
     * <code>
     * Maybe.notSet(String.class)
     * </code>
	 */
	public final static <T> Maybe<T> notSet(Class<T> cls) {
		return new Maybe<T>(null);
	}

    /**
     * A {@link Maybe} that is not set, with the type specified using a type parameter.
     *
     * <p>
     * <code>
     * Maybe.&lt;String&gt;notSet()
     * </code>
     */
    public final static <T> Maybe<T> notSet() {
        return new Maybe<T>(null);
    }

    public final static <T> Maybe<T> setIf(boolean condition, T t) {
        if (condition) {
            return Maybe.setTo(t);
        } else {
            return Maybe.notSet();
        }
    }

    /**
     * Reciprocal of {@link #setIf(boolean, Object)}.
     */
    public final static <T> Maybe<T> notSetIf(boolean b, T t) {
        return setIf(!b, t);
    }



	private final T t;

	private Maybe(T t) {
		this.t = t;
	}
	
	public boolean isSet() {
		return t != null;
	}
	
	public T get() {
		if (!isSet()) {
			throw new IllegalStateException("No object");
		}
		return t;
	}


}
