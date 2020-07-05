package demoapp.dom.types.jodatime.jodalocaldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JodaLocalDateJdoEntities {

    public Optional<JodaLocalDateJdo> find(final org.joda.time.LocalDate readOnlyProperty) {
        return repositoryService.firstMatch(JodaLocalDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JodaLocalDateJdo> all() {
        return repositoryService.allInstances(JodaLocalDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
