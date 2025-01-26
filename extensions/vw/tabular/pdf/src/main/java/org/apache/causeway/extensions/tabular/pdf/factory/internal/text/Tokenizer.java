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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public final class Tokenizer {

	private static final Token OPEN_TAG_I = new Token(TokenType.OPEN_TAG, "i");
	private static final Token OPEN_TAG_B = new Token(TokenType.OPEN_TAG, "b");
	private static final Token OPEN_TAG_OL = new Token(TokenType.OPEN_TAG, "ol");
	private static final Token OPEN_TAG_UL = new Token(TokenType.OPEN_TAG, "ul");
	private static final Token CLOSE_TAG_I = new Token(TokenType.CLOSE_TAG, "i");
	private static final Token CLOSE_TAG_B = new Token(TokenType.CLOSE_TAG, "b");
	private static final Token CLOSE_TAG_OL = new Token(TokenType.CLOSE_TAG, "ol");
	private static final Token CLOSE_TAG_UL = new Token(TokenType.CLOSE_TAG, "ul");
	private static final Token CLOSE_TAG_P = new Token(TokenType.CLOSE_TAG, "p");
	private static final Token CLOSE_TAG_LI = new Token(TokenType.CLOSE_TAG, "li");
	private static final Token POSSIBLE_WRAP_POINT = new Token(TokenType.POSSIBLE_WRAP_POINT, "");
	private static final Token WRAP_POINT_P = new Token(TokenType.WRAP_POINT, "p");
	private static final Token WRAP_POINT_LI = new Token(TokenType.WRAP_POINT, "li");
	private static final Token WRAP_POINT_BR = new Token(TokenType.WRAP_POINT, "br");

	private Tokenizer() {
	}

	private static boolean isWrapPointChar(final char ch) {
		return
				ch == ' '  ||
				ch == ','  ||
				ch == '.'  ||
				ch == '-'  ||
				ch == '@'  ||
				ch == ':'  ||
				ch == ';'  ||
				ch == '\n' ||
				ch == '\t' ||
				ch == '\r' ||
				ch == '\f' ||
				ch == '\u000B';
	}

	private static Stack<Integer> findWrapPoints(final String text) {
		Stack<Integer> result = new Stack<>();
		result.push(text.length());
		for (int i = text.length() - 2; i >= 0; i--) {
			if (isWrapPointChar(text.charAt(i))) {
				result.push(i + 1);
			}
		}
		return result;
	}

	private static Stack<Integer> findWrapPointsWithFunction(final String text, final WrappingFunction wrappingFunction) {
		final String[] split = wrappingFunction.getLines(text);
		int textIndex = text.length();
		final Stack<Integer> possibleWrapPoints = new Stack<>();
		possibleWrapPoints.push(textIndex);
		for (int i = split.length - 1; i > 0; i--) {
			final int splitLength = split[i].length();
			possibleWrapPoints.push(textIndex - splitLength);
			textIndex -= splitLength;
		}
		return possibleWrapPoints;
	}

	public static List<Token> tokenize(final String text, final WrappingFunction wrappingFunction) {
		final List<Token> tokens = new ArrayList<>();
		if (text != null) {
			final Stack<Integer> possibleWrapPoints = wrappingFunction == null
					? findWrapPoints(text)
					: findWrapPointsWithFunction(text, wrappingFunction);
			int textIndex = 0;
			final StringBuilder sb = new StringBuilder();
			// taking first wrap point
			Integer currentWrapPoint = possibleWrapPoints.pop();
			while (textIndex < text.length()) {
				if (textIndex == currentWrapPoint) {
					if (sb.length() > 0) {
						tokens.add(Token.text(sb.toString()));
						sb.delete(0, sb.length());
					}
					tokens.add(POSSIBLE_WRAP_POINT);
					currentWrapPoint = possibleWrapPoints.pop();
				}
				final char c = text.charAt(textIndex);
				switch (c) {
				case '<':
					boolean consumed = false;
					if (textIndex < text.length() - 2) {
						final char lookahead1 = text.charAt(textIndex + 1);
						final char lookahead2 = text.charAt(textIndex + 2);
						if ('i' == lookahead1 && '>' == lookahead2) {
							// <i>
							if (sb.length() > 0) {
								tokens.add(Token.text(sb.toString()));
								// clean string builder
								sb.delete(0, sb.length());
							}
							tokens.add(OPEN_TAG_I);
							textIndex += 2;
							consumed = true;
						} else if ('b' == lookahead1 && '>' == lookahead2) {
							// <b>
							if (sb.length() > 0) {
								tokens.add(Token.text(sb.toString()));
								// clean string builder
								sb.delete(0, sb.length());
							}
							tokens.add(OPEN_TAG_B);
							textIndex += 2;
							consumed = true;
						} else if ('b' == lookahead1 && 'r' == lookahead2) {
							if (textIndex < text.length() - 3) {
								// <br>
								final char lookahead3 = text.charAt(textIndex + 3);
								if (lookahead3 == '>') {
									if (sb.length() > 0) {
										tokens.add(Token.text(sb.toString()));
										// clean string builder
										sb.delete(0, sb.length());
									}
									tokens.add(WRAP_POINT_BR);
									// normal notation <br>
									textIndex += 3;
									consumed = true;
								} else if (textIndex < text.length() - 4) {
									// <br/>
									final char lookahead4 = text.charAt(textIndex + 4);
									if (lookahead3 == '/' && lookahead4 == '>') {
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											// clean string builder
											sb.delete(0, sb.length());
										}
										tokens.add(WRAP_POINT_BR);
										// normal notation <br/>
										textIndex += 4;
										consumed = true;
									} else if (textIndex < text.length() - 5) {
										final char lookahead5 = text.charAt(textIndex + 5);
										if (lookahead3 == ' ' && lookahead4 == '/' && lookahead5 == '>') {
											if (sb.length() > 0) {
												tokens.add(Token.text(sb.toString()));
												// clean string builder
												sb.delete(0, sb.length());
											}
											tokens.add(WRAP_POINT_BR);
											// in case it is notation <br />
											textIndex += 5;
											consumed = true;
										}
									}
								}
							}
						} else if ('p' == lookahead1 && '>' == lookahead2) {
							// <p>
							if (sb.length() > 0) {
								tokens.add(Token.text(sb.toString()));
								// clean string builder
								sb.delete(0, sb.length());
							}
							tokens.add(WRAP_POINT_P);
							textIndex += 2;
							consumed = true;
						} else if ('o' == lookahead1 && 'l' == lookahead2) {
							// <ol>
							if (textIndex < text.length() - 3) {
								final char lookahead3 = text.charAt(textIndex + 3);
								if (lookahead3 == '>') {
									if (sb.length() > 0) {
										tokens.add(Token.text(sb.toString()));
										// clean string builder
										sb.delete(0, sb.length());
									}
									tokens.add(OPEN_TAG_OL);
									textIndex += 3;
									consumed = true;
								}
							}
						} else if ('u' == lookahead1 && 'l' == lookahead2) {
							// <ul>
							if (textIndex < text.length() - 3) {
								final char lookahead3 = text.charAt(textIndex + 3);
								if (lookahead3 == '>') {
									if (sb.length() > 0) {
										tokens.add(Token.text(sb.toString()));
										// clean string builder
										sb.delete(0, sb.length());
									}
									tokens.add(OPEN_TAG_UL);
									textIndex += 3;
									consumed = true;
								}
							}
						} else if ('l' == lookahead1 && 'i' == lookahead2) {
							// <li>
							if (textIndex < text.length() - 3) {
								final char lookahead3 = text.charAt(textIndex + 3);
								if (lookahead3 == '>') {
									if (sb.length() > 0) {
										tokens.add(Token.text(sb.toString()));
										// clean string builder
										sb.delete(0, sb.length());
									}
									tokens.add(WRAP_POINT_LI);
									textIndex += 3;
									consumed = true;
								}
							}
						} else if ('/' == lookahead1) {
							// one character tags
							if (textIndex < text.length() - 3) {
								final char lookahead3 = text.charAt(textIndex + 3);
								if ('>' == lookahead3) {
									if ('i' == lookahead2) {
										// </i>
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_I);
										textIndex += 3;
										consumed = true;
									} else if ('b' == lookahead2) {
										// </b>
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_B);
										textIndex += 3;
										consumed = true;
									} else if ('p' == lookahead2) {
										//</p>
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_P);
										textIndex += 3;
										consumed = true;
									}
								}
							}
							if (textIndex < text.length() - 4) {
								// lists
								final char lookahead3 = text.charAt(textIndex + 3);
								final char lookahead4 = text.charAt(textIndex + 4);
								if ('l' == lookahead3) {
									if ('o' == lookahead2 && '>' == lookahead4) {
										// </ol>
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_OL);
										textIndex += 4;
										consumed = true;
									} else if ('u' == lookahead2 && '>' == lookahead4) {
										// </ul>
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_UL);
										textIndex += 4;
										consumed = true;
									}
								} else if ('l' == lookahead2 && 'i' == lookahead3) {
									// </li>
									if ('>' == lookahead4) {
										if (sb.length() > 0) {
											tokens.add(Token.text(sb.toString()));
											sb.delete(0, sb.length());
										}
										tokens.add(CLOSE_TAG_LI);
										textIndex += 4;
										consumed = true;
									}
								}
							}
						}

					}
					if (!consumed) {
						sb.append('<');
					}
					break;
				default:
					sb.append(c);
					break;
				}
				textIndex++;
			}

			if (sb.length() > 0) {
				tokens.add(Token.text(sb.toString()));
				sb.delete(0, sb.length());
			}
			tokens.add(POSSIBLE_WRAP_POINT);

			return tokens;
		} else

		{
			return Collections.emptyList();
		}
	}

}
