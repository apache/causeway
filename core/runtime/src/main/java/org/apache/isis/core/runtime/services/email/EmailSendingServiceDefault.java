package org.apache.isis.core.runtime.services.email;

import java.util.Properties;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.email.EmailSendingService;
import org.apache.isis.applib.services.email.events.UserCreationEvent;

/**
 *
 */
@DomainService
public class EmailSendingServiceDefault implements EmailSendingService {

    private static final String EMAIL = "apache.isis.test@gmail.com";
    private static final String PASSWD = "ApacheIsis987^";

    @Override
    public void send(UserCreationEvent userCreationEvent) {

        try {
            Email email = new HtmlEmail();
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(587);
            email.setAuthenticator(new DefaultAuthenticator(EMAIL, PASSWD));
            email.setStartTLSEnabled(true);
            Properties properties = email.getMailSession().getProperties();
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.debug", "true");
            properties.put("mail.smtps.port", "587");
            properties.put("mail.smtps.socketFactory.port", "587");
            properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtps.socketFactory.fallback", "false");
            properties.put("mail.smtp.starttls.enable", "true");

            email.setFrom("apache.isis.test@gmail.com");

            email.setSubject("SignUp verification");
            email.setMsg("This is a test mail ... :-)\n\nEmail: " +
                         userCreationEvent.getEmail() + "\n\nPasswd: " + userCreationEvent.getPassword());
            email.addTo("martin.grigorov@gmail.com");
            email.send();

        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}
