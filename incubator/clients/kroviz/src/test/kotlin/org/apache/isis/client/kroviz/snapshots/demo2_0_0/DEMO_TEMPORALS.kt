package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object DEMO_TEMPORALS: Response(){
    override val url = "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/temporals/invoke"
    override val str = """{
  "links": [
    {
      "rel": "self",
      "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "title": "Temporal Demo"
    },
    {
      "rel": "describedby",
      "href": "http://localhost:8080/restful/domain-types/demo.Temporal",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
    },
    {
      "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
      "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/object-layout",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
    },
    {
      "rel": "urn:org.apache.isis.restfulobjects:rels/object-icon",
      "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/image",
      "method": "GET",
      "type": "image/png"
    },
    {
      "rel": "urn:org.restfulobjects:rels/update",
      "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=",
      "method": "PUT",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "arguments": {}
    }
  ],
  "extensions": {
    "oid": "demo.Temporal:AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=",
    "isService": false,
    "isPersistent": true
  },
  "title": "Temporal Demo",
  "domainType": "demo.Temporal",
  "instanceId": "AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=",
  "members": {
    "description": {
      "id": "description",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"description\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/description",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "<div class=\"paragraph\">\n<p>(since 1.16)</p>\n</div>\n<div class=\"paragraph\">\n<p>The framework supports following temporal values from the Java Time API\n(and joda.org):</p>\n</div>\n<div class=\"olist arabic\">\n<ol class=\"arabic\">\n<li>\n<p>Date only</p>\n<div class=\"ulist\">\n<ul>\n<li>\n<p>java.sql.<strong>Date</strong></p>\n</li>\n<li>\n<p>java.time.<strong>LocalDate</strong>; (since 2.0.0-M1)</p>\n</li>\n<li>\n<p>org.joda.time.<strong>LocalDate</strong></p>\n</li>\n</ul>\n</div>\n</li>\n</ol>\n</div>\n<div class=\"olist arabic\">\n<ol class=\"arabic\" start=\"2\">\n<li>\n<p>Date and Time</p>\n<div class=\"ulist\">\n<ul>\n<li>\n<p>java.util.<strong>Date</strong></p>\n</li>\n<li>\n<p>java.sql.<strong>Timestamp</strong></p>\n</li>\n<li>\n<p>java.time.<strong>LocalDateTime</strong> (since 2.0.0-M1)</p>\n</li>\n<li>\n<p>java.time.<strong>OffsetDateTime</strong> (since 2.0.0-M1)</p>\n</li>\n<li>\n<p>org.joda.time.<strong>DateTime</strong></p>\n</li>\n<li>\n<p>org.joda.time.<strong>LocalDateTime</strong></p>\n</li>\n</ul>\n</div>\n</li>\n</ol>\n</div>\n<div class=\"paragraph\">\n<p>If used with JAXB View Models, you need to specify specific XmlAdapters\nas provided by <code>org.apache.isis.applib.util.JaxbAdapters.*</code>:</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre class=\"highlight\"><code class=\"language-java\" data-lang=\"java\">@XmlRootElement(name = \"Demo\")\n@XmlType\n@XmlAccessorType(XmlAccessType.FIELD)\n@DomainObject(nature=Nature.VIEW_MODEL)\npublic class TemporalDemo extends DemoStub {\n\n    // -- DATE ONLY (LOCAL TIME)\n\n    @XmlElement @XmlJavaTypeAdapter(SqlDateAdapter.class)\n    @Getter @Setter private java.sql.Date javaSqlDate;\n\n    @XmlElement @XmlJavaTypeAdapter(LocalDateAdapter.class)\n    @Getter @Setter private LocalDate javaLocalDate;\n\n    // -- DATE AND TIME (LOCAL TIME)\n\n    @XmlElement @XmlJavaTypeAdapter(DateAdapter.class)\n    @Getter @Setter private Date javaUtilDate;\n\n    @XmlElement @XmlJavaTypeAdapter(SqlTimestampAdapter.class)\n    @Getter @Setter private java.sql.Timestamp javaSqlTimestamp;\n\n    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)\n    @Getter @Setter private LocalDateTime javaLocalDateTime;\n\n    // -- DATE AND TIME (WITH TIMEZONE OFFSET)\n\n    @XmlElement @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)\n    @Getter @Setter private OffsetDateTime javaOffsetDateTime;\n\n    // --\n\n    @Override\n    public void initDefaults() {\n\n        log.info(\"TemporalDemo::initDefaults\");\n\n        javaUtilDate = new Date();\n        javaSqlDate = new java.sql.Date(System.currentTimeMillis());\n        javaSqlTimestamp = new java.sql.Timestamp(System.currentTimeMillis());\n\n        javaLocalDate = LocalDate.now();\n        javaLocalDateTime = LocalDateTime.now();\n        javaOffsetDateTime = OffsetDateTime.now();\n    }\n\n}</code></pre>\n</div>\n</div>\n<div class=\"paragraph\">\n<p>See the temporal demo\n<a href=\"https://github.com/apache/isis/tree/master/examples/demo/src/main/java/demoapp/dom/types/time\">sources</a>.</p>\n</div>",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      },
      "disabledReason": "Always disabled"
    },
    "javaLocalDate": {
      "id": "javaLocalDate",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaLocalDate\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaLocalDate",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "2020-01-25",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      }
    },
    "javaLocalDateTime": {
      "id": "javaLocalDateTime",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaLocalDateTime\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaLocalDateTime",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "2020-01-25T14:07:05.356",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      }
    },
    "javaOffsetDateTime": {
      "id": "javaOffsetDateTime",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaOffsetDateTime\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaOffsetDateTime",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "20200125T140705.356+0100",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      }
    },
    "javaSqlDate": {
      "id": "javaSqlDate",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaSqlDate\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaSqlDate",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "2020-01-24",
      "format": "date",
      "extensions": {
        "x-isis-format": "javasqldate"
      }
    },
    "javaSqlTimestamp": {
      "id": "javaSqlTimestamp",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaSqlTimestamp\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaSqlTimestamp",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": 1579957625356,
      "format": "utc-millisec",
      "extensions": {
        "x-isis-format": "javasqltimestamp"
      }
    },
    "javaUtilDate": {
      "id": "javaUtilDate",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"javaUtilDate\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/properties/javaUtilDate",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "2020-01-25T13:07:05Z",
      "format": "date-time",
      "extensions": {
        "x-isis-format": "javautildate"
      }
    },
    "clearHints": {
      "id": "clearHints",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
          "href": "http://localhost:8080/restful/objects/demo.Temporal/AR-LCAAAAAAAAACFkMEKgkAYhO8-hew1tH-t1ZRVL9Ip6JA-wKJrGO5upUi9fcaCqyH0H2e-GYafpi_R2gN_do2SMcIuIJvLUlWNvMaoyI_OAdldz2TFWiV5jN68Q2li0YwLlVj2ePTGBnZ5tBnreeKBBw5gxyN0O9cNeVIlW2eNY-iib7SESRCGJPA9siO-5idvMSNvBB8Hi_taZOGvbPqas1053kcQREDcqWGJmopzXXe8_9exARwB6KafhEW3-qcf9EaPV5MBAAA=/actions/clearHints",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    }
  }
}
"""
}
