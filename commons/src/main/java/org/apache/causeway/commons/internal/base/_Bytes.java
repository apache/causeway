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
package org.apache.causeway.commons.internal.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.springframework.lang.Nullable;

import org.jspecify.annotations.NonNull;

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
 * @since 2.0
 */
public final class _Bytes {

    private _Bytes(){}

    // -- CONSTRUCTION

    private static final int BUFFER_SIZE = 16 * 1024; // 16k

    /**
     * Reads the input stream into an array of byte, then closes the input (-stream).
     * @param input
     * @return null if {@code input} is null
     * @throws IOException
     */
    public static byte[] of(final @Nullable InputStream input) throws IOException {
        if(input==null) {
            return null;
        }

        try(final ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            final byte[] buffer = new byte[BUFFER_SIZE];

            int nRead;
            while ((nRead = input.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, nRead);
            }
            bos.flush();
            return bos.toByteArray();
        } finally {
            input.close();
        }
    }

    /**
     * Reads the input stream into an array of byte, but does not close the input (-stream).
     * @param input
     * @return null if {@code input} is null
     * @throws IOException
     */
    public static byte[] ofKeepOpen(final @Nullable InputStream input) throws IOException {
        if(input==null) {
            return null;
        }

        try(final ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            final byte[] buffer = new byte[BUFFER_SIZE];

            int nRead;
            while ((nRead = input.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, nRead);
            }
            bos.flush();
            return bos.toByteArray();
        }
    }

    // -- ARRAY TO STREAM AND VICE VERSA

    /**
     * Converts given byte array into a stream of int values,
     * while the element wise type conversion is preserving sign.
     * @apiNote The Java byte type is signed.
     * @see #ofIntStream(IntStream)
     */
    public static IntStream streamAsInts(final @Nullable byte[] bytes) {
        if(bytes==null
                || bytes.length==0) {
            return IntStream.empty();
        }
        return IntStream.range(0, bytes.length)
                .map(index->(bytes[index] & 0xff));
    }

    /**
     * Converts given {@link IntStream} into a byte array,
     * while the element wise type conversion is preserving sign,
     * but ignoring overflow.
     * @apiNote The Java byte type is signed.
     * @implNote certainly not the most efficient algorithm, as we resort to boxing and temporary list creation
     * @see #streamAsInts(byte[])
     */
    private static byte[] ofIntStream(final @Nullable IntStream intStream) {
        if(intStream==null) {
            return new byte[0];
        }
        var listOfInts = intStream
                .boxed()
                .collect(Collectors.toList());
        var bytes = new byte[listOfInts.size()];
        IntStream.range(0, listOfInts.size())
        .forEach(index->bytes[index]=(byte)(int)listOfInts.get(index));
        return bytes;
    }

    // -- TO AND FROM HEX DUMP

    // -- TO AND FROM HEX DUMP

    /**
     * Converts given byte array into a delimiter separated list of 2 character fixed length hex numbers.
     * @apiNote future extensions may support pretty printing, but for now the resulting string is just a single line
     * @see #ofHexDump(String, String)
     */
    public static String hexDump(final @Nullable byte[] bytes, final @Nullable String delimiter) {
        if(bytes==null) {
            return "";
        }
        return _Bytes.streamAsInts(bytes)
                .mapToObj(Integer::toHexString)
                .map(s->s.length()==1
                    ? "0" + s
                    : s)
                .collect(Collectors.joining(_Strings.nullToEmpty(delimiter)));
    }

    /**
     * Shortcut for {@code hexDump(bytes, " ")} using space as delimiter.
     * @see #hexDump(byte[], String)
     */
    public static String hexDump(final @Nullable byte[] bytes) {
        return hexDump(bytes, " ");
    }

    /**
     * Converts given delimiter separated list of 2 character fixed length hex numbers into a byte array.
     * @see #hexDump(byte[], String)
     */
    public static byte[] ofHexDump(final @Nullable String hexDump, final @Nullable String delimiter) {
        if(hexDump==null) {
            return new byte[0];
        }
        final int delimLen = _NullSafe.size(delimiter);
        final int stride = 2 + delimLen;

        final IntStream intStream = IntStream.range(0, (hexDump.length() + delimLen)/stride)
            .mapToObj(i->{
                final int start = i * stride;
                return hexDump.substring(start, start + 2);
            })
            .mapToInt(hex->Integer.parseUnsignedInt(hex, 16));
        return ofIntStream(intStream);
    }

    /**
     * Shortcut for {@code ofHexDump(hexDump, " ")} using space as delimiter.
     * @see #ofHexDump(String, String)
     */
    public static byte[] ofHexDump(final @Nullable String hexDump) {
        return ofHexDump(hexDump, " ");
    }

    // -- PREPEND/APPEND

    /**
     * Copies all bytes from {@code bytes} followed by all bytes from {@code target} into a newly-allocated byte array.
     * @param target
     * @param bytes to be prepended to {@code target}
     * @return bytes + target, or null if both {@code target} and {@code bytes} are null
     */
    public static final byte[] prepend(final @Nullable byte[] target, final @Nullable byte ... bytes) {
        if(target==null) {
            if(bytes==null) {
                return null;
            }
            return bytes.clone();
        }
        if(bytes==null) {
            return target.clone();
        }
        final byte[] result = new byte[target.length + bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        System.arraycopy(target, 0, result, bytes.length, target.length);
        return result;
    }

    /**
     * Copies all bytes from {@code target} followed by all bytes from {@code bytes} into a newly-allocated byte array.
     * @param target
     * @param bytes to be appended to {@code target}
     * @return target + bytes, or null if both {@code target} and {@code bytes} are null
     */
    public static final byte[] append(final @Nullable byte[] target, final @Nullable byte ... bytes) {
        if(target==null) {
            if(bytes==null) {
                return null;
            }
            return bytes.clone();
        }
        if(bytes==null) {
            return target.clone();
        }
        final byte[] result = new byte[target.length + bytes.length];
        System.arraycopy(target, 0, result, 0, target.length);
        System.arraycopy(bytes, 0, result, target.length, bytes.length);
        return result;
    }

    // -- BASE-64 ENCODING

    /**
     * Encodes all bytes from the specified byte array into a newly-allocated byte array using
     * the specified {@code encoder}.
     * @param encoder
     * @param input
     * @return null if {@code input} is null
     */
    public static final byte[] encodeToBase64(final Base64.@NonNull Encoder encoder, final @Nullable byte[] input) {
        return input!=null ? encoder.encode(input) : null;
    }

    /**
     * Decodes all bytes from the input byte array using the specified {@code decoder}, writing the
     * results into a newly-allocated output byte array.
     * @param decoder
     * @param base64
     * @return null if {@code base64} is null
     */
    public static final byte[] decodeBase64(final Base64.@NonNull Decoder decoder, final @Nullable byte[] base64) {
        return base64!=null ? decoder.decode(base64) : null;
    }

    // -- COMPRESSION

    /**
     * Compresses the given byte array, without being specific about the used algorithm.<br/>
     * However, following symmetry holds: <br/>
     * {@code x == decompress(compress(x))}
     * @param input
     * @return null if {@code input} is null
     */
    public static final byte[] compress(final @Nullable byte[] input) {
        if(input==null) {
            return null;
        }
        if(input.length==0) {
            return input;
        }
        try {
            return _Bytes_GZipCompressorSmart.compress(input);
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
    public static final byte[] decompress(final @Nullable byte[] compressed) {
        if(compressed==null) {
            return null;
        }
        if(compressed.length==0) {
            return compressed;
        }
        try {
            return _Bytes_GZipCompressorSmart.decompress(compressed);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Compresses the given byte array, using {@link Deflater} algorithm.<br/>
     * Symmetry holds: <br/>
     * {@code x == decompressZlib(compressZlib(x))}
     * @param input
     * @return null if {@code input} is null
     */
    public static final byte[] compressZlib(final @Nullable byte[] input) {
        if(input==null) {
            return null;
        }
        if(input.length==0) {
            return input;
        }
        try {
            return _Bytes_ZLibCompressor.compress(input);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Decompresses the given byte array, using {@link Inflater} algorithm.<br/>
     * Symmetry holds: <br/>
     * {@code x == decompressZlib(compressZlib(x))}
     * @param compressed
     * @return null if {@code compressed} is null
     */
    public static final byte[] decompressZlib(final @Nullable byte[] compressed) {
        if(compressed==null) {
            return null;
        }
        if(compressed.length==0) {
            return compressed;
        }
        try {
            return _Bytes_ZLibCompressor.decompress(compressed);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // -- UNARY OPERATOR COMPOSITION

    /**
     * Monadic BytesOperator that allows composition of unary byte[] operators.
     */
    public static final class BytesOperator {

        private final UnaryOperator<byte[]> operator;

        private BytesOperator(final @NonNull UnaryOperator<byte[]> operator) {
            this.operator = operator;
        }

        public byte[] apply(final byte[] input) {
            return operator.apply(input);
        }

        public BytesOperator andThen(final UnaryOperator<byte[]> andThen) {
            return new BytesOperator(s->andThen.apply(operator.apply(s)));
        }

    }

    /**
     * Returns a BytesOperator that allows composition of unary byte[] operators
     */
    public static BytesOperator operator() {
        return new BytesOperator(UnaryOperator.identity());
    }

    // -- SPECIAL COMPOSITES

    // using naming convention asX .. encode / ofX .. decode

    public static final BytesOperator asUrlBase64 = operator()
            .andThen(bytes->encodeToBase64(Base64.getUrlEncoder(), bytes));

    public static final BytesOperator ofUrlBase64 = operator()
            .andThen(bytes->decodeBase64(Base64.getUrlDecoder(), bytes));

    public static final BytesOperator asCompressedUrlBase64 = operator()
            .andThen(_Bytes::compress)
            .andThen(bytes->encodeToBase64(Base64.getUrlEncoder(), bytes));

    public static final BytesOperator ofCompressedUrlBase64 = operator()
            .andThen(bytes->decodeBase64(Base64.getUrlDecoder(), bytes))
            .andThen(_Bytes::decompress);

    public static final BytesOperator asZlibCompressedUrlBase64 = operator()
            .andThen(_Bytes::compressZlib)
            .andThen(bytes->encodeToBase64(Base64.getUrlEncoder(), bytes));

    public static final BytesOperator ofZlibCompressedUrlBase64 = operator()
            .andThen(bytes->decodeBase64(Base64.getUrlDecoder(), bytes))
            .andThen(_Bytes::decompressZlib);

    public static final BytesOperator asBase64 = operator()
            .andThen(bytes->encodeToBase64(Base64.getEncoder(), bytes));

    public static final BytesOperator ofBase64 = operator()
            .andThen(bytes->decodeBase64(Base64.getDecoder(), bytes));

    public static final BytesOperator asCompressedBase64 = operator()
            .andThen(_Bytes::compress)
            .andThen(bytes->encodeToBase64(Base64.getEncoder(), bytes));

    public static final BytesOperator ofCompressedBase64 = operator()
            .andThen(bytes->decodeBase64(Base64.getDecoder(), bytes))
            .andThen(_Bytes::decompress);

    // --

}
