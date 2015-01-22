package org.apache.isis.applib.services.email;

import java.io.Serializable;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * TODO ISIS-987 Javadoc
 */
public interface EmailService extends Serializable {

    @PostConstruct
    @Programmatic
    public void init() ;

    @Programmatic
    boolean send(List<String> to, List<String> cc, List<String> bcc, String subject, String body, DataSource... attachments);

    /**
     * Whether this service has been configured and thus available for use.
     */
    @Programmatic
    boolean isConfigured();

}
