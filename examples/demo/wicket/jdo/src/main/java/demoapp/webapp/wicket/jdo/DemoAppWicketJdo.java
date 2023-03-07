/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package demoapp.webapp.wicket.jdo;

import java.util.Map;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.apache.causeway.commons.internal.os._OsUtil;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.config.util.SpringProfileUtil;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.pdfjs.wkt.ui.CausewayModuleExtPdfjsWicketUi;
import org.apache.causeway.extensions.sse.wicket.CausewayModuleExtSseWicket;
import org.apache.causeway.valuetypes.asciidoc.metamodel.CausewayModuleValAsciidocMetaModel;
import org.apache.causeway.valuetypes.asciidoc.persistence.jdo.dn.CausewayModuleValAsciidocPersistenceJdoDn;
import org.apache.causeway.valuetypes.asciidoc.ui.wkt.CausewayModuleValAsciidocUiWkt;
import org.apache.causeway.valuetypes.markdown.metamodel.CausewayModuleValMarkdownMetaModel;
import org.apache.causeway.valuetypes.markdown.persistence.jdo.dn.CausewayModuleValMarkdownPersistenceJdoDn;
import org.apache.causeway.valuetypes.markdown.ui.wkt.CausewayModuleValMarkdownUiWkt;
import org.apache.causeway.valuetypes.vega.metamodel.CausewayModuleValVegaMetaModel;
import org.apache.causeway.valuetypes.vega.persistence.jdo.dn.CausewayModuleValVegaPersistenceJdoDn;
import org.apache.causeway.valuetypes.vega.ui.wkt.CausewayModuleValVegaUiWkt;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import demoapp.web.DemoAppManifestJdo;
import demoapp.webapp.wicket.common.ui.DemoAppWicketCommon;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    // App-Manifest (Configuration)
    DemoAppManifestJdo.class,

    // Metamodel
    CausewayModuleValAsciidocMetaModel.class,
    CausewayModuleValMarkdownMetaModel.class,
    CausewayModuleValVegaMetaModel.class,

    // UI (Wicket Viewer)
    CausewayModuleViewerWicketViewer.class,
    CausewayModuleExtSseWicket.class,
    CausewayModuleValAsciidocUiWkt.class,
    CausewayModuleValMarkdownUiWkt.class,
    CausewayModuleValVegaUiWkt.class,
    CausewayModuleExtPdfjsWicketUi.class,

    // Custom Demo UI (Wicket Viewer)
    DemoAppWicketCommon.class,

    // Persistence (JDO/DN5)
    CausewayModuleValAsciidocPersistenceJdoDn.class,
    CausewayModuleValMarkdownPersistenceJdoDn.class,
    CausewayModuleValVegaPersistenceJdoDn.class,

    //XrayEnable.class // for debugging only
    //WicketViewerXrayEnable.class // for debugging only

    DemoAppWicketJdo.WorkaroundJpaEntityManagerFactoryRequired.class
})
//@Log4j2
public class DemoAppWicketJdo extends SpringBootServletInitializer {

    /**
     *
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {

        // activates when sys-env THERE_CAN_BE_ONLY_ONE=true
        _OsUtil.thereCanBeOnlyOne();

    	CausewayPresets.prototyping();
        //CausewayPresets.logging(WebRequestCycleForCauseway.class, "debug");

        SpringProfileUtil.removeActiveProfile("demo-jpa"); // just in case
        SpringProfileUtil.addActiveProfile("demo-jdo");

        CausewayModuleExtCommandLogApplib.honorSystemEnvironment();


        SpringApplication.run(new Class[] { DemoAppWicketJdo.class }, args);

    }

    /**
     * maybe this workaround is not need, if we remove the right JPA artifacts from the class-path;
     * however, don't know which one that would be
     */
    static class WorkaroundJpaEntityManagerFactoryRequired {

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            return new EntityManagerFactory() {
                @Override public <T> T unwrap(final Class<T> cls) { return null; }
                @Override public boolean isOpen() { return false; }
                @Override public Map<String, Object> getProperties() { return null; }
                @Override public PersistenceUnitUtil getPersistenceUnitUtil() { return null; }
                @Override public Metamodel getMetamodel() { return null; }
                @Override public CriteriaBuilder getCriteriaBuilder() { return null; }
                @Override public Cache getCache() { return null; }
                @Override public EntityManager createEntityManager(
                        final SynchronizationType synchronizationType, final Map map) { return null; }
                @Override public EntityManager createEntityManager(
                        final SynchronizationType synchronizationType) { return null; }
                @Override public EntityManager createEntityManager(final Map map) { return null; }
                @Override public EntityManager createEntityManager() { return null; }
                @Override public void close() {}
                @Override public void addNamedQuery(final String name, final Query query) {}
                @Override public <T> void addNamedEntityGraph(
                        final String graphName, final EntityGraph<T> entityGraph) {}
            };
        }
    }

}
