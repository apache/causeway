package org.apache.isis.applib.services.audit;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.UUID;

@DomainService(nature = NatureOfService.DOMAIN)
public class AuditerServiceLogging implements AuditerService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditerServiceLogging.class);

    @PostConstruct
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return LOG.isDebugEnabled();
    }

    @Programmatic
    @Override
    public void audit(
            final UUID interactionId, int sequence,
            final String targetClassName, final Bookmark target,
            final String memberId, final String propertyName,
            final String preValue, final String postValue,
            final String user, final Timestamp timestamp) {

        String auditMessage =
                interactionId + "," + sequence + ": " +
                target.toString() + " by " + user + ", " + propertyName + ": " + preValue + " -> " + postValue;
        LOG.debug(auditMessage);
    }

}
