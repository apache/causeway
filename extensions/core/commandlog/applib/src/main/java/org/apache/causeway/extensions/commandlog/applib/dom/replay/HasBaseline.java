package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

public interface HasBaseline {

    abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    java.sql.Timestamp getBaseline();

    HasBaseline withBaseline(Timestamp baseline);


    static Timestamp addSeconds(Timestamp ts, int secondsToAdd) {
        Instant instant = ts.toInstant();
        return addSeconds(instant, secondsToAdd);
    }

    static Timestamp addSeconds(Instant instant, int secondsToAdd) {
        return Timestamp.from(instant.plusSeconds(secondsToAdd));
    }

    static Timestamp addMillis(Instant instant, int millisToAdd) {
        return Timestamp.from(instant.plusMillis(millisToAdd));
    }

}
