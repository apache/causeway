package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;
import org.apache.isis.extensions.jpa.metamodel.specloader.validator.JpaMetaModelValidator;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.metamodel.specloader.progmodelfacets.OpenJpaProgrammingModelFacets;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class OpenJpaPersistenceMechanismInstallerTest_openAndClose {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(Utils.configurationForOpenJpaOverHsqlDb())
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .with(new OpenJpaProgrammingModelFacets())
        .with(new JpaMetaModelValidator())
        .with(new OpenJpaPersistenceMechanismInstaller())
        .withServices(repo)
        .build()
        ;


    @Test
    public void servicesBootstrapped() {
        final List<Object> services = IsisContext.getServices();
        assertThat(services.size(), is(1));
        assertThat(services.get(0), is((Object)repo));
        
        final ObjectAdapter serviceAdapter = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(repo);
        assertThat(serviceAdapter, is(not(nullValue())));
        
        assertThat(serviceAdapter.getOid(), is(equalTo((Oid)RootOidDefault.create(ObjectSpecId.of("PrimitiveValuedEntities"), "1"))));
    }
    
    @Test
    public void beginTranAndThenCommit() {
        iswf.beginTran();
        iswf.commitTran();
    }

    @Test
    public void emptyList() {
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(0));
        iswf.commitTran();
    }

    @Test
    public void persistThenRetrieve() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity = repo.newEntity();
        entity.setId(2);
        iswf.commitTran();

        // don't bounce
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();

        // do bounce
        iswf.bounceSystem();
        
        iswf.beginTran();
        list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistAdapters() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        ObjectAdapter adapter = iswf.adapterFor(entity);
        
        assertThat(adapter.isTransient(), is(true));
        assertThat(adapter.getResolveState(), is(ResolveState.TRANSIENT));
        assertThat(adapter.getOid().isTransient(), is(true));
        
        entity.setId(1);
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(1));
        
        adapter = iswf.adapterFor(list.get(0));
        assertThat(adapter.getResolveState(), is(ResolveState.GHOST));
        assertThat(adapter.isTransient(), is(false));
        assertThat(adapter.getOid().enString(), is("PRMV:1"));

        iswf.commitTran();
    }

}
