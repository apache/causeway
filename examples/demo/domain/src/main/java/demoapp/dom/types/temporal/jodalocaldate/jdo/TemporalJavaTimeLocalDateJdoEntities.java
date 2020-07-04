package demoapp.dom.types.temporal.jodalocaldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJodaLocalDateJdoEntities {

    public Optional<TemporalJodaLocalDateJdo> find(final org.joda.time.LocalDate readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJodaLocalDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJodaLocalDateJdo> all() {
        return repositoryService.allInstances(TemporalJodaLocalDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
