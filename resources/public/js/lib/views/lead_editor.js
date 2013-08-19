/* 
   Component Name: lead_editor
   Description:  Gather leads
*/

define(
    ["jquery", "underscore", "backbone", "models/lead", "validate"],
    function($, _, Backbone, Lead) {
	var lead_editor =  Backbone.View.extend({
	    events: {
		"click .btn": "updateModel",
	    },
	    initialize: function(options) {
		_.bindAll(this, "render", "updateModel", "submit");
		this.router = options.router;
		this.model = new Lead();
		this.model.on("sync", this.render);
		this.render();
		return this;
	    },
	    render: function(e) {
		this.$el.html(Jemplate.process("lead_editor.tt"));
		$("#lead_form").validate();
		return this;
	    },
	    submit: function(e){
//		e.preventDefault();

	    },
	    updateModel: function(e) {
		e.preventDefault();
		$("#lead_form").validate();
		if(!$("#lead_form").valid()){
		    return this;
		}
		var first_name = this.$('#lead_first_name').val();
		this.model.set('first_name', first_name);
		var last_name = this.$('#lead_last_name').val();
		this.model.set('last_name', last_name);
		var email = this.$('#lead_email').val();
		this.model.set('email', email);
		var phone = this.$('#lead_phone').val();
		this.model.set('phone', phone);
		var zip = this.$('#lead_zip').val();
		this.model.set('postal_code', zip);
		this.model.save({}, {
		    success: function(model, response, options) {
			$('.modal-backdrop').remove();
			$('#lead_form_link').remove();
			$('#rootwizard').html(Jemplate.process("thank_you.tt"));			
		    },
		    error: function(e, response) {
			var json = JSON.parse(response.responseText);
			var html = Jemplate.process("server_validation_message.tt", {errors: JSON.parse(json.error)});
			this.$('.alert-error').show();
			this.$('.alert-error').append(html);
		    }
		});
		return this;
	    },
	    close: function(){
		this.$el.empty();
		this.unbind();
		return this;
	    }
	});
	return lead_editor;
    });

