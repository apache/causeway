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
package org.apache.causeway.core.config.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.CausewayConfiguration.ViewerProfiles;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ViewerProfileUtil {

    /**
     * Resolves the default viewer profiles for a given namespace prefix on namespace configuration.
     * Uses longest-prefix matching. Falls back to {@code default} key.
     */
    public Set<String> profilesForNamespace(
            final ViewerProfiles viewerProfiles,
            final String namespacePrefix) {
        String matchedKey = null;
        int longestPrefixLen = -1;

        for (String ns : viewerProfiles.namespace().keySet()) {
            if (namespacePrefix.startsWith(ns)) {
                if (ns.length() > longestPrefixLen) {
                    longestPrefixLen = ns.length();
                    matchedKey = ns;
                }
            }
        }

        String rawProfiles = matchedKey != null
                ? viewerProfiles.namespace().get(matchedKey)
                : viewerProfiles.namespace().getOrDefault("default", "web-ui,api");

        return parseProfiles(rawProfiles);
    }

    /**
     * Returns all profile IDs that a specific viewer supports.
     */
    public Set<String> profilesForViewer(
            final ViewerProfiles viewerProfiles,
            final String viewerId) {
        String rawProfiles = viewerProfiles.map().getOrDefault(viewerId, "");
        return parseProfiles(rawProfiles);
    }

    /**
     * Namespace configuration must not contain profiles,
     * that are not also mapped by viewers.
     */
    public void validate(
            final Map<String, String> map,
            final Map<String, String> namespace) {
        Set<String> viewerSupportedProfiles = map.values().stream()
            .map(ViewerProfileUtil::parseProfiles)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        Set<String> namespaceReferencedProfiles = namespace.values().stream()
            .map(ViewerProfileUtil::parseProfiles)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        Set<String> profilesNotMappedByAnyViewer =
            _Sets.minus(namespaceReferencedProfiles, viewerSupportedProfiles);
        Assert.isTrue(profilesNotMappedByAnyViewer.isEmpty(), ()->
            "causeway.viewer-profiles.namespace.. has profiles %s not mapped by any viewer"
                .formatted(profilesNotMappedByAnyViewer));
    }

    // -- HELPER

    private Set<String> parseProfiles(final String raw) {
        return StringUtils.hasLength(raw)
            ? Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())
            : Set.of();
    }

}
