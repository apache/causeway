package org.apache.isis.applib.services.email;

import java.io.Serializable;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Provides the ability to send emails to one or more recipients.
 *
 * <p>
 * The core framework also provides a default implementation <tt>EmailServiceDefault</tt> that sends email as an
 * HTML message, using an external SMTP provider.  See the Isis website for further details.
 * </p>
 */
public interface EmailService extends Serializable {

    /**
     * Always called by the framework, and allows the implementation to read configuration properties and initialize itself
     */
    @PostConstruct
    @Programmatic
    public void init() ;

    /**
     * Main API to send email and optional attachments.
     */
    @Programmatic
    boolean send(List<String> to, List<String> cc, List<String> bcc, String subject, String body, DataSource... attachments);

    /**
     * Whether this service has been configured and thus available for use.
     */
    @Programmatic
    boolean isConfigured();

}
