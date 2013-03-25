(function() {
    var addSoldiers = function() {
        return $.getJSON('/data/soldiers.jsonp?callback=?', function(data) {
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
                'soldierId': event.target.getAttribute('soldier-id')
            };
            var soldierId = Mustache.render(template, data);
            $('#hire-soldier-modal-body').html(soldierId);
            $('#hire-soldier-dialog').modal();

            $('#btn-dialog-hire-soldier').off('click'); // Remove previous listener if any
            $('#btn-dialog-hire-soldier').on('click', _.bind(function() {
                $.ajax({
                    type: 'POST',
                    url: "/data/hire/" + this.getAttribute('soldier-id') + "?codeName=" + $('#hire-form-code-name').val(),
                    error: function(xhr, textStatus, errorThrown) {
                    	addSoldiers().done(function() {
                    		init();
	                    	var template = $('#hire-soldier-error-template').html();
	                        var data = { 'message': xhr.statusText + " : " + xhr.responseText };
	                        var errorMessage = Mustache.render(template, data);
	                        $('#hire-soldier-error-placeholder').html(errorMessage);
	                    	console.log(xhr);
                    	});
                    },
                    success: function(xhr) {
                    	addSoldiers().done(function() {
                    		init();
                    		$('#hire-soldier-dialog').modal('hide');
                    	});
                    }
                });
            }, event.target)); // bind function on current target which is the hire button
        });
    };

    var init = function() {
        addClickListenerToSoldierButtons();
    };

    $(document).ready(function() {
        addSoldiers().done(init);
    });
})();
