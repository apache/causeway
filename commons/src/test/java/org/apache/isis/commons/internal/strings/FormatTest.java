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
package org.apache.isis.commons.internal.strings;


import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class FormatTest {

    // copy of code in scripts/generateConfigDocs.groovy
    public static String format(final String str, int len) {
        if(str.length() <= len) {
            return str;
        }

        final StringBuilder buf = new StringBuilder();
        String remaining = str;

        while(remaining.length() > 0) {
            int lastDot = remaining.substring(0, len).lastIndexOf('.');
            int lastDash = remaining.substring(0, len).lastIndexOf('-');
            int splitAt = lastDot > 0
                                ? lastDot + 1
                                : lastDash > 0
                                    ? lastDash + 1
                                    : len;
            if(buf.length() > 0) {
                buf.append("\n");
            }
            buf.append(remaining, 0, splitAt);
            remaining = remaining.substring(splitAt);

            if(remaining.length() <= len) {
                buf.append("\n").append(remaining);
                remaining = "";
            }
        }
        return buf.toString();
    }

    @Test
    public void split_once() {
        Assertions.assertEquals("abc.def.ghi.jkl.mno.\npqr.stu.vwx.yza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 20));
    }

    @Test
    public void split_twice() {
        Assertions.assertEquals("abc.def.ghi.jkl.\nmno.pqr.stu.vwx.\nyza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 16));
    }

    @Test
    public void split_not_on_divider() {
        Assertions.assertEquals("abc.def.ghi.\njkl.mno.pqr.\nstu.vwx.yza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 15));
    }

    @Test
    public void no_divider() {
        Assertions.assertEquals("abcdefghij\nklmnopqrst\nuvwxyza", format("abcdefghijklmnopqrstuvwxyza", 10));
    }

    @Test
    public void real_world() {
        Assertions.assertEquals("isisx.persistence.\njdo-dataxnucleus.\nclass-metadata-\nloaded-listener", format("isisx.persistence.jdo-dataxnucleus.class-metadata-loaded-listener", 20));
    }


}
