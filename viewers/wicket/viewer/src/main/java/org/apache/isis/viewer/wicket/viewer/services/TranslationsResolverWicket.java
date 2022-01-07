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
package org.apache.isis.viewer.wicket.viewer.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;


/**
 * An implementation that reads from /WEB-INF/...
 * TODO this Service is also required by the VaadinViewer, could be moved to a shared module
 * TODO ... or (ideally) let Spring handle translations
 */
@Service
@Named("isis.viewer.wicket.TranslationsResolverWicket")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Wicket")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class TranslationsResolverWicket implements TranslationsResolver {

    private final ServletContext servletContext;
    private final IsisConfiguration isisConfiguration;

    @Override
    public List<String> readLines(final String fileName) {

        final String configLocation =
                isisConfiguration.getCore().getRuntimeServices().getTranslation().getResourceLocation();

        try {
            if(configLocation != null) {
                log.info( "Reading translations relative to config override location: {}", configLocation );

                return Files.readAllLines(newFile(configLocation, fileName), StandardCharsets.UTF_8);
            } else {
                final URL url = servletContext.getResource("/WEB-INF/" + fileName);
                return readLines(url);
            }
        } catch (final RuntimeException | IOException ignored) {
            return Collections.emptyList();
        }
    }

    static Path newFile(final String dir, final String fileName) {
        final File base = new File(dir);
        final Path path = base.toPath();
        return path.resolve(fileName);
    }

    private static final Pattern nonEmpty = Pattern.compile("^(#:|msgid|msgstr).+$");

    private static List<String> readLines(final URL url) throws IOException {
        if(url == null) {
            return Collections.emptyList();
        }

        val acceptedLines = _Text.readLinesFromUrl(url, StandardCharsets.UTF_8)
        .stream()
        .filter(input->input != null && nonEmpty.matcher(input).matches())
        .collect(Collectors.toList());

        return Collections.unmodifiableList(acceptedLines);
    }

}
