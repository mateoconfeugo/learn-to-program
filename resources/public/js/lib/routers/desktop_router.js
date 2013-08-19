define(
    ['jquery', 'underscore', 'backbone', 'common/region_manager', 'views/lead_editor', 'log4javascript'], 

    function($, _, Backbone, RegionManager, LeadEditor) {
	var log = log4javascript.getDefaultLogger();
	var router = Backbone.Router.extend({
	    initialize: function(options) {
//		_.bindAll(this, 'routes');
		this.config = options.config;
//		log.info("Initializing Bailout Client Application Router");
		this.manager = new RegionManager({config: options.config});
//		var view = new ServerMonitor({config: this.config});
//		return this;
//		$('#server_message_channel').html(view.render());
//		this.manager.show({view: ServerMonitor, el: '#server_message_channel',  config: this.config});		   	       
		this.manager.show({view: LeadEditor, el: '#stage',  config: this.config});
	    },
            'routes': {
		'lead_form': 'lead_editor'
            },
	    'lead_editor':  function() {
		this.manager.show({view: LeadEditor, el: '#stage',  config: this.config});
//		this.$('#nav-controls-destination').html(this.$('#nav-controls').html());
//		this.$('#nav-controls').remove();
	    }
	});
	return router;
    });
