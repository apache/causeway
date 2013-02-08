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
package org.apache.isis.viewer.restfulobjects.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class NOTUSED_InputStreamUtil {

    private NOTUSED_InputStreamUtil() {
    }

    @SuppressWarnings("unused")
    private static List<String> getArgs(final InputStream body) {
        // will be sorted by arg
        final Map<String, String> args = new TreeMap<String, String>();
        if (body == null) {
            return listOfValues(args);
        }
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(body);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                final String[] keyValuePairs = readLine.split("&");
                for (final String keyValuePair : keyValuePairs) {
                    final String[] keyThenValue = keyValuePair.split("=");
                    args.put(keyThenValue[0], keyThenValue[1]);
                }
                readLine = bufferedReader.readLine();
            }
            return listOfValues(args);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ArrayList<String> listOfValues(final Map<String, String> args) {
        // returns the values in the order of the keys
        return new ArrayList<String>(args.values());
    }

}
