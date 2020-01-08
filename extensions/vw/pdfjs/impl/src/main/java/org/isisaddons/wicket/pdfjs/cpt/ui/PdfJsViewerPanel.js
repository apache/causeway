$(function () {

    function raiseEvent(elem, event, moreArgs) {
        var canvasId = elem.data('canvasId');
        var args = {"canvasId": canvasId}
        if (moreArgs) {
            for (a in moreArgs){
                args[a] = moreArgs[a];
            }
        }
        Wicket.Event.publish(event, args);
    }

    $('.pdf-js-page-prev').click(function () {
        raiseEvent($(this), WicketStuff.PDFJS.Topic.PREVIOUS_PAGE);
    });

    $('.pdf-js-page-next').click(function () {
        raiseEvent($(this), WicketStuff.PDFJS.Topic.NEXT_PAGE);
    });

    $('.pdf-js-zoom-in').click(function () {
        raiseEvent($(this), WicketStuff.PDFJS.Topic.ZOOM_IN);
    });

    $('.pdf-js-zoom-out').click(function () {
        raiseEvent($(this), WicketStuff.PDFJS.Topic.ZOOM_OUT);
    });

    $('.pdf-js-print').click(function () {
        raiseEvent($(this), WicketStuff.PDFJS.Topic.PRINT);
        $('.progress').show();
        $('.progress-bar').css( "width",  "5%" );
    });

    $('.pdf-js-zoom-current').change(function () {
        var scale = $(this).val();
        raiseEvent($(this), WicketStuff.PDFJS.Topic.ZOOM_TO, {scale : scale});
    });

    $('.pdf-js-height-current').change(function () {
        var height = $(this).val();
        raiseEvent($(this), WicketStuff.PDFJS.Topic.HEIGHT_TO, {height: height});
    });

    $('.pdf-js-page-current').change(function () {
        var page = parseInt($(this).val());
        raiseEvent($(this), WicketStuff.PDFJS.Topic.PAGE_TO, {page : page});
    });

    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.CURRENT_PAGE, function (jqEvent, pageNumber, data) {
        $('.pdf-js-page-current[data-canvas-id="'+data.canvasId+'"]').val(pageNumber);

        var $prevPageBtn = $('.pdf-js-page-prev[data-canvas-id="'+data.canvasId+'"]');
        if (pageNumber === 1) {
            $prevPageBtn.attr("disabled", "disabled");
        } else {
            $prevPageBtn.removeAttr("disabled");
        }

        var $nextPageBtn = $('.pdf-js-page-next[data-canvas-id="'+data.canvasId+'"]');
        if (pageNumber === $nextPageBtn.data("total-pages")) {
            $nextPageBtn.attr("disabled", "disabled");
        } else {
            $nextPageBtn.removeAttr("disabled");
        }
    });

    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.TOTAL_PAGES, function (jqEvent, total, data) {
        $('.pdf-js-page-total[data-canvas-id="'+data.canvasId+'"]').text(total);

        var $nextPageBtn = $('.pdf-js-page-next[data-canvas-id="'+data.canvasId+'"]');
        $nextPageBtn.data("total-pages", total);
        if (total > 1) {
            $nextPageBtn.removeAttr("disabled");
        }

        var currentPage = $('.pdf-js-page-current[data-canvas-id="'+data.canvasId+'"]');
        var size = total.toString().length;

        currentPage.attr('maxLength', size);
        currentPage.css('width', size + 'em');
    });

    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.CURRENT_PAGE, function (jqEvent, pageNumber, data) {
        $('.pdf-js-page-current[data-canvas-id="'+data.canvasId+'"]').val(pageNumber);

        var $prevPageBtn = $('.pdf-js-page-prev[data-canvas-id="'+data.canvasId+'"]');
        if (pageNumber === 1) {
            $prevPageBtn.attr("disabled", "disabled");
        } else {
            $prevPageBtn.removeAttr("disabled");
        }

        var $nextPageBtn = $('.pdf-js-page-next[data-canvas-id="'+data.canvasId+'"]');
        if (pageNumber === $nextPageBtn.data("total-pages")) {
            $nextPageBtn.attr("disabled", "disabled");
        } else {
            $nextPageBtn.removeAttr("disabled");
        }

        PdfJsViewerPanel.Callbacks.updatePageNum(pageNumber);

    });

    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.CURRENT_PRINT_PAGE, function (jqEvent, pageNumber, data) {
        if (pageNumber === -1){
            // error
            window.alert("Printing failed!")
        }
        else {
            var total = parseInt($('.pdf-js-page-total[data-canvas-id="'+data.canvasId+'"]').text());

            if (pageNumber === total) {
                $('.progress').hide();
            }
            else {
                var percent = Math.floor((pageNumber / total) * 100);
                $('.progress-bar').css( "width", percent + "%" );
            }
        }

    });


    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.CURRENT_ZOOM, function (jqEvent, zoom, data) {

        var zoomDropDown =  $('.pdf-js-zoom-current[data-canvas-id="'+data.canvasId+'"]');

        zoomDropDown.val(zoom);

        if (!zoomDropDown.val()) {
            $("option.pdf-js-zoom").each(function() {
               $(this).remove();
            });

            addZoomOptions(zoomDropDown, zoom);
        }

        PdfJsViewerPanel.Callbacks.updateScale(zoom);
    })

    Wicket.Event.subscribe(WicketStuff.PDFJS.Topic.CURRENT_HEIGHT, function (jqEvent, height, data) {

        var heightDropDown =  $('.pdf-js-height-current[data-canvas-id="'+data.canvasId+'"]');

        heightDropDown.val(height);

        if (!heightDropDown.val()) {
            $("option.pdf-js-height").each(function() {
               $(this).remove();
            });

            addHeightOptions(heightDropDown, height);
        }

        PdfJsViewerPanel.Callbacks.updateHeight(height);

    })


    function addZoomOptions(zoomDropDown, currentZoom) {
        var zoomNumericOptions = ["0.50", "0.75", "1.00", "1.25", "1.50", "2.00", "3.00", "4.00"];
        var numericOptions = addOptions(zoomNumericOptions, currentZoom);

        for (var i = 0; i < numericOptions.length; i++) {
            var zoom = numericOptions[i];

            var title = Math.floor((zoom * 100)) + "%";
            var op = "<option value='" + zoom + "' class='pdf-js-numeric'>" + title + "</option>"
            zoomDropDown.append($(op));
        }

        zoomDropDown.val(currentZoom);
    }


    function addHeightOptions(heightDropDown, currentHeight) {
        var heightNumericOptions = ["400", "500", "600", "700", "800", "1000", "1200", "1400"];
        var numericOptions = addOptions(heightNumericOptions, currentHeight);

        for (var i = 0; i < numericOptions.length; i++) {
            var height = numericOptions[i];

            var title = height + "px";
            var op = "<option value='" + title + "' class='pdf-js-numeric'>" + height + "</option>"
            heightDropDown.append($(op));
        }

        heightDropDown.val(currentHeight);
    }


    function addOptions(options, currentOpt) {

        var newOptions = [];
        var added = false;

        for (var i = 0; i < options.length; i++) {
            var option = options[i];

            if (!added && parseFloat(currentOpt) < parseFloat(option)) {
                newOptions.push(currentOpt);
                added = true;
            }
            else if (currentOpt === option) {
                added = true;
            }

            newOptions.push(option);
        }

        if (!added){
            newOptions.push(currentOpt);
        }

        return newOptions;
    }



});
