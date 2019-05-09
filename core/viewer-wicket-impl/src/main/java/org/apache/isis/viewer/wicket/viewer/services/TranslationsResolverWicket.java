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
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.WebAppConstants;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;

import lombok.extern.slf4j.Slf4j;


/**
 * An implementation that reads from /WEB-INF/...
 */
@Singleton @Slf4j
public class TranslationsResolverWicket implements TranslationsResolver {

    @Override
    @Programmatic
    public List<String> readLines(final String file) {
        final ServletContext servletContext = getServletContext();

        final String configLocation = servletContext.getInitParameter(WebAppConstants.CONFIG_DIR_PARAM);
        try {
            if(configLocation != null) {
                log.info( "Reading translations relative to config override location: {}", configLocation );
                return Files.readLines(newFile(configLocation, file), Charsets.UTF_8);
            } else {
                final URL url = servletContext.getResource("/WEB-INF/" + file);
                return readLines(url);
            }
        } catch (final RuntimeException | IOException ignored) {
            return null;
        }
    }

    static File newFile(final String dir, final String file) {
        final File base = new File(dir);
        final Path path = base.toPath();
        final Path resolve = path.resolve(file);
        return resolve.toFile();
    }

    protected ServletContext getServletContext() {
        return getIsisWicketApplication().getServletContext();
    }

    private static final Pattern nonEmpty = Pattern.compile("^(#:|msgid|msgstr).+$");
    private static List<String> readLines(final URL url) throws IOException {
        if(url == null) {
            return null;
        }
        final CharSource charSource = Resources.asCharSource(url, Charsets.UTF_8);
        final ImmutableList<String> strings = charSource.readLines();
        return Collections.unmodifiableList(
                _Lists.newArrayList(
                        Iterables.filter(strings, new Predicate<String>() {
                            @Override
                            public boolean apply(final String input) {
                                return input != null && nonEmpty.matcher(input).matches();
                            }
                        })));
    }

    protected IsisWicketApplication getIsisWicketApplication() {
        return IsisWicketApplication.get();
    }

}
