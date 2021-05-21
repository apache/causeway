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
package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object RESTFUL_DOMAIN_TYPES : Response() {
    override val url = "http://localhost:8080/restful/domain-types"
    override val str = """
{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/domain-types",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/type-list\""
        }
    ],
    "values": [
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Float",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.LocalResourcePath",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.EventsDemoMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.utils.DemoStub",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.TranslationServicePoMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.TupleDemoMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.user.HasUsername_open",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.ZonedDateTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.SwaggerServiceMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusVersionTimestamp",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.app.user.ApplicationUser_permissions",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Integer",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.DependentArgsActionDemo_useDisable",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Primitives",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_addUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.dto.Dto_downloadXsd",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/tabMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.math.BigDecimal",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_viewing",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Character",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Long",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationRole",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_allow",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.math.BigInteger",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.tenancy.ApplicationTenancy_removeUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_veto",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.user.ApplicationUser_addRole",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.DependentArgsActionDemo_useChoices",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.TooltipMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.ConfigurationProperty",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/byte",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/double",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.metamodel.Object_objectIdentifier",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.Set",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.DependentArgsActionDemo_useAutoComplete",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Homepage",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationClassMember",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Markup",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Blob",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkHolder_lookup",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.viewer.wicket.viewer.mixins.Object_clearHints",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.DependentArgsActionDemo_useDefault",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.role.ApplicationRole",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AssociatedActionDemoTask",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.menu.MenuBarsService.Type",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Double",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.LocalDateTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AsyncAction",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.user.ApplicationUserStatus",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.events.EventsDemo.UiButtonEvent",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/long",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.jaxb.JaxbService.IsisSchemas",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Errors",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.String",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisExtFixtures.FixtureScripts",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationPermission",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.types.blob.BlobDemo_downloadLogo",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.metamodel.Object_downloadMetaModelXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AssociatedAction",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.metamodel.Object_objectType",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.layout.LayoutService.Style",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_addUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts.NonPersistedObjectsStrategy",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.swagger.SwaggerService.Visibility",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.NumberConstant",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.ConfigurationMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.TupleDemo",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.dto.Dto_downloadXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.eventLogWriter",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.DependentArgs",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Jee",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationClassCollection",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.joda.time.LocalDateTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.layout.Object_downloadLayoutXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationPermissionMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/char",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusIdLong",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.FeaturedTypesMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.user.ApplicationUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.EventLogEntry",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.user.AccountType",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.UserPermissionViewModel",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.SortedSet",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.Date",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Tab",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.TreeDemoMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/float",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_datanucleusVersionLong",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.Parity",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.internal.ioc.spring.BeanAdapterSpring",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Short",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationUserMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.LocalTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.bookmark.BookmarkHolder_object",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Byte",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Blob",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.OffsetTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationClassAction",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.services.swagger.SwaggerService.Format",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.testing.fixtures.applib.fixturespec.FixtureScriptsSpecification",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.core.commons.collections.Can",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/void",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Tooltip",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.sql.Timestamp",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.Collection",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Text",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_updateRole",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationClassProperty",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_changing",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationTenancyMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.List",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Tree",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.OffsetDateTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.layout.Object_rebuildMetamodel",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationTenancy",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationClass",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.graph.Vertex",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.time.LocalDate",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.graph.SimpleEdge",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Temporal",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.util.SortedMap",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.valuetypes.sse.applib.value.ListeningMarkup",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Clob",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.Events",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_delete",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.AbstractService",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.value.Password",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.MeService",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.joda.time.LocalTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.types.tuple.ComplexNumber",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.persistence.jdo.datanucleus5.jdosupport.mixins.Persistable_downloadJdoMetadata",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Boolean",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.ErrorMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.JeeMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AsyncDemoTask",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationFeatureViewModels",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isisExtFixture.FixtureResult",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.graph.tree.LazyTreeNode",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.app.feature.ApplicationFeatureViewModel",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.tree.FileNode.Type",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.DependentArgsDemoItem",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.joda.time.DateTime",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AssociatedActionMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationPackage",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/int",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.sql.Date",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.joda.time.LocalDate",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.AsyncActionMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demo.DependentArgsActionMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.mixins.layout.Object_openRestApi",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.annotation.SemanticsOf",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/demoapp.dom.actions.depargs.DependentArgsActionDemo_useHide",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.app.user.ApplicationUser_filterPermissions",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.applib.graph.tree.TreeNode",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/boolean",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/short",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.app.feature.ApplicationPermission_feature",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/isissecurity.ApplicationRoleMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Object",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/java.lang.Class",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/domain-type",
            "href": "http://localhost:8080/restful/domain-types/org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_removeUser",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        }
    ],
    "extensions": {}
}

"""
}
