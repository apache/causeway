package demoapp.dom.types.temporal.jodalocaldatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJodaLocalDateTimeJdoEntities {

    public Optional<TemporalJodaLocalDateTimeJdo> find(final org.joda.time.LocalDateTime readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJodaLocalDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJodaLocalDateTimeJdo> all() {
        return repositoryService.allInstances(TemporalJodaLocalDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
