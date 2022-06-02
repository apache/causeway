package org.isisaddons.module.audit.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.RepositoryService;

/**
 * Provides supporting functionality for querying {@link AuditEntry audit entry} entities.
 *
 * <p>
 * This supporting service with no UI and no side-effects, and there are no other implementations of the service,
 * thus has been annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This means that there is no
 * need to explicitly register it as a service (eg in <tt>isis.properties</tt>).
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class AuditingServiceRepository {

    @Programmatic
    public AuditEntry findFirstByTarget(final Bookmark target) {
        final String targetStr = target.toString();
        return findFirstByTarget(targetStr);
    }

    @Programmatic
    public AuditEntry findFirstByTarget(final String targetStr) {
        final List<AuditEntry> matches = repositoryService.allMatches(
                new QueryDefault<>(AuditEntry.class,
                        "findFirstByTarget",
                        "targetStr", targetStr
                ));
        return matches.isEmpty() ? null : matches.get(0);
    }

    @Programmatic
    public List<AuditEntry> findRecentByTarget(final Bookmark target) {
        final String targetStr = target.toString();
        return findRecentByTarget(targetStr);
    }

    @Programmatic
    public List<AuditEntry> findRecentByTarget(final String targetStr) {
        return repositoryService.allMatches(
                new QueryDefault<>(AuditEntry.class,
                        "findRecentByTarget",
                        "targetStr", targetStr
                ));
    }

    @Programmatic
    public List<AuditEntry> findRecentByTargetAndPropertyId(
            final Bookmark target,
            final String propertyId) {
        final String targetStr = target.toString();
        return repositoryService.allMatches(
                new QueryDefault<>(AuditEntry.class,
                        "findRecentByTargetAndPropertyId",
                        "targetStr", targetStr,
                        "propertyId", propertyId
                    ));
    }

    @Programmatic
    public List<AuditEntry> findByTransactionId(final UUID transactionId) {
        return repositoryService.allMatches(
                new QueryDefault<>(AuditEntry.class,
                        "findByTransactionId", 
                        "transactionId", transactionId));
    }

    @Programmatic
    public List<AuditEntry> findByTargetAndFromAndTo(
            final Bookmark target, 
            final LocalDate from, 
            final LocalDate to) {
        final String targetStr = target.toString();
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<AuditEntry> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTargetAndTimestampBetween", 
                        "targetStr", targetStr,
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTargetAndTimestampAfter", 
                        "targetStr", targetStr,
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTargetAndTimestampBefore", 
                        "targetStr", targetStr,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTarget", 
                        "targetStr", targetStr);
            }
        }
        return repositoryService.allMatches(query);
    }

    @Programmatic
    public List<AuditEntry> findByFromAndTo(
            final LocalDate from, 
            final LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<AuditEntry> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTimestampBetween", 
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTimestampAfter", 
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<>(AuditEntry.class,
                        "findByTimestampBefore", 
                        "to", toTs);
            } else {
                query = new QueryDefault<>(AuditEntry.class,
                        "find");
            }
        }
        return repositoryService.allMatches(query);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ?new java.sql.Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

    @Inject
    RepositoryService repositoryService;

}
