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
package org.apache.isis.core.config.converters;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;

@Component
@Named("isis.config.PatternsConverter")
@ConfigurationPropertiesBinding
public class PatternsConverter implements Converter<String, Map<Pattern, String>> {

    @Override
    public Map<Pattern, String> convert(String source) {
        return toPatternMap(source);
    }

    /**
     * The pattern matches definitions like:
     * <ul>
     * <li>methodNameRegex:value</li>
     * </ul>
     *
     * <p>
     *     Used for associating cssClass and cssClassFa (font awesome icon) values to method pattern names.
     * </p>
     */
    private static final Pattern PATTERN_FOR_COLON_SEPARATED_PAIR = Pattern.compile("(?<methodRegex>[^:]+):(?<value>.+)");

    private static Map<Pattern, String> toPatternMap(String cssClassPatterns) {
        final Map<Pattern,String> valueByPattern = _Maps.newLinkedHashMap();
        if(cssClassPatterns != null) {
            final StringTokenizer regexToCssClasses = new StringTokenizer(cssClassPatterns, ",");
            final Map<String,String> valueByRegex = _Maps.newLinkedHashMap();
            while (regexToCssClasses.hasMoreTokens()) {
                String regexToCssClass = regexToCssClasses.nextToken().trim();
                if (_Strings.isNullOrEmpty(regexToCssClass)) {
                    continue;
                }
                final Matcher matcher = PATTERN_FOR_COLON_SEPARATED_PAIR.matcher(regexToCssClass);
                if(matcher.matches()) {
                    valueByRegex.put(matcher.group("methodRegex"), matcher.group("value"));
                }
            }
            for (Map.Entry<String, String> entry : valueByRegex.entrySet()) {
                final String regex = entry.getKey();
                final String cssClass = entry.getValue();
                valueByPattern.put(Pattern.compile(regex), cssClass);
            }
        }
        return valueByPattern;
    }

}
