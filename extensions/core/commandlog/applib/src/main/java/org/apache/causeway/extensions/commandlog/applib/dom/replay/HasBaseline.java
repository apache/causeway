package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.sql.Timestamp;

import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

public interface HasBaseline {

    abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    java.sql.Timestamp getBaseline();

    HasBaseline withBaseline(Timestamp baseline);

    @UtilityClass
    class Util {

        static Timestamp addSeconds(Timestamp ts, int secondsToAdd) {
            return Timestamp.from(ts.toInstant().plusSeconds(secondsToAdd));
        }
    }
}
