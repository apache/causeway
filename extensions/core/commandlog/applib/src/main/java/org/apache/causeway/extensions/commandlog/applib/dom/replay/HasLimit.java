package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

public interface HasLimit {

    abstract class ActionDomainEvent<T>
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T> { }


    int getLimit();

    HasLimit withLimit(int limit);

}
