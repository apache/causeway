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
package org.apache.isis.applib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

/**
 * Zip utility for generating compressed output.
 * <p>
 * Typical use:
 * <pre>
 * val zipWriter = ZipWriter.newInstance();
 * 
 * for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
 *      val namespaceUri = entry.getKey();
 *      val schemaText = entry.getValue();
 *      zipWriter.nextEntry(zipEntryNameFor(namespaceUri), writer->{
 *     	    writer.write(schemaText);
 *      });
 * }
 *  
 * return BlobClobFactory.blobZip(fileName, zipWriter.toBytes());
 * </pre>
 * 
 * @since 2.0 {@index}
 * @apiNote Implementation is <em>not</em> thread safe.
 *
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class ZipWriter {

    @Value(staticConstructor = "of")
    public static class ZipOutputStreamWrapper {
        private final @NonNull ZipOutputStream zipOutputStream;
        
        public void writeCharactersUtf8(final @Nullable CharSequence chars) throws IOException {
            writeCharacters(chars, StandardCharsets.UTF_8);
        }
        
        public void writeCharacters(
                final @Nullable CharSequence chars, 
                final @NonNull Charset charset) throws IOException {
            if(chars==null 
                    || chars.length()==0) {
                return; // no-op
            }
            zipOutputStream.write(_Strings.toBytes(chars.toString(), charset));
        }
        
        public void writeBytes(final @Nullable byte bytes[]) throws IOException {
            if(_NullSafe.isEmpty(bytes)) {
                return; // no-op
            }
            zipOutputStream.write(bytes);
        }

        public void writeBytes(final @Nullable byte bytes[], int off, int len) throws IOException {
            if(_NullSafe.isEmpty(bytes)) {
                return; // no-op
            }
            zipOutputStream.write(bytes, off, len);
        }
        
    }
    
    @FunctionalInterface
    public interface OnZipEntry {
        public void accept(ZipOutputStreamWrapper writer) throws IOException;
    }

    public static ZipWriter newInstance() {
        return ofFailureMessage("Unable to create zip");
    }

    public static ZipWriter ofFailureMessage(String failureMessage) {
        val baos = new ByteArrayOutputStream();
        val zos = new ZipOutputStream(baos);
        val writer = new ZipOutputStreamWrapper(zos);
        return new ZipWriter(baos, zos, writer, failureMessage);
    }

    private final ByteArrayOutputStream baos;
    private final ZipOutputStream zos;
    private final ZipOutputStreamWrapper writer;
    private final String failureMessage;
    private byte[] zippedBytes;

    /**
     * Adds a new zipEntry with given {@code zipEntryName}, and provides the
     * {@link OutputStreamWriter} via {@link OnZipEntry} for the consumer to 
     * write the actual (uncompressed) zip-entry content. 
     * @param zipEntryName
     * @param onZipEntry
     */
    public void nextEntry(String zipEntryName, OnZipEntry onZipEntry) {
        if(zippedBytes!=null) {
            throw new IllegalStateException("Cannot create a new ZipEntry an a closed ZipWriter");
        }
        try {
            zos.putNextEntry(new ZipEntry(zipEntryName));
            onZipEntry.accept(writer);
            zos.closeEntry();
        } catch (final IOException e) {
            throw _Exceptions.unrecoverable(failureMessage, e);
        }
    }

    /**
     * Terminal operation, closes the writer. 
     * Calling this operation multiple times, will return the same array instance object. 
     * @return the byte array created by the underlying ZipOutputStream
     */
    public byte[] toBytes() {
        if(zippedBytes==null) {
            try {
                zos.close();
            } catch (IOException e) {
                throw _Exceptions.unrecoverable(failureMessage, e);
            }
            zippedBytes = baos.toByteArray();
        }
        return zippedBytes;
    }


}
