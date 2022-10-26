package org.apache.causeway.client.kroviz.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response

class DOMAIN_TYPES: Response() {
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
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionHiddenVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveChars",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertySnapshotVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigIntegerVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationUserManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayCalendarEventEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperCharacters",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigDecimalVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalDateTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveBooleanVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyProjectingChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DomainObjectEntityChangePublishingEnabledEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaDateTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperShorts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyMaxLengthVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigIntegerEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionLayoutPromptStyleVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyMustSatisfyVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkdownEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.StatefulViewModelUsingJaxb.Child",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.EmbeddedTypeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkups",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlDates",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.conf.ConfigurationProperty",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutMultiLineVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.MixinVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayCalendarEventVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigIntegers",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.AssociatedActionDemoTask",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationUser",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveLongs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayBlobs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperByteVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.SecManVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigDecimals",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperFloatEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveLongEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.ParameterNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalDates",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayPasswords",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CollectionDomainEventVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.FacetAttrNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionRestrictToVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeZonedDateTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperShortVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionTypeOfChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutHiddenChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.FileNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperIntegerEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeZonedDateTimeEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyCommandPublishingEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveBooleanEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperFloats",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.Tab",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.RoleMemento",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationTypeAction",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperLongEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationTenancyManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayClobEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveShortVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalDateTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyExecutionPublishingEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveIntVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayAsciiDocs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilDateEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayLocalResourcePaths",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaMathBigDecimalEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.EventBusServiceDemoVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.EventLogEntry",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkdowns",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionDomainEventVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionTypeOfVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDateVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationTypeCollection",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.StatefulViewModelJaxbRefsEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.UserMemento",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetDateTimeEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CollectionDomainEventChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveBooleans",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.XmlSnapshotChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperBooleanVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveIntEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeZonedDateTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyOptionalityVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyHiddenVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayAsciiDocVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDates",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutTypicalLengthVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveShorts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationTypeMember",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.FacetGroupNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.ActionNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.conf.ConfigurationViewmodel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DomainObjectEntityChangePublishingEnabledMetaAnnotOverriddenEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlDateEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.AssociatedAction",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDateTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalDateVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayBlobEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.NumberConstantEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.TypeNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperIntegers",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutLabelPositionVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilUuidVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.testing.fixtures.FixtureResult",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalTimeEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilDates",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperBytes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.Tooltip",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaNetUrlVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaAwtBufferedImages",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyEditingVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.AppFeat",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionExecutionPublishingEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.UserPermissionViewModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayBlobVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangVoids",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveLongVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayPasswordEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionAssociateWithVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyHiddenChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkdownVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetDateTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperByteEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveFloatEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperFloatVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveDoubleVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlTimestampVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.XmlSnapshotParentVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveBytes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.FacetNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayPasswordVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationTenancy",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionCommandPublishingEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyDomainEventVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.MessageServiceDemoVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationRole",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutRenderDayVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperIntegerVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutCssClassVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaNetUrls",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveCharEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.EventBusServiceDemoVm.UiButtonEvent",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.DomainObjectList",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayCalendarEvents",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.TenantedEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveByteEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.Homepage",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDateEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlTimestamps",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationPermission",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlDateVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperBooleanEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayClobVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutHiddenVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.CollectionNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.XmlSnapshotPeerChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaAwtBufferedImageEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JaxbRefEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionLayoutPositionVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyProjectingVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperBooleans",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDateTimeEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveInts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangStrings",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetTimeEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.XmlSnapshotPeerVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayClobs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveByteVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperCharacterVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilDateVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ErrorReportingServiceDemoVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeOffsetDateTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutDescribedAsVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DependentArgs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperDoubleVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.InteractionDtoVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperShortEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperLongs",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.StatefulVmUsingJaxb",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveShortEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkupVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.AsyncDemoTask",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.AsyncAction",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilUuidEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangStringVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationType",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DependentArgsDemoItem",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveFloats",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaNetUrlEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayMarkupEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaSqlTimestampEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperDoubleEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionAssociateWithChildVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperCharacterEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperDoubles",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveCharVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.security.LoginRedirect",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveFloatVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaLocalTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaTimeLocalDateTimes",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationRoleManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.FibonacciNumberVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveDoubles",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DomainObjectEntityChangePublishingVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.commandLog.Command",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CustomUiVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayAsciiDocEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperLongVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DomainObjectEntityChangePublishingDisabledEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.ext.secman.ApplicationOrphanedPermissionManager",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JodaDateTimeVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.applib.PropertyNode",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayLocalResourcePathEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaUtilUuids",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaAwtBufferedImageVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.ActionSemanticsVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationTypeProperty",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.WrapperFactoryEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutNamedVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationFeatureViewModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.DomainObjectEntityChangePublishingEnabledMetaAnnotatedEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PrimitiveDoubleEntity",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.CausewayLocalResourcePathVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyProjectingChildJpa",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/causeway.feat.ApplicationNamespace",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyLayoutRepaintingVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyFileAcceptVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-type",
    "href" : "http://localhost:8080/restful/domain-types/demo.PropertyRegexPatternVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  } ],
  "extensions" : { }
}        
    """
}
