package org.ro.org.ro.to.bs3

/*
 * Copyright (c) 2019.
 *
 * This file is part of xmlutil.
 *
 * This file is licenced to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You should have received a copy of the license with the source distribution.
 * Alternatively, you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//package nl.adaptivity.xml.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.QName
import nl.adaptivity.xmlutil.XMLConstants
import nl.adaptivity.xmlutil.XmlEvent
import nl.adaptivity.xmlutil.serialization.UnknownXmlFieldException
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.serializer
import nl.adaptivity.xmlutil.util.CompactFragment
import org.w3c.dom.Location
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.form.select.DataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun String.normalize() = replace(" />", "/>")

@UseExperimental(UnstableDefault::class)
val testConfiguration = JsonConfiguration(strictMode = false)

class TestCommon {

    abstract class TestBase<T>(
            val value: T,
            val serializer: KSerializer<T>,
            val serialModule: SerialModule = EmptyModule,
            private val baseXmlFormat: XML = XML(serialModule),
            private val baseJsonFormat: Json = Json(testConfiguration, serialModule)
    ) {
        abstract val expectedXML: String
        abstract val expectedJson: String

        fun serializeXml(): String = baseXmlFormat.stringify(serializer, value).normalize()

        fun serializeJson(): String = baseJsonFormat.stringify(serializer, value)

        @Test
        open fun testSerializeXml() {
            assertEquals(expectedXML, serializeXml())
        }

        @Test
        open fun testDeserializeXml() {
            assertEquals(value, baseXmlFormat.parse(serializer, expectedXML))
        }

        @Test
        open fun testSerializeJson() {
            assertEquals(expectedJson, serializeJson())
        }

        @Test
        open fun testDeserializeJson() {
            assertEquals(value, baseJsonFormat.parse(serializer, expectedJson))
        }

    }

    abstract class TestPolymorphicBase<T>(value: T, serializer: KSerializer<T>, serialModule: SerialModule)
        :TestBase<T>(value, serializer, serialModule, DataType.XML(serialModule) { autoPolymorphic = true }) {

        abstract val expectedNonAutoPolymorphicXML: String

        @Test
        fun nonAutoPolymorphic_serialization_should_work() {
            val serialized =
                    XML(context = serialModule) { autoPolymorphic = false }.stringify(serializer, value).normalize()
            assertEquals(expectedNonAutoPolymorphicXML, serialized)
        }

        @Test
        fun nonAutoPolymorphic_deserialization_should_work() {
            val actualValue = XML(context = serialModule) { autoPolymorphic = false }
                    .parse(serializer, expectedNonAutoPolymorphicXML)

            assertEquals(value, actualValue)
        }

    }

    class SimpleDataTest : TestBase<Address>(
            Address("10", "Downing Street", "London"),
            Address.serializer()
    ) {
        override val expectedXML: String =
                "<address houseNumber=\"10\" street=\"Downing Street\" city=\"London\" status=\"VALID\"/>"

        override val expectedJson: String =
                "{\"houseNumber\":\"10\",\"street\":\"Downing Street\",\"city\":\"London\",\"status\":\"VALID\"}"

        val unknownValues
            get() =
                "<address xml:lang=\"en\" houseNumber=\"10\" street=\"Downing Street\" city=\"London\" status=\"VALID\"/>"

        @Test
        fun deserialize_with_unused_attributes() {
            val e = assertFailsWith<UnknownXmlFieldException> {
                XML.parse(serializer, unknownValues)
            }

            val expectedMsgStart = "Could not find a field for name {http://www.w3.org/XML/1998/namespace}lang\n" +
                    "  candidates: houseNumber, street, city, status at position "
            val msgSubstring = e.message?.let { it.substring(0, minOf(it.length, expectedMsgStart.length)) }

            assertEquals(expectedMsgStart, msgSubstring)
        }


        @Test
        fun deserialize_with_unused_attributes_and_custom_handler() {
            var ignoredName: QName? = null
            var ignoredIsAttribute: Boolean? = null
            val xml = XML {
                unknownChildHandler = { _, isAttribute, name, _ ->
                    ignoredName = name
                    ignoredIsAttribute = isAttribute
                }
            }
            assertEquals(value, xml.parse(serializer, unknownValues))
            assertEquals(QName(XMLConstants.XML_NS_URI, "lang", "xml"), ignoredName)
            assertEquals(true, ignoredIsAttribute)
        }

    }

    class OptionalBooleanTest : TestBase<Location>(
            Location(Address("1600", "Pensylvania Avenue", "Washington DC")),
            Location.serializer()
    ) {
        override val expectedXML: String =
                "<Location><address houseNumber=\"1600\" street=\"Pensylvania Avenue\" city=\"Washington DC\" status=\"VALID\"/></Location>"
        override val expectedJson: String =
                "{\"addres\":{\"houseNumber\":\"1600\",\"street\":\"Pensylvania Avenue\",\"city\":\"Washington DC\",\"status\":\"VALID\"},\"temperature\":NaN}"

        val noisyXml
            get() =
                "<Location><unexpected><address>Foo</address></unexpected><address houseNumber=\"1600\" street=\"Pensylvania Avenue\" city=\"Washington DC\" status=\"VALID\"/></Location>"

        fun fails_with_unexpected_child_tags() {
            val e = assertFailsWith<UnknownXmlFieldException> {
                XML.parse(serializer, noisyXml)
            }
            assertEquals(
                    "Could not find a field for name {http://www.w3.org/XML/1998/namespace}lang\n" +
                            "  candidates: houseNumber, street, city, status at position [row,col {unknown-source}]: [1,1]",
                    e.message
            )
        }


        @Test
        fun deserialize_with_unused_attributes_and_custom_handler() {
            var ignoredName: QName? = null
            var ignoredIsAttribute: Boolean? = null
            val xml = XML {
                unknownChildHandler = { _, isAttribute, name, _ ->
                    ignoredName = name
                    ignoredIsAttribute = isAttribute
                }
            }
            assertEquals(value, xml.parse(serializer, noisyXml))
            assertEquals(QName(XMLConstants.NULL_NS_URI, "unexpected", ""), ignoredName)
            assertEquals(false, ignoredIsAttribute)
        }

    }

    class SimpleClassWithNullablValueNONNULL : TestBase<NullableContainer>(
            NullableContainer("myBar"),
            NullableContainer.serializer()
    ) {
        override val expectedXML: String = "<p:NullableContainer xmlns:p=\"urn:myurn\" bar=\"myBar\"/>"
        override val expectedJson: String = "{\"bar\":\"myBar\"}"
    }

    class SimpleClassWithNullablValueNULL : TestBase<NullableContainer>(
            NullableContainer(),
            NullableContainer.serializer()
    ) {
        override val expectedXML: String = "<p:NullableContainer xmlns:p=\"urn:myurn\"/>"
        override val expectedJson: String = "{\"bar\":null}"
    }

    class ASimpleBusiness : TestBase<Business>(
            Business("ABC Corp", Address("1", "ABC road", "ABCVille")),
            Business.serializer()
    ) {
        override val expectedXML: String =
                "<Business name=\"ABC Corp\"><headOffice houseNumber=\"1\" street=\"ABC road\" city=\"ABCVille\" status=\"VALID\"/></Business>"
        override val expectedJson: String =
                "{\"name\":\"ABC Corp\",\"headOffice\":{\"houseNumber\":\"1\",\"street\":\"ABC road\",\"city\":\"ABCVille\",\"status\":\"VALID\"}}"
    }

    class AChamberOfCommerce : TestBase<Chamber>(
            Chamber(
                    "hightech", listOf(
                    Business("foo", null),
                    Business("bar", null)
            )
            ),
            Chamber.serializer()
    ) {
        override val expectedXML: String = "<chamber name=\"hightech\">" +
                "<member name=\"foo\"/>" +
                "<member name=\"bar\"/>" +
                "</chamber>"
        override val expectedJson: String =
                "{\"name\":\"hightech\",\"members\":[{\"name\":\"foo\",\"headOffice\":null},{\"name\":\"bar\",\"headOffice\":null}]}"
    }

    class AnEmptyChamber : TestBase<Chamber>(
            Chamber("lowtech", emptyList()),
            Chamber.serializer()
    ) {
        override val expectedXML: String = "<chamber name=\"lowtech\"/>"
        override val expectedJson: String = "{\"name\":\"lowtech\",\"members\":[]}"
    }

    class ACompactFragment : TestBase<CompactFragment>(
            CompactFragment(listOf(XmlEvent.NamespaceImpl("p", "urn:ns")), "<p:a>someA</p:a><b>someB</b>"),
            CompactFragment.serializer()
    ) {
        override val expectedXML: String =
                "<compactFragment xmlns:p=\"urn:ns\"><p:a>someA</p:a><b>someB</b></compactFragment>"
        override val expectedJson: String =
                "{\"namespaces\":[{\"prefix\":\"p\",\"namespaceURI\":\"urn:ns\"}],\"content\":\"<p:a>someA</p:a><b>someB</b>\"}"
    }

    class ClassWithImplicitChildNamespace : TestBase<Namespaced>(
            Namespaced("foo", "bar"),
            Namespaced.serializer()
    ) {
        override val expectedXML: String =
                "<xo:namespaced xmlns:xo=\"http://example.org\"><xo:elem1>foo</xo:elem1><xo:elem2>bar</xo:elem2></xo:namespaced>"
        val invalidXml =
                "<xo:namespaced xmlns:xo=\"http://example.org\"><elem1>foo</elem1><xo:elem2>bar</xo:elem2></xo:namespaced>"
        override val expectedJson: String = "{\"elem1\":\"foo\",\"elem2\":\"bar\"}"

        @Test
        fun invalidXmlDoesNotDeserialize() {
            assertFailsWith<UnknownXmlFieldException> {
                XML.parse(serializer, invalidXml)
            }
        }
    }

    class AComplexElement : TestBase<Special>(
            Special(),
            Special.serializer()
    ) {
        override val expectedXML: String =
                """<localname xmlns="urn:namespace" paramA="valA"><paramb xmlns="urn:ns2">1</paramb><flags xmlns:f="urn:flag">""" +
                        "<f:flag>2</f:flag>" +
                        "<f:flag>3</f:flag>" +
                        "<f:flag>4</f:flag>" +
                        "<f:flag>5</f:flag>" +
                        "<f:flag>6</f:flag>" +
                        "</flags></localname>"
        override val expectedJson: String = "{\"paramA\":\"valA\",\"paramB\":1,\"flagValues\":[2,3,4,5,6]}"
    }

    class InvertedPropertyOrder : TestBase<Inverted>(
            Inverted("value2", 7),
            Inverted.serializer()
    ) {
        override val expectedXML: String = """<Inverted arg="7"><elem>value2</elem></Inverted>"""
        override val expectedJson: String = "{\"elem\":\"value2\",\"arg\":7}"

        @Test
        fun noticeMissingChild() {
            val xml = "<Inverted arg='5'/>"
            assertFailsWith<MissingFieldException> {
                XML.parse(serializer, xml)
            }
        }

        @Test
        fun noticeIncompleteSpecification() {
            val xml = "<Inverted arg='5' argx='4'><elem>v5</elem></Inverted>"
            assertFailsWith<UnknownXmlFieldException>("Could not find a field for name argx") {
                XML.parse(serializer, xml)
            }

        }
    }

    class AClassWithPolymorhpicChild : TestPolymorphicBase<Container>(
            Container("lbl", ChildA("data")),
            Container.serializer(),
            baseModule
    ) {
        override val expectedXML: String
            get() = "<Container label=\"lbl\"><childA valueA=\"data\"/></Container>"
        override val expectedJson: String
            get() = "{\"label\":\"lbl\",\"member\":{\"type\":\"nl.adaptivity.xml.serialization.ChildA\",\"valueA\":\"data\"}}"
        override val expectedNonAutoPolymorphicXML: String get() = "<Container label=\"lbl\"><member type=\".ChildA\"><value valueA=\"data\"/></member></Container>"
    }

    class AClassWithMultipleChildren: TestPolymorphicBase<Container2>(
            Container2("name2", listOf(ChildA("data"), ChildB("xxx"))),
            Container2.serializer(),
            baseModule
    ) {
        override val expectedXML: String
            get() = "<Container2 name=\"name2\"><ChildA valueA=\"data\"/><better valueB=\"xxx\"/></Container2>"
        override val expectedNonAutoPolymorphicXML: String
            get() = expectedXML
        override val expectedJson: String
            get() = "{\"name\":\"name2\",\"children\":[{\"type\":\"nl.adaptivity.xml.serialization.ChildA\",\"valueA\":\"data\"},{\"type\":\"childBNameFromAnnotation\",\"valueB\":\"xxx\"}]}"


    }

    class ASimplerClassWithUnspecifiedChildren: TestPolymorphicBase<Container3>(
            Container3("name2", listOf(ChildA("data"), ChildB("xxx"), ChildA("yyy"))),
            Container3.serializer(),
            baseModule
    ) {
        override val expectedXML: String
            get() = "<container-3 xxx=\"name2\"><childA valueA=\"data\"/><childB valueB=\"xxx\"/><childA valueA=\"yyy\"/></container-3>"
        override val expectedJson: String
            get() = "{\"xxx\":\"name2\",\"member\":[{\"type\":\"nl.adaptivity.xml.serialization.ChildA\",\"valueA\":\"data\"},{\"type\":\"childBNameFromAnnotation\",\"valueB\":\"xxx\"},{\"type\":\"nl.adaptivity.xml.serialization.ChildA\",\"valueA\":\"yyy\"}]}"
        override val expectedNonAutoPolymorphicXML: String
            get() = "<container-3 xxx=\"name2\"><member type=\"nl.adaptivity.xml.serialization.ChildA\"><value valueA=\"data\"/></member><member type=\"childBNameFromAnnotation\"><value valueB=\"xxx\"/></member><member type=\"nl.adaptivity.xml.serialization.ChildA\"><value valueA=\"yyy\"/></member></container-3>"
    }


    class CustomSerializedClass : TestBase<CustomContainer>(
            CustomContainer(Custom("foobar")),
            CustomContainer.serializer()
    ) {

        override val expectedXML: String = "<CustomContainer elem=\"foobar\"/>"
        override val expectedJson: String = "{\"nonXmlElemName\":\"foobar\"}"

    }

    class AContainerWithSealedChild: TestBase<SealedSingle>(
            SealedSingle("mySealed", SealedA("a-data")),
            SealedSingle.serializer()
    ) {
        override val expectedXML: String
            get() = "<SealedSingle name=\"mySealed\"><SealedA data=\"a-data\" extra=\"2\"/></SealedSingle>"
        override val expectedJson: String
            get() = "{\"name\":\"mySealed\",\"member\":{\"data\":\"a-data\",\"extra\":\"2\"}}"
    }

    class AContainerWithSealedChildren: TestPolymorphicBase<Sealed>(
            Sealed("mySealed", listOf(SealedA("a-data"), SealedB("b-data"))),
            Sealed.serializer(),
            sealedModule
    ) {
        override val expectedXML: String
            get() = "<Sealed name=\"mySealed\"><SealedA data=\"a-data\" extra=\"2\"/><SealedB main=\"b-data\" ext=\"0.5\"/></Sealed>"
        override val expectedJson: String
            get() = "{\"name\":\"mySealed\",\"members\":[{\"type\":\"nl.adaptivity.xml.serialization.SealedA\",\"data\":\"a-data\",\"extra\":\"2\"},{\"type\":\"nl.adaptivity.xml.serialization.SealedB\",\"main\":\"b-data\",\"ext\":0.5}]}"
        override val expectedNonAutoPolymorphicXML: String
            get() = "<Sealed name=\"mySealed\"><member type=\".SealedA\"><value data=\"a-data\" extra=\"2\"/></member><member type=\".SealedB\"><value main=\"b-data\" ext=\"0.5\"/></member></Sealed>"
    }


}
