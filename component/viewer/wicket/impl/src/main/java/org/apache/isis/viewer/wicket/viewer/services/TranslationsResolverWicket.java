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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.servlet.ServletContext;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;


/**
 * An implementation that reads from /WEB-INF/...
 */
@DomainService
public class TranslationsResolverWicket implements TranslationsResolver {

    public static Logger LOG = LoggerFactory.getLogger(TranslationsResolverWicket.class);

    @Override
    @Programmatic
    public List<String> readLines(final String file) {
        try {
            final ServletContext servletContext = getIsisWicketApplication().getServletContext();
            final URL url = servletContext.getResource("/WEB-INF/" + file);
            return readLines(url);
        } catch (final RuntimeException | IOException ignored) {
            return null;
        }
    }

    private static List<String> readLines(final URL url) throws IOException {
        if(url == null) {
            return null;
        }
        final CharSource charSource = Resources.asCharSource(url, Charsets.UTF_8);
        return charSource.readLines();
    }

    protected IsisWicketApplication getIsisWicketApplication() {
        return IsisWicketApplication.get();
    }

}
