package demoapp.dom.extensions.secman.apptenancy.entities;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TenantedJdoEntities {

    public List<TenantedJdo> all() {
        return repositoryService.allInstances(TenantedJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
