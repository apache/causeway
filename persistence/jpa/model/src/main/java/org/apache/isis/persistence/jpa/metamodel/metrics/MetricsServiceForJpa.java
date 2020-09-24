package org.apache.isis.persistence.jpa.metamodel.metrics;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.metrics.MetricsService;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJpa.MetricsServiceForJpa")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@IsisInteractionScope
@Log4j2
public class MetricsServiceForJpa implements MetricsService {

    @Override
    public int numberObjectsLoaded() {
        log.warn("MetricsService for JPA not yet implemented.");
        // TODO implemented MetricsService for JPA
        return 0;
    }

    @Override
    public int numberObjectsDirtied() {
        log.warn("MetricsService for JPA not yet implemented.");
        // TODO implemented MetricsService for JPA
        return 0;
    }

}
