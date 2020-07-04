package demoapp.dom.types.temporal.jodadatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJodaDateTimeJdoEntities {

    public Optional<TemporalJodaDateTimeJdo> find(final org.joda.time.DateTime readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJodaDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJodaDateTimeJdo> all() {
        return repositoryService.allInstances(TemporalJodaDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
