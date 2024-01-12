package org.apache.causeway.core.runtimeservices.email;

import java.util.Arrays;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import org.apache.causeway.core.security.authentication.AuthenticationRequest;
import org.apache.causeway.core.security.authentication.standard.AuthenticatorAbstract;

import org.apache.causeway.core.security.authorization.Authorizor;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(
        classes = {EmailServiceDefault_IntegTest.TestApp.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Profile("test")
class EmailServiceDefault_IntegTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,

            AuthenticatorDummy.class,
            AuthorizorDummy.class,
    })
    public static class TestApp {
    }

    // a copy of AuthenticatorBypass from bypass module.
    @Service
    public static class AuthenticatorDummy extends AuthenticatorAbstract {

        @Override
        public boolean isValid(final AuthenticationRequest request) {
            return true;
        }

        @Override
        public boolean canAuthenticate(final Class<? extends AuthenticationRequest> authenticationRequestClass) {
            return true;
        }
    }

    // a copy of AuthorizorBypass from bypass module.
    @Service
    public static class AuthorizorDummy implements Authorizor {

        @Override
        public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
            return true;
        }

        @Override
        public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
            return true;
        }

    }



    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"))
            .withPerMethodLifecycle(false);

    @Autowired
    private EmailServiceDefault emailService;

    @Test
    void should_send_email_to_user_with_green_mail_extension() throws JSONException, MessagingException {

        boolean send = emailService.send(Arrays.asList("tester@spring.com"), Collections.emptyList(), Collections.emptyList(), "Hello subject", "Hello word");

        org.assertj.core.api.Assertions.assertThat(send).isTrue();

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        Assertions.assertEquals(1, receivedMessage.getAllRecipients().length);

        Assertions.assertEquals("tester@spring.com", receivedMessage.getAllRecipients()[0].toString());
        Assertions.assertEquals("test.sender@hotmail.com", receivedMessage.getFrom()[0].toString());
        Assertions.assertEquals("Message from Java Mail Sender", receivedMessage.getSubject());
        Assertions.assertEquals("Hello this is a simple email message", GreenMailUtil.getBody(receivedMessage));
    }

}