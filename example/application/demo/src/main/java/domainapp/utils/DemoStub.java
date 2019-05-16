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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.isis.applib.value.Markup;

import lombok.val;


public abstract class DemoStub {

	public String title() {
		return getClass().getSimpleName();
	}

	public abstract void initDefaults();
	
	public Markup getDescription() {
		return new Markup(descriptionAsHtml());
	}

	protected final static Map<String, String> constants = createConstants();
	private static Map<String, String> createConstants() {
		val map = new HashMap<String, String>();
		map.put("SOURCES_ISIS", "https://github.com/apache/isis/blob/master/core/applib/src/main/java");
		map.put("SOURCES_DEMO", "https://github.com/apache/isis/tree/v2/example/application/demo/src/main/java");
		map.put("ISSUES_DEMO", "https://issues.apache.org/jira/"); 
		return map;
	}

	protected String link(String name, String href) {
		return String.format("<a target=\"%s\" href=\"%s\">%s</a>", "blank", href, name);
	}

	protected String p(String content) {
		return String.format("<p>%s</p>", content);
	}

	protected String var(String name) {
		return String.format("${%s}", name);
	}

	protected String descriptionAsHtml() {
		val adocResourceName = getClass().getSimpleName()+".adoc";
		val adocResource = this.getClass().getResourceAsStream(adocResourceName);
		if(adocResource==null) {
			return String.format("Markdown resource '%s' not found.", adocResourceName);
		}
		try {
			return DemoUtils.asciidocToHtml(read(adocResource));
		} catch (IOException e) {
			return String.format("Failed to read from adoc resource '%s': ", e.getMessage());
		}
	}

	/**
	 * Read the given {@code input} into a String, while also pre-processing placeholders.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String read(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
			return buffer.lines()
					.map(this::resolveVariables)
					.collect(Collectors.joining("\n"));
		}
	}

	/**
	 * For the given {@code input} replaces '${var-name}' with the variable's value.
	 * @param input
	 * @return
	 */
	private String resolveVariables(String input) {
		val ref = new AtomicReference<String>(input);
		constants.forEach((k, v)->{
			ref.set(ref.get().replace(var(k), v));
		});
		return ref.get();
	}

}
