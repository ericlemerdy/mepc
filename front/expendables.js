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

    var removeClickListenerFromDialogButton = function() {
        $('#btn-dialog-hire-soldier').off('click');
    };

    var addClickListenerToDialogButton = function(data) {
        $('#btn-dialog-hire-soldier').click(function(event) {
            $('#hire-' + data.soldierId).addClass('disabled').off('click');
            $('#hire-soldier-dialog').modal('hide');
        });
    };

    var addClickListenerToSoldierButtons = function() {
        $('.btn-hire-soldier').click(function(event) {
            var template = $('#hire-soldier-modal-body-template').html();
            var data = {
                'soldierId' : event.target.getAttribute('soldier-id')
            };
            var soldierId = Mustache.render(template, data);
            $('#hire-soldier-modal-body').html(soldierId);
            addClickListenerToDialogButton(data);
            $('#hire-soldier-dialog').modal();
        });
    };

    var init = function() {
        addClickListenerToSoldierButtons();
    };

    var addHiddenListenerToDialog = function() {
        $('#hire-soldier-dialog').on('hidden', function() {
            removeClickListenerFromDialogButton();
        });
    };

    $(document).ready(function() {
        addHiddenListenerToDialog();
        retrieveConfiguration().done(function() {
            addSoldiers().done(init);
        });
    });
})();
