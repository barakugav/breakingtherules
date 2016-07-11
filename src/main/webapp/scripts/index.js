'use strict';


/**************** Main Angular ********************/

(function (angular, $, bootbox) {
	
	var app = angular.module('BreakingTheRules', ['btrData', 'ngRoute', 'ngFileUpload', 'ui.select', 'ngSanitize']);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/', { templateUrl: 'pages/chooseJob.html' })
			.when('/main', { templateUrl: 'pages/main.html' });
	}]);

	app.constant('Constants',  {
		attributes: ['source', 'destination', 'service'],
		events: {
			// global events
			RULES_CHANGED: 'rulesChanged',
			FILTER_UPDATE: 'filterUpdate',
			SUGGESTION_CHOSEN: 'suggestionChosen',

			// specific events
			INPUT_CLEARED: 'inputCleared',
			PAGE_CHANGE: 'pageChange',
		}
	});

	app.filter('capitalize', function() {
		// Taken from http://codepen.io/WinterJoey/pen/sfFaK
		return function(input, all) {
			var reg = (all) ? /([^\W_]+[^\s-]*) */g : /([^\W_]+[^\s-]*)/;
			return (!!input) ? input.replace(reg, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
		};
	});

})(angular, jQuery, bootbox);
