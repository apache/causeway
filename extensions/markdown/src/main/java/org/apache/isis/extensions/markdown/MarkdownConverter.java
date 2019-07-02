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
package org.apache.isis.extensions.markdown;

import java.util.Arrays;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;

public final class MarkdownConverter {

	/**
	 * For syntax highlighting to work, the client/browser needs to load specific 
	 * java-script. To enable load it with your {@code application.js}
	 * <p>
	 * eg. like the one bundled with {@code src/main/resources/prism1.14.js}.
	 * <pre>
	 * function includeJs(jsFilePath) {
	 *     var js = document.createElement("script");
	 *     js.type = "text/javascript";
	 *     js.src = jsFilePath;
	 *     document.body.appendChild(js);
	 * }
	 * 
	 * includeJs("/scripts/prism1.14.js");
	 * </pre>
	 *  
	 * @param markdown - markdown formated input to be converted to HTML
	 */
	public static String mdToHtml(String markdown) {
		if(markdownSupport==null) {
			markdownSupport = new MarkdownSupport();
		}
		return markdownSupport.toHtml(markdown);
	}

	// -- HELPER

	private static MarkdownSupport markdownSupport;

	private static class MarkdownSupport {
		private Parser parser;
		private HtmlRenderer renderer;

		public MarkdownSupport() {
			MutableDataSet options = new MutableDataSet();

			// uncomment to set optional extensions
			options.set(Parser.EXTENSIONS, Arrays.asList(
					TablesExtension.create(), 
					StrikethroughExtension.create()));

			// uncomment to convert soft-breaks to hard breaks
			//options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

			parser = Parser.builder(options).build();
			renderer = HtmlRenderer.builder(options).build();
		}

		public String toHtml(String markdown) {
			return renderer.render(parser.parse(markdown));
		}
	}

}
