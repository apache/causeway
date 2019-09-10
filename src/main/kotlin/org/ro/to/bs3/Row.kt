package org.ro.to.bs3

class Row(
        protected var colOrClearFixVisibleOrClearFixHidden: List<Bs3RowContent>? = ArrayList<Bs3RowContent>(),
        var metadataError: String,
        var id: String,
        cssClass: String
) : Bs3ElementAbstract(cssClass)
