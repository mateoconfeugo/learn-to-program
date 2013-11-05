// Filename: main.js
require.config({
    shim: {
	'underscore': {
	    exports: '_'
	},
	"backbone": {
            deps: ["underscore", "jquery"],
            exports: "Backbone" //attaches "Backbone" to the window object
	},
	"socketio": {
	    deps: ["jquery"],
	    exports: "io"
	},
	"bootstrap": {
	    deps: ["jquery"]
	},
	"bootstrapWizard": {
	    deps: ["jquery", "bootstrap"],
	    exports: "jQuery.bootstrapWizard"
	},
	"validate": {
	    deps: ["jquery"],
	    exports: "jQuery.fn.validate"
	},
	'log4javascript': {
	    exports: 'getDefaultLogger log'
	},
	"shim": {
	    "jquery-gentleSelect" : {
		deps: ["jquery"],
		exports: "jQuery.fn.gentleSelect"
	    }
	}
    },
    paths: {
	jquery: '/js/lib/jquery.min',
	underscore: '/js/lib/underscore-min',
	backbone: '/js/lib/backbone-min',
	bootstrapWizard: '/js/lib/jquery.bootstrap.wizard.min',
	socketio: '/js/lib/socket.io',
	bootstrap: '/js/lib/bootstrap',
	log4javascript:'/js/lib/log4javascript',
	validate:'/js/lib/jquery.validate.min'
    },
    text: {
	useXhr: function (url, protocol, hostname, port) {
	    // allow cross-domain requests
	    // remote server allows CORS
	    return true;
	}
    },

});

require(['jquery', 'underscore', 'backbone', 'routers/desktop_router', 'bootstrapWizard'], 
	function($, _, Backbone, Desktop) {
	    $('#rootwizard').bootstrapWizard();
//	    $('#header').html(Jemplate.process("layout.tt"));
	    var pager_cntls = $(".pager wizard");
	    $('.hero-unit').append($('#nav-controls'));
	});

