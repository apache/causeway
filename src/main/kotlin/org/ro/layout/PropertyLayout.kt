package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class PropertyLayout(val cssClass: String? = null,        //AbstractLo
                          val named: String? = null, //MemberLo
                          val describedAs: String? = null,    //MemberLo
                          var metadataError: String? = null,   //MemberLo
                          val link: Link? = null,          //MemberLo
                          val id: String? = null,           //MemberLo
                          val hidden: Boolean? = null,       //MemberLo
                          val namedEscaped: String? = null,    //MemberLo
                          val promptStyle: String? = null,   //MemberLo
                          val action: String? = null,
                          val labelPosition: String? = null,
                          val multiLine: Boolean? = null,
                          val renderedAsDayBefore: Boolean? = null,
                          val typicalLength: Int? = null,
                          val unchanging: String? = null
)
