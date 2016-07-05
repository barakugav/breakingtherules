'use strict';


/**************** Main Angular ********************/

(function (angular, $) {
	
	var app = angular.module('BreakingTheRules', ['btrData', 'ngRoute', 'ngFileUpload']);

	app.config(['$routeProvider', function($routeProvider) {
		$routeProvider
			.when('/', { templateUrl: 'pages/chooseJob.html' })
			.when('/main', { templateUrl: 'pages/main.html' });
	}]);

	app.factory('Notify', [function () {
		var notificationPosition = 'top center';

		function success(message) {
			$.notify(message, { 
				position: notificationPosition,
				className: 'success'
			});
		}
		function info(message) {
			$.notify(message, { 
				position: notificationPosition,
				className: 'info'
			});
		}
		function warn(message) {
			$.notify(message, { 
				position: notificationPosition,
				className: 'warning'
			});
		}

		return {
			success: success,
			info: info,
			warn: warn
		};
	}]);

	app.factory('StatusMonitor', ['BtrData', function (BtrData) {
		var status = {};
		var curPromise;

		function update() {
			var newPromise = BtrData.getStatus();
			newPromise.then(function (response) {
				status = response.data;
			});
			curPromise = newPromise;
			return newPromise;
		}
		function onReady(f) {
			curPromise.then(f);
		}

		function getOriginalRule() {
			return status.originalRule;
		}
		function getCreatedRulesCount() {
			return status.createdRulesCount;
		}
		function getTotalHitsCount() {
			return status.totalHitsCount;
		}
		function getCoveredHitsCount() {
			return status.coveredHitsCount;
		}
		function getFilteredHitsCount() {
			return status.filteredHitsCount;
		}
		function getFilter() {
			return status.filter;
		}

		update();

		return {
			update: update,
			onReady: onReady,

			getOriginalRule: getOriginalRule,
			getCreatedRulesCount: getCreatedRulesCount,
			getTotalHitsCount: getTotalHitsCount,
			getCoveredHitsCount: getCoveredHitsCount,
			getFilteredHitsCount: getFilteredHitsCount,
			getFilter: getFilter
		};
	}]);

	app.factory('Constants', [function () {
		return {
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
		};
	}]);

	app.filter('capitalize', function() {
		// Taken from http://codepen.io/WinterJoey/pen/sfFaK
		return function(input, all) {
			var reg = (all) ? /([^\W_]+[^\s-]*) */g : /([^\W_]+[^\s-]*)/;
			return (!!input) ? input.replace(reg, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
		};
	});

})(angular, jQuery);
