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

package domainapp.utils;

import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;

public class DemoUtils {

    public static String emphasize(String string) {
        return new StringBuilder()
                .append("\n====================================================\n")
                .append(string)
                .append("\n====================================================\n")
                .toString();
    }

    public static String asciidocToHtml(String adoc) {
        if(adoc==null) {
            asciidoctor = Asciidoctor.Factory.create();
            options = defaultOptions();
        }
        return asciidoctor.convert(adoc, options);
    }

    // -- HELPER

    private static Asciidoctor asciidoctor;
    private static Map<String, Object> options;
    
    private static Map<String, Object> defaultOptions() {
        return new HashMap<>();
    }
	
/////////////////////////////////////////////////////////////////////	
// MARKDOWN SUPPORT REMOVED
//
//   public static String markdownToHtml(String markdown) {
//        if(markdownSupport==null) {
//            markdownSupport = new MarkdownSupport();
//        }
//        return markdownSupport.toHtml(markdown);
//    }
//	
//	// -- HELPER
//	
//	private static MarkdownSupport markdownSupport;
//	
//	private static class MarkdownSupport {
//		private Parser parser;
//		private HtmlRenderer renderer;
//		
//		public MarkdownSupport() {
//			MutableDataSet options = new MutableDataSet();
//
//			// uncomment to set optional extensions
//			options.set(Parser.EXTENSIONS, Arrays.asList(
//					TablesExtension.create(), 
//					StrikethroughExtension.create()));
//
//			// uncomment to convert soft-breaks to hard breaks
//			//options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
//
//			parser = Parser.builder(options).build();
//			renderer = HtmlRenderer.builder(options).build();
//		}
//		
//		public String toHtml(String markdown) {
//			return renderer.render(parser.parse(markdown));
//		}
//	}
	

}
