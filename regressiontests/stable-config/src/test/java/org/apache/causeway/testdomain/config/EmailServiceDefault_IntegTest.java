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
package org.apache.causeway.testdomain.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.runtimeservices.email.EmailServiceDefault;
import org.apache.causeway.testdomain.conf.Configuration_headless;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                CausewayModuleCoreRuntimeServices.class,
                //JavaMailSenderImpl.class,
                EmailServiceDefault_IntegTest.MailSenderProvider.class
        }
)
@TestPropertySource({
    "classpath:/application-config-test.properties",
    CausewayPresets.UseLog4j2Test
})
class EmailServiceDefault_IntegTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"))
            .withPerMethodLifecycle(false);

    @Autowired(required = true)
    private EmailServiceDefault emailService;
    
    @Configuration
    static class MailSenderProvider {
        @Bean
        public JavaMailSender getJavaMailSender() {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("127.0.0.1");
            mailSender.setPort(3025);
            
            mailSender.setUsername("user");
            mailSender.setPassword("admin");
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");
            
            return mailSender;
        }
    }
    
    @Test
    void should_send_email_to_user_with_green_mail_extension() throws JSONException, MessagingException {

        boolean sent = emailService.send(
                Arrays.asList("tester@spring.com"),
                Collections.emptyList(),
                Collections.emptyList(),
                "Message from Java Mail Sender",
                "Hello this is a simple email message");

        assertTrue(sent);
        
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        assertEquals(1, receivedMessage.getAllRecipients().length);
        assertEquals("tester@spring.com", receivedMessage.getAllRecipients()[0].toString());
        assertEquals("test.sender@hotmail.com", receivedMessage.getFrom()[0].toString());
        assertEquals("Message from Java Mail Sender", receivedMessage.getSubject());
        
        // extract payload from multiple parts
        var bodyLines = TextUtils.readLines(GreenMailUtil.getBody(receivedMessage));
        var filteredLines = bodyLines.filter(line->line.startsWith("Hello"));
        assertEquals(Can.of("Hello this is a simple email message"), filteredLines);
    }

}
