package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.TransferObject
import pl.treksoft.kvision.state.observableListOf

class ListDM(override val title: String) : DisplayModelWithLayout() {
    var data = observableListOf<Exposer>()
    private var rawData = observableListOf<TransferObject>()

    override fun addData(obj: TransferObject) {
        if (!rawData.contains(obj)) {
            rawData.add(obj)
            val exo = Exposer(obj as TObject)
            data.add(exo.dynamise())  //if exposer is not dynamised, data access in tables won't work
        }
    }

    override fun reset() {
        isRendered = false
        data = observableListOf()
        rawData = observableListOf()
    }

}
