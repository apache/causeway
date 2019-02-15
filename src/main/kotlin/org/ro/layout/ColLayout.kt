package org.ro.layout

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import org.ro.to.Link

@Serializable
data class ColLayout(@Optional val domainObject: DomainObjectLayout? = null,
                     @Optional val action: List<ActionLayout> = emptyList(),  // org.ro.authors
                     @Optional val named: String? = "",
                     @Optional val describedAs: String? = "",
                     @Optional val plural: String? = "",
                     @Optional val link: Link? = null,
                     @Optional val bookmarking: String? = "",
                     val metadataError: String? = "",
                     val cssClass: String? = "",
                     @Optional val cssClassFa: String? = "",
                     @Optional val cssClassFaPosition: String? = "",
                     @Optional val namedEscaped: Boolean? = false,
                     @Optional val size: String? = "",
                     @Optional val id: String? = "",
                     @Optional val span: Int? = 0,
                     @Optional val unreferencedActions: Boolean? = false,
                     @Optional val unreferencedCollections: Boolean? = false,
                     @Optional val tabGroup: List<TabGroupLayout> = emptyList(),
                     @Optional val fieldSet: List<FieldSetLayout> = emptyList()
) {
    /*  fun build(): HBox {
          val result = HBox()
          UIUtil().decorate(result, "ColLayout", "debug")
          var b: UIComponent?
          for (tl in tabGroup) {
              b = tl.build()
              result.addChild(b)
          }
          for (fsl in fieldSet) {
              b = fsl.build()
              result.addChild(b)
          }
          // actions will not be rendered as buttons
          return result
      }   */

}