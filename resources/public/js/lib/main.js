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
	socketio: '/js/lib/socket.io',
	bootstrap: '/js/lib/bootstrap',
	log4javascript:'/js/lib/log4javascript',
    },
    text: {
	useXhr: function (url, protocol, hostname, port) {
	    // allow cross-domain requests
	    // remote server allows CORS
	    return true;
	}
    },

});

require(['jquery', 'underscore', 'backbone'],
	function($, _, Backbone) {
	    var pager_cntls = $(".pager wizard");
	});
