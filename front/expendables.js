(function() {
    var dataHost = '';

    var retrieveConfiguration = function() {
        return $.getJSON('/conf.js', function(data) {
            dataHost = data.dataHost;
        });
    };

    var addSoldiers = function() {
        return $.getJSON('http://' + dataHost + '/data/soldiers.jsonp?callback=?', function(data) {
                var soldiersTemplate = $('#soldiers-template').html();
                var soldiersHtml = Mustache.render(soldiersTemplate, data);
                $('#soldiers-container').html(soldiersHtml);
                });
    };

    var addClickListenerToSoldierButtons = function() {
        $('.btn-hire-soldier').click(function(event) {
            if ($(event.target).hasClass('disabled')) return; // Return if button is disabled
            var template = $('#hire-soldier-modal-body-template').html();
            var data = {
                'soldierId' : event.target.getAttribute('soldier-id')
            };
            var soldierId = Mustache.render(template, data);
            $('#hire-soldier-modal-body').html(soldierId);
            $('#hire-soldier-dialog').modal();

            $('#btn-dialog-hire-soldier').off('click'); // Remove previous listener if any
            $('#btn-dialog-hire-soldier').one('click', _.bind(function() {
                $(this).addClass('disabled');
                $('#hire-soldier-dialog').modal('hide');
                // TODO : Make the ajax call
            }, event.target)); // bind function on current target which is the hire button
        });
    };

    var init = function() {
        addClickListenerToSoldierButtons();
    };

    $(document).ready(function() {
        retrieveConfiguration().done(function() {
            addSoldiers().done(init);
        });
    });
})();
