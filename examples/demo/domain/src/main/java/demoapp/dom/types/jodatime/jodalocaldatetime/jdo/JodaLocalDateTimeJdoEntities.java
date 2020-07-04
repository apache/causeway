package demoapp.dom.types.jodatime.jodalocaldatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JodaLocalDateTimeJdoEntities {

    public Optional<JodaLocalDateTimeJdo> find(final org.joda.time.LocalDateTime readOnlyProperty) {
        return repositoryService.firstMatch(JodaLocalDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JodaLocalDateTimeJdo> all() {
        return repositoryService.allInstances(JodaLocalDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
