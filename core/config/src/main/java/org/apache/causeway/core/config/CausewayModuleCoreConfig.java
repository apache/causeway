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
package org.apache.causeway.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.applib.RestfulPathProvider;
import org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessor;
import org.apache.causeway.core.config.converters.PatternsConverter;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.core.config.environment.CausewayLocaleInitializer;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.environment.CausewayTimeZoneInitializer;
import org.apache.causeway.core.config.validators.PatternOptionalStringConstraintValidator;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;

@Configuration(proxyBeanMethods = false)
@Import({

    // @Component
    CausewayConfiguration.class,
    PatternsConverter.class,
    CausewayBeanFactoryPostProcessor.class,
    CausewayLocaleInitializer.class,
    CausewayTimeZoneInitializer.class,
    PatternOptionalStringConstraintValidator.class,
    RestfulPathProvider.class,

    // @Service
    DataSourceIntrospectionService.class,
    CausewaySystemEnvironment.class,
    WebAppContextPath.class,
})
@EnableConfigurationProperties({
        CausewayConfiguration.Causeway.class,
        EclipselinkConfiguration.class,
        EclipselinkConfiguration.Weaving.class,
        EclipselinkConfiguration.DdlGeneration.class,
        EclipselinkConfiguration.Jdbc.BatchWriting.class,
        EclipselinkConfiguration.Jdbc.CacheStatements.class,
})
public class CausewayModuleCoreConfig {

    public static final String NAMESPACE = "causeway.config";

    @Bean
    public EmailConfiguration emailConfiguration(
        CausewayConfiguration conf,
        @Value("#{systemProperties['spring.mail.username']}") String senderEmailUsername,
        @Value("#{systemProperties['spring.mail.password']}") String senderEmailPassword,
        @Value("#{systemProperties['spring.mail.host']}") String senderEmailHostName,
        @Value("#{systemProperties['spring.mail.port']}") Integer senderEmailPort,
        @Value("#{systemProperties['spring.mail.javamail.properties.mail.smtp.starttls.enable']}") Boolean senderEmailTlsEnabled,
        @Value("#{systemProperties['spring.mail.properties.mail.smtp.timeout']}") Integer smtpTimeout,
        @Value("#{systemProperties['spring.mail.properties.mail.smtp.connectiontimeout']}") Integer smtpConnectionTimeout) {

        var emailConfiguration = conf.core().runtimeServices().email();

        return new EmailConfiguration(
            _Strings.emptyToNull(senderEmailUsername),
            _Strings.emptyToNull(senderEmailPassword),
            _Strings.emptyToNull(senderEmailHostName),
            senderEmailPort!=null ? senderEmailPort : 587,
            senderEmailTlsEnabled!=null ? senderEmailTlsEnabled : true,
            smtpTimeout!=null ? smtpTimeout : 2000,
            smtpConnectionTimeout!=null ? smtpConnectionTimeout : 2000,
            emailConfiguration.throwExceptionOnFail(),
            _Strings.emptyToNull(emailConfiguration.sender().address()),
            _Strings.emptyToNull(emailConfiguration.override().to()),
            _Strings.emptyToNull(emailConfiguration.override().cc()),
            _Strings.emptyToNull(emailConfiguration.override().bcc()));
    }

}
