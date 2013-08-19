define(['jquery', 'underscore', 'backbone'],
       function($, _, Backbone) {
	   var lead = Backbone.Model.extend({
	       validation: {
		   'lead_first_name': {
		       required: true,
		       msg: 'Please enter a last name'
		   },
		   'lead_last_name': {
		       required: true,
		       msg: 'Please enter a last name'
		   },
		   'lead_zip': {
		       length: 5,
		       required: true,
		       msg: 'Please enter a valid zip'
		   },
		   'lead_email': {
		       pattern: 'email',
		       required: true,
		       msg: 'Please enter a valid email'
		   },
		   'lead_phone': {
		       pattern: 'digits',
		       msg: 'Please enter a valid phone number'
		   }
	       },
	       urlRoot: 'lead'
	   });
	   return lead;
       });	  
