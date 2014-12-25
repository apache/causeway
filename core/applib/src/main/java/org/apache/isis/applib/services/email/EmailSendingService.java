package org.apache.isis.applib.services.email;

import java.io.Serializable;
import org.apache.isis.applib.services.email.events.UserCreationEvent;

/**
 *
 */
public interface EmailSendingService extends Serializable {

    void send(UserCreationEvent userCreationEvent);
}
