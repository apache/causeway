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

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides byte[] related algorithms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public final class _Bytes {

	private _Bytes(){}
	
	// -- BASE-64 ENCODING

	/**
	 * Encodes all bytes from the specified byte array into a newly-allocated byte array using 
	 * the specified {@code encoder}.
	 * @param encoder
	 * @param input
	 * @return null if {@code input} is null
	 */
	public final static byte[] encodeToBase64(Base64.Encoder encoder, final byte[] input) {
		if(input==null) {
			return null;
		}
		Objects.requireNonNull(encoder);
		return encoder.encode(input);
	}

	/**
	 * Decodes all bytes from the input byte array using the specified {@code decoder}, writing the 
	 * results into a newly-allocated output byte array.
	 * @param decoder
	 * @param base64
	 * @return null if {@code base64} is null
	 */
	public final static byte[] decodeBase64(Base64.Decoder decoder, final byte[] base64) {
		if(base64==null) {
			return null;
		}
		Objects.requireNonNull(decoder);
		return decoder.decode(base64);
	}

	// -- COMPRESSION
	
	/**
	 * Compresses the given byte array, without being specific about the used algorithm.<br/>
	 * However, following symmetry holds: <br/>
	 * {@code x == decompress(compress(x))}
	 * @param input
	 * @return null if {@code input} is null
	 */
	public static final byte[] compress(byte[] input) {
		if(input==null) {
			return null;
		}
		if(input.length==0) {
			return input;
		}
		try {
			return _Bytes_GZipCompressor.compress(input);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Decompresses the given byte array, without being specific about the used algorithm.<br/>
	 * However, following symmetry holds: <br/>
	 * {@code x == decompress(compress(x))}
	 * @param compressed
	 * @return null if {@code compressed} is null
	 */
	public static final byte[] decompress(byte[] compressed) {
		if(compressed==null) {
			return null;
		}
		if(compressed.length==0) {
			return compressed;
		}
		try {
			return _Bytes_GZipCompressor.decompress(compressed);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	// -- UNARY OPERATOR COMPOSITION

	/**
	 * Monadic BytesOperator that allows composition of unary byte[] operators.
	 */
	public final static class BytesOperator {

		private final UnaryOperator<byte[]> operator;

		private BytesOperator(UnaryOperator<byte[]> operator) {
			Objects.requireNonNull(operator);
			this.operator = operator;
		}

		public byte[] apply(byte[] input) {
			return operator.apply(input);
		}

		public BytesOperator andThen(UnaryOperator<byte[]> andThen) {
			return new BytesOperator(s->andThen.apply(operator.apply(s)));
		}

	}
	
	/**
	 * Returns a monadic BytesOperator that allows composition of unary byte[] operators
	 * @return
	 */
	public static BytesOperator operator() {
		return new BytesOperator(UnaryOperator.identity());
	}
	
	// -- SPECIAL COMPOSITES 

	// using naming convention asX../ofX..

	public final static BytesOperator asUrlBase64 = operator()
			.andThen(bytes->encodeToBase64(Base64.getUrlEncoder(), bytes));
	
	public final static BytesOperator ofUrlBase64 = operator()
			.andThen(bytes->decodeBase64(Base64.getUrlDecoder(), bytes));
	
	public final static BytesOperator asCompressedUrlBase64 = operator()
			.andThen(_Bytes::compress)
			.andThen(bytes->encodeToBase64(Base64.getUrlEncoder(), bytes));
	
	public final static BytesOperator ofCompressedUrlBase64 = operator()
			.andThen(bytes->decodeBase64(Base64.getUrlDecoder(), bytes))
			.andThen(_Bytes::decompress);
	
	// --
	
	
}
