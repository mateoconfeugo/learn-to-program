define(['jquery', 'underscore', 'backbone', 'views/html_page', 'log4javascript'], 

       function($, _, Backbone, HTMLPage) {	   
	   var log = log4javascript.getDefaultLogger();

	   var wiz_manager = Backbone.View.extend({
	       events: {
		   "click  #first": "first_view",
		   "click  #previous": "previous_view",
		   "click  #finish": "finish_view"
	       },
	       initialize: function(options) {
		   _.bindAll(this, 'close') 
		   this.config = this.options.config;
		   var landing_site = Backbone.Model.extend();
		   this.model =  landing_site;
		   var site_setup = this.test_wizard_heirarchy(); 		   // var garden_setup = this.wizard_heirarchy();
		   this.render();
		   return this;
	       },
	       render: function() {
		   var i = 0, views = [];
		   _.each(this.current_node.children, function(view) { views.push(view)});
		   var html = Jemplate.process("site_wizard.tt", {views: views});
		   this.el = html;
		   this.$el.html(html);
		   $('.site_wizard').bootstrapWizard('show', this.current_position);
	       },
	       close: function(){
		   this.$el.empty()
		   this.unbind();
		   return this;
	       },
	       wizard_heirarchy: function() {
		   return { category: "garden", "title": "Setting up the Garden",
			    subcategories: [
				{category: "create_garden", "title": "Garden Attributes", "type": "GardenEditor"},
				{category: "create_reservoir", "title": "Reservoir", "type": "ReservoirEditor"},
				{category: "create_nutrient_mixer", "title": "Nutrient Mixer", "type": "MixerEditor"},
				{category: "grow", "title": "Assembling a Grow",
				 subcategories: [
				     {category: "station", "title": "Adding Stations", "type": "StationEditor"},	 
				     {category: "create_grow", "title": "Naming the Grow", "type": "GrowBrowser"},
				     {category: "feed_plan", "title": "Building a Feed Plan",
				      subcategories:[{
					  category: "recipe", "title": "Select or create Recipe", "type": "RecipeBuilder"},
					  {category: "feed_schedule", "title": "Schedule Feeding", "type": "TaskScheduler"},
				      ]},
				     {category: "actuator", "title": "Connect Actuators", "type": "PowerStripBrowser"},
				     {category: "scheduled_task", "title": "Schedule Tasks", "type": "TaskScheduler"},
				     {category: "constraint", "title": "Setup System Triggered Tasks", "type": "ConstraintTaskEditor"}
				 ]},
				{category: "configure_reporting", "title": "Reporting", "type": "ReportManager"},
				{category: "configure_alerting", "title": "Alerting", "type": "AlertManager"},
				{category: "runtime_control", "title": "Activate", "type": "RuntimeControl"}
			    ]};
	       },
	       test_wizard_heirarchy: function() {
		   return { category: "garden", "title": "Setting up the Garden",
			    subcategories: [
				{category: "create_garden", "title": "Garden Attributes", "type": "GardenBrowser"},
				{category: "create_reservoir", "title": "Reservoir", "type": "ReservoirEditor"},
				{category: "create_nutrient_mixer", "title": "Nutrient Mixer", "type": "Mixer"},
				{category: "grow", "title": "Assembling a Grow",
				 subcategories: [
				     {category: "create_grow", "title": "Attributes", "type": "Placeholder"},
				     {category: "station", "title": "Stations", "type": "Placeholder"},	 
				     {category: "dispenser", "title": "Dispenser", "type": "DispenserBrowser"},
				     {category: "feed_plan", "title": "Feed Plan",
				      subcategories:[{
					  category: "recipe", "title": "Recipe", "type": "RecipeBuilder"},
					  {category: "feed_schedule", "title": "Schedule Feeding", "type": "Placeholder"},
				      ]},
				     {category: "actuator", "title": "Actuators", "type": "PowerStripBrowser"},
				     {category: "scheduled_task", "title": "Schedule Tasks", "type": "Placeholder"},
				     {category: "constraint", "title": "Triggered Tasks", "type": "Placeholder"}
				 ]},
				{category: "configure_reporting", "title": "Reporting", "type": "Placeholder"},
				{category: "configure_alerting", "title": "Alerting", "type": "Placeholder"},
				{category: "runtime_control", "title": "Activate", "type": "Placeholder"}
			    ]};
	       }
	   });
	   return wiz_manager;
       });

