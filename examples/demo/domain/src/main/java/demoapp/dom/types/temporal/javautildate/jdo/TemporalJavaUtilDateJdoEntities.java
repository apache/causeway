package demoapp.dom.types.temporal.javautildate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaUtilDateJdoEntities {

    public Optional<TemporalJavaUtilDateJdo> find(final java.util.Date readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaUtilDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaUtilDateJdo> all() {
        return repositoryService.allInstances(TemporalJavaUtilDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
