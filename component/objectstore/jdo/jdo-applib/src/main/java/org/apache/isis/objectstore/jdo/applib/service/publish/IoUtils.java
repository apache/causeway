/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.publish;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.NonRecoverableException;

class IoUtils {

    public static byte[] toUtf8ZippedBytes(String entryName, final String toZip) {
        if(toZip == null) {
            return null;
        }
        ZipOutputStream zos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            zos = new ZipOutputStream(baos);
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            
            final byte[] utf8Bytes = toZip.getBytes(Charset.forName("UTF-8"));
            zos.write(utf8Bytes);
            zos.flush();
        } catch (final IOException ex) {
            throw new FatalException(ex);
        } finally {
            closeSafely(zos);
        }
        return baos.toByteArray();
    }

    public static String fromUtf8ZippedBytes(String entryName, final byte[] toUnzip) {
        if(toUnzip == null) {
            return null;
        }
        ByteArrayInputStream bais = null;
        ZipInputStream zis = null;
        try {
            bais = new ByteArrayInputStream(toUnzip);
            zis = new ZipInputStream(bais);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if(!entry.getName().equals(entryName)) {
                    zis.closeEntry();
                    continue;
                } 
                final byte[] utf8Bytes = IoUtils.readBytes(zis);
                return new String(utf8Bytes, Charset.forName("UTF-8"));
            }
            return null;
        } catch(IOException ex) {
            throw new NonRecoverableException(ex);
        } finally {
            IoUtils.closeSafely(zis);
        }
    }

    static byte[] readBytes(final InputStream zis) throws IOException {
        final byte[] buffer = new byte[2048];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int numBytes;
        while ((numBytes = zis.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, numBytes);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    static void closeSafely(ZipInputStream zis) {
        if(zis != null) {
            try {
                zis.closeEntry();
            } catch (IOException e) {
                // ignore
            }
            try {
                zis.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    static void closeSafely(ZipOutputStream zos) {
        if(zos != null) {
            try {
                zos.closeEntry();
            } catch (IOException e) {
                // ignore
            }
            try {
                zos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }


}
