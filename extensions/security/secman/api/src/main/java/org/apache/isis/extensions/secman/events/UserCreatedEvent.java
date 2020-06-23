package org.apache.isis.extensions.secman.events;


import lombok.Getter;
import lombok.Value;

@Value(staticConstructor="of")
public class UserCreatedEvent {
    public static enum EventType {
         localUser,
         delegateUser
    }
    @Getter UserCreatedEvent.EventType eventType;
    @Getter String userName;

}
