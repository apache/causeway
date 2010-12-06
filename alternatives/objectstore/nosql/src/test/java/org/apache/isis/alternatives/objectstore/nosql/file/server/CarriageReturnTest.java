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

package org.apache.isis.alternatives.objectstore.nosql.file.server;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.junit.Ignore;
import org.junit.Test;

public class CarriageReturnTest {

    // on Windows, the following test fails, because println() != print("\n")
    // uncomment to see it fail
    @Ignore
    @Test
    public void printlnDoesNotWork() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Util.ENCODING));
        pw.println("ok");
        pw.println("{data1}");
        pw.flush();

        assertThat(out.toString().toCharArray(), is(equalTo("ok\n{data1}\n".toCharArray())));
    }

    // this test passes, even on Windows.
    @Test
    public void printWithCarriageReturnWorks() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Util.ENCODING));
        pw.print("ok\n");
        pw.print("{data1}\n");
        pw.flush();

        assertThat(out.toString().toCharArray(), is(equalTo("ok\n{data1}\n".toCharArray())));
    }

}
