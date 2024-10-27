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
package org.apache.causeway.commons.internal.html;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.Builder;
import lombok.Singular;
/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@lombok.Value @Builder
public class _BootstrapBadge {

    final String caption;
    final String faIcon;
    final String href;
    final String tooltip;
    final String nestedCaption;

    @Singular
    final List<String> cssClasses;

    public String toHtml() {
        return _Strings.isNotEmpty(getHref())
                ? link()
                : noLink();
    }

    // -- HELPER

    /*
     * // no href
     * <span class="badge bg-light placeholder-literal-xxx">[none]</span>
     */
    public String noLink() {

        var sb = new StringBuilder();
        sb
        .append("<span ")
        .append("class=\"")
        .append(classesLiteral("badge", "bg-light"))
        .append("\"");

        // optional tooltip
        if(_Strings.isNotEmpty(getTooltip())) {
            sb.append(" data-bs-container=\"body\" "
                + "data-bs-toggle=\"tooltip\" "
                + "title=\""+getTooltip()+"\"");
        }

        sb.append(">"); // end span open tag

        // optional fa-icon
        if(_Strings.isNotEmpty(getFaIcon())) {
            sb
            .append("<i class=\"")
            .append(getFaIcon())
            .append("\"></i>");
        }

        sb
        .append(getCaption())
        .append("</span>");

        return sb.toString();
    }

    /*
     * // with href
     * <a class="btn btn-sm btn-light placeholder-literal-xxx" href="https://www.example.org" target="_blank">
     *      has more ... <span class="badge text-sm text-bg-secondary">4</span>
     * </a>
     */
    public String link() {

        var sb = new StringBuilder();
        sb
        .append("<a ")
        .append("class=\"")
        .append(classesLiteral("btn", "btn-sm", "bg-light"))
        .append("\"")
        .append(" href=\"")
        .append(getHref())
        .append("\"")
        .append(" target=\"")
        .append("_blank")
        .append("\"");

        sb.append(">"); // end a open tag

        sb
        .append(getCaption());

        // optional nested badge
        if(_Strings.isNotEmpty(getNestedCaption())) {
            sb.append(String.format(" <span class=\"badge text-sm text-bg-secondary\">%s</span>", getNestedCaption()));
        }

        sb
        .append("</a>");

        return sb.toString();
    }

    private String classesLiteral(final String ... primaryClasses) {
        return Stream.concat(_NullSafe.stream(primaryClasses), _NullSafe.stream(cssClasses))
                .collect(Collectors.joining(" "));
    }

}