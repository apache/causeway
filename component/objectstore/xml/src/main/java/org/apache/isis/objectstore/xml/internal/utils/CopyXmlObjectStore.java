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

package org.apache.isis.objectstore.xml.internal.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.isis.core.commons.exceptions.IsisException;

public class CopyXmlObjectStore {
    public static void main(final String[] args) {
        final String workingDirectory = args[0];
        final String testDirectory = args[1];

        copyAllFiles(testDirectory, workingDirectory);
    }

    private static void copyAllFiles(final String testDirectory, final String workingDirectory) {
        final File from = new File(testDirectory);
        final File to = new File(workingDirectory);

        if (!to.exists()) {
            to.mkdirs();
        }
        if (to.isFile()) {
            throw new IsisException("To directory is actually a file " + to.getAbsolutePath());
        }

        final String list[] = from.list();
        for (final String element : list) {
            copyFile(new File(from, element), new File(to, element));
        }
    }

    private static void copyFile(final File from, final File to) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            bos = new BufferedOutputStream(new FileOutputStream(to));

            final byte buffer[] = new byte[2048];

            int len = 0;
            while ((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        } catch (final IOException e) {
            throw new IsisException("Error copying file " + from.getAbsolutePath() + " to " + to.getAbsolutePath(), e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (final IOException ignore) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (final IOException ignore) {
                }
            }
        }
    }

}
