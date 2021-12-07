var viz = new Viz();

function render(graph) {
    $("#topology").empty();
    viz.renderSVGElement(graph)
    .then(function(element) {
        $("#topology").append(element);
        svgPanZoom('#topology svg', {
            zoomEnabled: true,
            controlIconsEnabled: true
        });
    })
}

function topology(key, downstream) {
    var url = "/topology";
    if (key !== 'all_flows') {
        url = url + "?from="+key+"&downstream="+downstream;
    }
    $.get(url, function(graph) {
        render(graph);
    });
}

function items() {
    $.get("/items", function(items) {
        $.each(items, function(val, item) {
            $('#items').append(
                $('<option></option>').val(item.key).html(item.name)
            );
        });
    });
}

$(function() {
    items();
    topology($('#items option:selected').val(), $('#downstream').is(':checked'))
    $('#items, #downstream').change(function() {
        topology($('#items option:selected').val(), $('#downstream').is(':checked'))
    });
});