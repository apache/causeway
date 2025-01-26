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
package org.apache.causeway.extensions.tabular.pdf.factory.internal.text;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.pdfbox.pdmodel.font.PDFont;

public record Token(
        TokenType type,
        String text,
        Map<PDFont, Float> cache) {

    public static Token text(final String text) {
        return new Token(TokenType.TEXT, text, new ConcurrentHashMap<>());
    }

    public Token(final TokenType type, final String text) {
        this(type, text, new ConcurrentHashMap<>());
    }

	public float getWidth(final PDFont font) throws IOException {
		return cache.computeIfAbsent(font, this::computeWidth);
	}

	private float computeWidth(final PDFont font) {
	    try {
            return font.getStringWidth(text);
        } catch (Exception e) {
            // if text contains characters that are unavailable with given font, fallback to an arbitrary placeholder character
            try {
                return font.getStringWidth("X") * text.length();
            } catch (IOException e1) {
                return 1.f;
            }
        }
	}

}
