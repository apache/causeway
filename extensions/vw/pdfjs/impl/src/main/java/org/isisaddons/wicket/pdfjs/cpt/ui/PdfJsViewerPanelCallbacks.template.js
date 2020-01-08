if (typeof(PdfJsViewerPanel) !== 'object') {
    window.PdfJsViewerPanel = {};

    if (typeof(PdfJsViewerPanel.Callbacks) !== 'object') {
        PdfJsViewerPanel.Callbacks = {
            updatePageNum: function (pageNum) {
                Wicket.Ajax.get({'u':'__updatePageNum_getCallbackUrl()__&pageNum=' + pageNum})
            },
            updateScale: function (scale) {
                Wicket.Ajax.get({'u':'__updateScale_getCallbackUrl()__&scale=' + scale})
            },
            updateHeight: function (height) {
                Wicket.Ajax.get({'u':'__updateHeight_getCallbackUrl()__&height=' + height})
            }
        };
    }

}


