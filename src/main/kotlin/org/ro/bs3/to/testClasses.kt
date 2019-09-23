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

/*
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.modules.SerializersModule
import nl.adaptivity.xmlutil.serialization.*


enum class AddresStatus {
    VALID,
    INVALID,
    TEMPORARY
}

@Serializable
@XmlSerialName("address", namespace = "", prefix = "")
data class Address(
        val houseNumber: String,
        val street: String,
        val city: String,
        @XmlElement(false) val status: AddresStatus = AddresStatus.VALID
)

@Serializable
data class Location(
        val addres: Address,
        @XmlDefault("NaN")
        val temperature: Double = Double.NaN
)

@Serializable
data class Business(val name: String, @XmlSerialName("headOffice", "", "") val headOffice: Address?)

@Serializable
@XmlSerialName("chamber", namespace = "", prefix = "")
data class Chamber(val name: String, @XmlSerialName("member", namespace = "", prefix = "") val members: List<Business>)

@Serializable
@XmlSerialName("localname", "urn:namespace", prefix = "")
data class Special(
        val paramA: String = "valA",
        @XmlSerialName("paramb", namespace = "urn:ns2", prefix = "")
        @XmlElement(true) val paramB: Int = 1,
        @SerialName("flagValues")
        @XmlSerialName("flags", namespace = "urn:namespace", prefix = "")
        @XmlChildrenName("flag", namespace = "urn:flag", prefix = "f")
        val param: List<Int> = listOf(2, 3, 4, 5, 6)
)

@Serializable
data class Inverted(
        @Required
        @XmlElement(true)
        val elem: String = "value",
        @Required
        val arg: Short = 6
)

@Serializable
@XmlSerialName("namespaced", "http://example.org", "xo")
data class Namespaced(
        @XmlElement(true)
        val elem1: String,
        @XmlElement(true)
        val elem2: String
)

@Serializable
@XmlSerialName("NullableContainer", "urn:myurn", "p")
data class NullableContainer(var bar: String? = null)

@Serializable
data class CustomContainer(
        @SerialName("nonXmlElemName")
        @XmlSerialName("elem", "", "")
        @Serializable(with = CustomSerializer::class)
        val somethingElse: Custom
)

data class Custom(val property: String)

@Serializer(forClass = Custom::class)
class CustomSerializer : KSerializer<Custom> {

    override val descriptor: SerialDescriptor = StringSerializer.descriptor


    override fun deserialize(decoder: Decoder): Custom {
        return Custom(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, obj: Custom) {
        encoder.encodeString(obj.property)
    }
}

@Serializable
open class Base

val baseModule = SerializersModule {
    polymorphic(Base::class) {
        ChildA::class with ChildA.serializer()
        ChildB::class with ChildB.serializer()
    }
}

@Serializable
@XmlSerialName("childA", namespace = "", prefix = "")
data class ChildA(val valueA: String) : Base()

@Serializable
@SerialName("childBNameFromAnnotation")
@XmlSerialName("childB", namespace = "", prefix = "")
data class ChildB(val valueB: String) : Base()

@Serializable
data class Container(val label: String, @Polymorphic val member: Base)

@Serializable
data class Container2(val name: String, @XmlPolyChildren(arrayOf(".ChildA", "childBNameFromAnnotation=better")) val children: List<@Polymorphic Base>)

@SerialName("container-3")
@Serializable
data class Container3(val xxx: String, @SerialName("member") val members: List<@Polymorphic Base>)

@Serializable
sealed /*open*/ class SealedParent

val sealedModule = SerializersModule {
    polymorphic(SealedParent::class) {
        SealedA::class with SealedA.serializer()
        SealedB::class with SealedB.serializer()
    }
}

@Serializable
data class SealedA(val data: String, val extra: String = "2") : SealedParent()

@Serializable
data class SealedB(val main: String, val ext: Float = 0.5F) : SealedParent()

@Serializable
data class Sealed(
        val name: String,
        @XmlSerialName("member", "", "")
        val members: List<@Polymorphic SealedParent>
)

@Serializable
data class SealedSingle(val name: String, val member: SealedA)
          */
