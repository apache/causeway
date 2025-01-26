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
package org.apache.causeway.extensions.tabular.pdf.factory.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFont;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.Token.TokenType;

final class PipelineLayer {

	private final StringBuilder text = new StringBuilder();
	private String lastTextToken = "";
	private List<Token> tokens = new ArrayList<>();
	private String trimmedLastTextToken = "";
	private float width;
	private float widthLastToken;
	private float widthTrimmedLastToken;
	private float widthCurrentText;

	public boolean isEmpty() {
		return tokens.isEmpty();
	}

	public void push(final Token token) {
		tokens.add(token);
	}

	public void push(final PDFont font, final float fontSize, final Token token) throws IOException {
		if (token.type().equals(TokenType.PADDING)) {
			width += Float.parseFloat(token.text());
		}
		if (token.type().equals(TokenType.BULLET)) {
			// just appending one space because our bullet width will be wide as one character of current font
			text.append(token.text());
			width += (token.getWidth(font) / 1000f * fontSize);
		}

		if (token.type().equals(TokenType.ORDERING)) {
			// just appending one space because our bullet width will be wide as one character of current font
			text.append(token.text());
			width += (token.getWidth(font) / 1000f * fontSize);
		}

		if (token.type().equals(TokenType.TEXT)) {
			text.append(lastTextToken);
			width += widthLastToken;
			lastTextToken = token.text();
			trimmedLastTextToken = rtrim(lastTextToken);
			widthLastToken = token.getWidth(font) / 1000f * fontSize;

			if (trimmedLastTextToken.length() == lastTextToken.length()) {
				widthTrimmedLastToken = widthLastToken;
			} else {
				widthTrimmedLastToken = (font.getStringWidth(trimmedLastTextToken) / 1000f * fontSize);
			}

			widthCurrentText = text.length() == 0 ? 0 :
					(font.getStringWidth(text.toString()) / 1000f * fontSize);
		}

		push(token);
	}

	public void push(final PipelineLayer pipeline) {
		text.append(lastTextToken);
		width += widthLastToken;
		text.append(pipeline.text);
		if (pipeline.text.length() > 0) {
			width += pipeline.widthCurrentText;
		}
		lastTextToken = pipeline.lastTextToken;
		trimmedLastTextToken = pipeline.trimmedLastTextToken;
		widthLastToken = pipeline.widthLastToken;
		widthTrimmedLastToken = pipeline.widthTrimmedLastToken;
		tokens.addAll(pipeline.tokens);

		pipeline.reset();
	}

	public void reset() {
		text.delete(0, text.length());
		width = 0.0f;
		lastTextToken = "";
		trimmedLastTextToken = "";
		widthLastToken = 0.0f;
		widthTrimmedLastToken = 0.0f;
		tokens.clear();
	}

	public String trimmedText() {
		return text.toString() + trimmedLastTextToken;
	}

	public float width() {
		return width + widthLastToken;
	}

	public float trimmedWidth() {
		return width + widthTrimmedLastToken;
	}

	public List<Token> tokens() {
		return new ArrayList<>(tokens);
	}

	@Override
	public String toString() {
		return text.toString() + "(" + lastTextToken + ") [width: " + width() + ", trimmed: " + trimmedWidth() + "]";
	}

	// -- HELPER

	private static String rtrim(final String s) {
	    int len = s.length();
	    while ((len > 0) && (s.charAt(len - 1) <= ' ')) {
	        len--;
	    }
	    if (len == s.length()) {
	        return s;
	    }
	    if (len == 0) {
	        return "";
	    }
	    return s.substring(0, len);
	}
}
