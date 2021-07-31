package org.apache.isis.persistence.jpa.integration.changetracking;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.metrics.MetricsService;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.PersistenceMetricsServiceJpa")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("jpa")
//@Log4j2
public class PersistenceMetricsServiceJpa
implements
    MetricsService {

    // -- METRICS

    @Override
    public int numberEntitiesLoaded() {
        return -1; // n/a
    }

    @Override
    public int numberEntitiesDirtied() {
        return -1; // n/a
    }

}
