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
package org.apache.isis.runtimes.dflt.objectstores.nosql.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.isis.runtimes.dflt.objectstores.nosql.file.server.Util;


public class ChecksummingPerfomance {

    public static void main(String[] args) throws Exception {

        CRC32 inputChecksum = new CRC32();
        CheckedInputStream in = new CheckedInputStream(new FileInputStream("test.data"), inputChecksum);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Util.ENCODING));

        for (int i = 0; i < 30; i++) {
            long time = System.currentTimeMillis();
            StringBuffer buf = null;
            for (int j = 0; j < 1000; j++) {
                buf = readFile(i);
            }
            time = System.currentTimeMillis() - time;
            System.out.print(time);
            System.out.print("   ");

            time = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                readChecksummedFile(i);
            }
            time = System.currentTimeMillis() - time;
            System.out.print(time);
            System.out.print("   ");

            time = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                testArray(buf);
            }
            time = System.currentTimeMillis() - time;
            System.out.print(time);
            System.out.print("   ");

            byte[] data = null;
            time = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                data = extractArray(buf);
            }
            time = System.currentTimeMillis() - time;
            System.out.print(time);
            System.out.print("   ");

            time = System.currentTimeMillis();
            for (int j = 0; j < 1000; j++) {
                checksumArray(data);
            }
            time = System.currentTimeMillis() - time;
            System.out.println(time);
        }

    }

    private static void testArray(StringBuffer buf) {
        byte[] data = buf.toString().getBytes();

        CRC32 inputChecksum = new CRC32();
        inputChecksum.reset();
        inputChecksum.update(data);

        // System.out.println(inputChecksum.getValue());
    }


    private static byte[] extractArray(StringBuffer buf) {
        byte[] data = buf.toString().getBytes();
        return data;
    }
    
    private static void checksumArray(byte[] data) {
        CRC32 inputChecksum = new CRC32();
        inputChecksum.reset();
        inputChecksum.update(data);

        // System.out.println(inputChecksum.getValue());
    }

    private static StringBuffer readChecksummedFile(int i) throws Exception {
        CRC32 inputChecksum = new CRC32();
        CheckedInputStream in = new CheckedInputStream(new FileInputStream("test" + i % 3 + ".data"), inputChecksum);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Util.ENCODING));

        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }

        // System.out.println(inputChecksum.getValue());

        reader.close();

        return buf;
    }

    private static StringBuffer readFile(int i) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("test" + i % 3 + ".data"), Util.ENCODING));

        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }

        // System.out.println(inputChecksum.getValue());

        reader.close();

        return buf;
    }
}
