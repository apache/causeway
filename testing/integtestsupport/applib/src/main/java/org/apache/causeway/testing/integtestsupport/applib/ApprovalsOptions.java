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
package org.apache.causeway.testing.integtestsupport.applib;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.core.Options;
import org.approvaltests.core.Scrubber;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.io.TextUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApprovalsOptions {

    public static Options xmlOptions() {
        return new Options()
                .withScrubber(ApprovalsOptions::scrub)
                .forFile()
                .withExtension(".xml");
    }

    private String scrub(final String input) {
        return TextUtils.streamLines(input)
                .map(ApprovalsOptions::scrubLine)
                .filter(line->!_Strings.nullToEmpty(line).isBlank()) // ignore blank lines, just in case
                .collect(Collectors.joining("\n")); // UNIX line ending convention
    }

    /**
     * As the XML spec states, order of attributes has no semantic significance and hence is not
     * guaranteed to be always the same, like in
     * <pre>
     * {@code <mml:param xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="mml:scalarParam" id="style">}
     * </pre>
     * So we have to scrub those for consistent comparison.
     * @param line
     * @return canonical form of the line
     */
    private String scrubLine(final String line) {

        var magicPrefix = "<mml:param ";
        var magicSuffix = ">";
        int p = line.indexOf(magicPrefix);
        if(p<0) {
            return line;
        }
        p += magicPrefix.length(); // pointer at end of "...<mml:param "
        int q = line.lastIndexOf(magicSuffix); // pointer at start of "... >"

        var chunks = _Lists.<String>newArrayList();
        chunks.add(line.substring(0, p-1)); // first chunk "...<mml:param"

        // ordered attributes
        var attrs = _Maps.<String, _Strings.KeyValuePair>newTreeMap();
        _Strings.splitThenStream(line.substring(p, q), " ")
                .map(attrLiteral->
                        _Strings.parseKeyValuePair(attrLiteral, '=')
                                .orElseGet(()->_Strings.pair(attrLiteral, null))
                )
                .forEach(attr->attrs.put(attr.getKey(), attr));

        // collect all chunks
        attrs.values()
                .forEach(attr->chunks.add(
                        attr.getValue()!=null
                                ? " " + attr.getKey() + "=" + attr.getValue()
                                : " " + attr.getKey()));
        chunks.add(magicSuffix);

        // reassemble line
        return chunks.stream().collect(Collectors.joining());
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    public Options gqlOptions() {
        return new Options().withScrubber(new Scrubber() {
            @SneakyThrows
            @Override
            public String scrub(final String s) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s));
            }
        }).forFile().withExtension(".gql");
    }

}
