package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object RESTFUL_DOMAIN_TYPES : Response() {
    override val url = "http://localhost:8080/restful/domain-types"
    override val str = """
{
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/domain-types",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/type-list\""
  } ],
  "values" : [ {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.bookmarks.BookmarkServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.adapter.mgr.AdapterManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.dto.DtoMappingHelper",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.sudo.SudoService.Spi",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.iactn.InteractionContext",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Integer",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.jaxb.JaxbService.Simple",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.config.IsisConfigurationDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.queryresultscache.QueryResultsCache.Control",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.urlencoding.UrlEncodingService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/javax.jdo.listener.LoadLifecycleListener",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.routing.RoutingServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Long",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.jdosupport.Persistable_datanucleusVersionLong",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.ConfigurationServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.grid.GridServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.deplcat.DeploymentCategoryProviderDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.menubars.MenuBarsLoaderServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.acceptheader.AcceptHeaderService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.memento.MementoServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.registry.ServiceRegistryDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.io.Serializable",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appmanifest.AppManifestProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/javax.jdo.listener.StoreLifecycleListener",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.background.BackgroundService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.specloader.SpecificationLoader",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.publish.PublishingServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.authorization.AuthorizationManagerAbstract",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.menu.MenuBarsService.Type",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmarkui.BookmarkUiService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.objectstore.jdo.datanucleus.service.support.IsisJdoSupportImpl",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.GuiceBeanProviderWicket",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.jaxb.JaxbService.IsisSchemas",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.grid.GridSystemServiceAbstract",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.metamodel.MetaModelService4",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.metamodel.MetaModelService3",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.metamodel.MetaModelService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.background.BackgroundServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.guice.GuiceBeanProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.factory.FactoryServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.grid.GridLoaderService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.components.Component",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.clock.ClockService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.swagger.SwaggerService.Visibility",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.jdosupport.IsisJdoSupport",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.HsqlDbManagerMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.exceprecog.ExceptionRecognizer",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.system.transaction.TransactionalClosure",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.AbstractSubscriber",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.schema.services.jaxb.JaxbServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.i18n.LocaleProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.appfeat.ApplicationMemberType",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Object",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.classdiscovery.ClassDiscoveryService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.fixtures.InstallableFixture",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.FixtureScriptsDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.xactn.TransactionServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.ixn.InteractionDtoServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.SortedSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.command.CommandDtoServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.menu.MenuBarsService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.sudo.SudoService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.grid.GridSystemService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.registry.ServiceRegistry",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.title.TitleService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.AbstractCollection",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.scratchpad.Scratchpad",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.SwaggerServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Blob",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.audit.AuditerServiceLogging",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.swagger.SwaggerService.Format",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.linking.DeepLinkService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.jdosupport.Persistable_datanucleusVersionTimestamp",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.Collection",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.sql.Timestamp",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.TranslationServicePoMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.fixturescripts.FixtureScripts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.audit.AuditerService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.LayoutService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.classdiscovery.ClassDiscoveryServiceUsingReflections",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.xactn.TransactionService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.urlencoding.UrlEncodingServiceUsingBaseEncoding",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.jaxb.JaxbService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.ConfigurationProperty",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.dto.Dto_downloadXml",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.sessmgmt.SessionManagementService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.Object_openRestApi",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Clob",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.changes.ChangedObjectsServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.fixturescripts.FixtureScript",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.AbstractService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.config.ConfigurationServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.sudo.SudoServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.DeepLinkServiceWicket",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.message.MessageServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.i18n.TranslationsResolver",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.Object_downloadLayoutXml",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.HashSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.jdosupport.Persistable_datanucleusIdLong",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.command.CommandContext",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.i18n.po.TranslationServicePo",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Boolean",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.tablecol.TableColumnOrderService.Default",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/homepage.HomePageService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.repository.RepositoryServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternalNoop",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.annotation.Bulk.InteractionContext",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.publish.PublisherService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.grid.bootstrap3.GridSystemServiceBS3",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.objectstore.jdo.datanucleus.service.eventbus.EventBusServiceJdo",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.email.EmailService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/simple.SimpleObject",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Object",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.background.BackgroundService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.auditing.AuditingServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.routing.RoutingService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.title.TitleServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.repository.RepositoryService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/domainapp.application.services.homepage.HomePageViewModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.i18n.TranslationService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.Object_clearHints",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.queryresultscache.QueryResultsCache",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.facetapi.MetaModelRefiner",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.Collections.UnmodifiableList",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.authentication.AuthenticationManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.actinvoc.ActionInvocationContext",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.Set",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.ViewModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.swagger.SwaggerService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.user.UserServiceDefault.SudoServiceSpi",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.menubars.bootstrap3.MenuBarsServiceBS3",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.userreg.EmailNotificationService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.memento.MementoService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.xmlsnapshot.XmlSnapshotServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecificationProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkHolder_lookup",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.jdosupport.Persistable_downloadJdoMetadata",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.AbstractViewModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceAbstract",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureFactory",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.homepage.HomePageProviderServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.authorization.AuthorizationManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.wrapper.WrapperFactory",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.objectstore.jdo.applib.service.exceprecog.ExceptionRecognizerCompositeForJdoObjectStore",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.layout.LayoutServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/domainapp.application.fixture.scenarios.DomainAppDemo",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.TreeSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.ArrayList",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.RandomAccess",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.xactn.TransactionService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.authsess.AuthenticationSessionProviderDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.Collections.UnmodifiableRandomAccessList",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.xactn.TransactionService3",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceForRestfulObjects",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.String",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.userprof.UserProfileService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.user.UserService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.LinkedHashSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.LayoutService.Style",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.dto.Dto_downloadXsd",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.iactn.Interaction",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.BookmarkUiServiceWicket",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.config.ConfigurationService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.command.CommandServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.Collections.UnmodifiableCollection",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.swagger.SwaggerServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.components.ApplicationScopedComponent",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.userreg.EmailNotificationServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Comparable",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.objectstore.jdo.datanucleus.service.support.TimestampService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceOrgApacheIsisV1",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.ServicesInjectorAware",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.net.URL",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.authentication.AuthenticationSessionProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.metrics.MetricsServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.AbstractList",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.eventbus.EventBusService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.grid.GridService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.LocaleProviderWicket",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.WithTransactionScope",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.persistence.FixturesInstalledFlag",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotServiceAbstract",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkHolder_object",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.factory.FactoryService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.menu.MenuBarsLoaderService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.grid.GridLoaderServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.tablecol.TableColumnOrderService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Cloneable",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/void",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.registry.ServiceRegistry2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.TranslationsResolverWicket",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.NavigableSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.List",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceXRoDomainType",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.LayoutService2",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.userprof.UserProfileServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.message.MessageService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerBase",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.config.IsisConfiguration",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.util.AbstractSet",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/domainapp.application.fixture.DomainAppFixtureScriptsSpecificationProvider",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.metamodel.MetaModelServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/java.lang.Iterable",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/javax.jdo.listener.InstanceLifecycleListener",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.hint.HintStore",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.sessmgmt.SessionManagementServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.homepage.HomePageProviderService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.system.session.IsisSessionFactory",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.persistsession.PersistenceSessionServiceInternalDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.fixturescripts.ExecutionParametersService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.publish.PublisherServiceLogging",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.AbstractContainedObject",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.Object_rebuildMetamodel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.wrapper.WrapperFactoryDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.metrics.MetricsService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.services.HintStoreUsingWicketSession",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.eventbus.EventBusServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.user.UserServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.command.spi.CommandService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.annotation.SemanticsOf",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/boolean",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.metamodel.MetaModelService",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.runtime.services.email.EmailServiceDefault",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.message.MessageServiceNoop",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  } ],
  "extensions" : { }
}
        """
}
