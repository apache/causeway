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

package org.apache.isis.applib.internal.base;

import static org.apache.isis.applib.internal.base._Strings_SplitIterator.splitIterator;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides String related algorithms.
 * </p><p>
 * Keep the public methods simple, these are basic building blocks for more complex composites.
 * Composites are provided as static fields. 
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public final class _Strings {

	private _Strings() {}
	
	// -- BASICS
	
	public static String[] emptyArray = new String[0];
	
	// -- BASIC PREDICATES
	
	/**
	 * 
	 * @param x
	 * @return true only if string is of zero length or null. 
	 */
	public static boolean isEmpty(final CharSequence x){
		return x==null || x.length()==0;
	}

	/**
	 * 
	 * @param x
	 * @return inverse of isEmpty(CharSequence). 
	 */
	public static boolean isNotEmpty(final CharSequence x){
		return x!=null && x.length()!=0;
	}
	
	// -- BASIC UNARY OPERATORS
	
	/**
	 * Trims the input.
	 * @param input
	 * @return null if the {@code input} is null
	 */
	public static String trim(String input) {
		if(input==null) {
			return null;
		}
		return input.trim();
	}
	
    /**
     * Converts all of the characters in {@code input} to lower case using the rules of the default locale. 
     * @param input
     * @return null if {@code input} is null
     */
    public static String lower(@Nullable final String input) {
    	if(input==null) {
    		return null;
    	}
        return input.toLowerCase();
    }
    
    /**
     * Converts all of the characters in {@code input} to upper case using the rules of the default locale. 
     * @param input
     * @return null if {@code input} is null
     */
    public static String upper(@Nullable final String input) {
    	if(input==null) {
    		return null;
    	}
        return input.toUpperCase();
    }
    
    /**
     * Converts the first character in {@code input} to upper case using the rules of the default locale. 
     * @param input
     * @return null if {@code input} is null
     */
    public static String capitalize(@Nullable final String input) {
    	if(input==null) {
    		return null;
    	}
    	if (input.length() == 0) {
    		return input;
    	}
    	if (input.length() == 1) {
    		return input.toUpperCase();
    	}
    	return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
    
	// -- SPLITTING
	
	/**
	 * Splits the {@code input} into chunks separated by {@code separator}, 
	 * then puts all chunks on the stream.
	 * <p>
	 * Corner cases: 
	 * <ul>
	 * <li>{@code input} starts with {@code separator}: an empty string is the first chunk put on the stream</li>
	 * <li>{@code input} ends with {@code separator}: an empty string is the last chunk put on the stream</li>
	 * <li>a {@code separator} is followed by another: an empty string is put on the stream</li>
	 * </ul> 
	 * @param input
	 * @param separator non-empty string
	 * @return empty stream if {@code input} is null
	 * @throws {@link IllegalArgumentException} if {@code separator} is empty
	 */
	public static Stream<String> splitThenStream(@Nullable final String input, final String separator) {
		if(isEmpty(separator))
			throw new IllegalArgumentException("a non empty separator is required");
		if(isEmpty(input))
			return Stream.of();
		if(!input.contains(separator))
			return Stream.of(input);
		
		return StreamSupport.stream(
		          Spliterators.spliteratorUnknownSize(splitIterator(input, separator), Spliterator.ORDERED),
		          false); // not parallel
	}
    
    // -- REPLACEMENT OPERATORS
    
    /**
     * Condenses any whitespace to the given {@code replacement}
     * 
     * @param input
     * @param replacement
     * @return null if {@code input} is null
     */
    public static String condenseWhitespaces(@Nullable final String input, final String replacement) {
    	if(input==null) {
    		return null;
    	}
    	Objects.requireNonNull(replacement);
        return input.replaceAll("\\s+", replacement);
    }
    
    // -- UNARY OPERATOR COMPOSITION
    
    /**
     * Monadic StringOperator that allows composition of unary string operators.
     */
    public final static class StringOperator {
    	
    	private final UnaryOperator<String> operator;
    	    	
		private StringOperator(UnaryOperator<String> operator) {
			Objects.requireNonNull(operator);
			this.operator = operator;
		}

		public String apply(String input) {
			return operator.apply(input);
		}
		
		public StringOperator andThen(UnaryOperator<String> andThen) {
			return new StringOperator(s->andThen.apply(operator.apply(s)));
		}
    	
    }
    
    /**
     * Returns a monadic StringOperator that allows composition of unary string operators
     * @return
     */
    public static StringOperator operator() {
		return new StringOperator(UnaryOperator.identity());
    }
    
    // -- SPECIAL COMPOSITES 
    
    // using naming convention asXxx...
    
    public final static StringOperator asLowerDashed = operator()
        	.andThen(_Strings::lower)
        	.andThen(s->_Strings.condenseWhitespaces(s, "-"));

 	public final static StringOperator asNormalized = operator()
 			.andThen(s->_Strings.condenseWhitespaces(s, " "));
    
 	public final static StringOperator asNaturalName2 = operator()
 			.andThen(s->_Strings_NaturalNames.naturalName2(s, true));

    
}
