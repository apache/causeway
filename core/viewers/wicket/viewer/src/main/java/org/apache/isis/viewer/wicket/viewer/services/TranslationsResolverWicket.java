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

import javax.servlet.ServletContext;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketApplication;

import lombok.val;
import lombok.extern.log4j.Log4j2;


/**
 * An implementation that reads from /WEB-INF/...
 */
@Service @Log4j2
public class TranslationsResolverWicket implements TranslationsResolver {

    /**
     * Servlet context parameter name used to specify the location for translations.
     */
    public static final String CONFIG_DIR_PARAM = "isis.config.dir";

    @Override
    public List<String> readLines(final String fileName) {
        final ServletContext servletContext = getServletContext();

        final String configLocation = servletContext.getInitParameter(CONFIG_DIR_PARAM);
        try {
            if(configLocation != null) {
                log.info( "Reading translations relative to config override location: {}", configLocation );
                
                return Files.readAllLines(newFile(configLocation, fileName), StandardCharsets.UTF_8);
            } else {
                final URL url = servletContext.getResource("/WEB-INF/" + fileName);
                return readLines(url);
            }
        } catch (final RuntimeException | IOException ignored) {
            return null;
        }
    }

    static Path newFile(final String dir, final String fileName) {
        final File base = new File(dir);
        final Path path = base.toPath();
        return path.resolve(fileName);
    }

    protected ServletContext getServletContext() {
        return getIsisWicketApplication().getServletContext();
    }

    private static final Pattern nonEmpty = Pattern.compile("^(#:|msgid|msgstr).+$");
    
    private static List<String> readLines(final URL url) throws IOException {
        if(url == null) {
            return null;
        }
        
        val lines = _Strings.readAllLines(url.openStream(), StandardCharsets.UTF_8);
        val acceptedLines = lines.stream()
        .filter(input->input != null && nonEmpty.matcher(input).matches())
        .collect(Collectors.toList());
        
        return Collections.unmodifiableList(acceptedLines);
    }

    protected IsisWicketApplication getIsisWicketApplication() {
        return IsisWicketApplication.get();
    }

}
