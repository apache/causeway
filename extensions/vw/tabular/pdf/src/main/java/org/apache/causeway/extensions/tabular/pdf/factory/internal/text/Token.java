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
import java.util.Objects;

import org.apache.pdfbox.pdmodel.font.PDFont;

// Token itself is thread safe, so you can reuse shared instances;
// however, subclasses may have additional methods which are not thread safe.
public class Token {

	private final TokenType type;

	private final String data;

	public Token(final TokenType type, final String data) {
		this.type = type;
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public TokenType getType() {
		return type;
	}

	public float getWidth(final PDFont font) throws IOException {
		return font.getStringWidth(getData());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + type + "/" + data + "]";
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return getType() == token.getType() &&
				Objects.equals(getData(), token.getData());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getType(), getData());
	}

	// Returns non-thread safe instance optimized for renderable text
	public static Token text(final TokenType type, final String data) {
		return new TextToken(type, data);
	}
}

// Non-thread safe subclass with caching to optimize tokens containing renderable text
class TextToken extends Token {
	private PDFont cachedWidthFont;
	private float cachedWidth;

	TextToken(final TokenType type, final String data) {
		super(type, data);
	}

	@Override
	public float getWidth(final PDFont font) throws IOException {
		if (font == cachedWidthFont) {
			return cachedWidth;
		}
		cachedWidth = super.getWidth(font);
		// must come after super call, in case it throws
		cachedWidthFont = font;
		return cachedWidth;
	}
}