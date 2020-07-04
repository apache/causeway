package demoapp.dom.types.jodatime.jodadatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JodaDateTimeJdoEntities {

    public Optional<JodaDateTimeJdo> find(final org.joda.time.DateTime readOnlyProperty) {
        return repositoryService.firstMatch(JodaDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JodaDateTimeJdo> all() {
        return repositoryService.allInstances(JodaDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
