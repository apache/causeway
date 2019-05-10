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

package org.apache.isis.core.runtime.authentication.standard;

import java.util.Random;

public class RandomCodeGenerator10Chars implements RandomCodeGenerator {

    private static final int NUMBER_CHARACTERS = 10;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    @Override
    public String generateRandomCode() {
        final StringBuilder buf = new StringBuilder(NUMBER_CHARACTERS);
	Random rand = new Random();
        for (int i = 0; i < NUMBER_CHARACTERS; i++) {
            final int pos = (int) ((rand.nextDouble() * CHARACTERS.length()));
            buf.append(CHARACTERS.charAt(pos));
        }
        return buf.toString();
    }

}
