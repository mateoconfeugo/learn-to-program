define(['jquery', 'underscore', 'backbone'],
       function($, _, Backbone) {
	   var Report = Backbone.Model.extend({
	       urlRoot: 'api/rest/report'
	   });
	   var Reports = Backbone.Collection.extend({
	       model: Report,
	       urlRoot: 'api/rest/report'
	   });
	   var ReportBrowser = Backbone.View.extend({
	       events: {
		   'click td a': 'clicked',
		   'submit form': 'updateModel'
	       },
	       initialize: function(options) {
		   _.bindAll(this, 'render', 'close', 'clicked', 'updateModel');
		   this.config = options.config;
		   this.collection = new Reports({config: options.config});
		   this.collection.add({id: 1, title: 'co2 levels', active: 1});
		   this.collection.add({id: 2, title: 'temperature', active: 1});
		   this.collection.on('change', this.render);
		   this.collection.on('sync', this.render);
		   this.collection.fetch();
		   this.render();  // TODO: remove once dispenser added to data base
	       },
	       clicked: function(e){
		   e.preventDefault();
		   var uri = e.currentTarget.href;
		   var regex = /([0-9]*)$/;
		   var result = uri.match(regex)[0];
		   var model = _.head(this.collection.where({id: result}));
		   this.model = model;
		   var html = Jemplate.process('report_editor.tt', {report: {id: 1, title: 'co2 levels', active: 1}});
		   this.$('#report_editor').html(html);
		   return this;
	       },
	       render: function() {
		   this.$el.html(Jemplate.process('report_browser.tt', {"reports": this.collection.toJSON()}));
		   if(this.model) {
		       this.$('#report_editor').html(Jemplate.process('report_editor.tt', {"report": this.model.toJSON()}));
		   }
	       },
	       updateModel: function(e){
		   e.preventDefault();
	       },
	       close: function(){
		   this.$el.empty()
		   this.unbind();
		   return this;
	       }
	   });
	   return ReportBrowser;
       });

