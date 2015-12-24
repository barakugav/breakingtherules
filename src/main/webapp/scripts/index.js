"use strict";

/****************** Settings ************************/

var settings = {

	guiStrings: {
		JOB_PROMPT: 'To begin, enter the job id:'
	},
	
	attributes: [
		"source", "destination", "service"
	]	

};

/**************** Main Angular ********************/

(function() {
	
	var app = angular.module('BreakingTheRules', []);

	// Set job before ng-app begins
	app.factory('SetJob', ['$http', function ($http) {
		var job_id = parseInt(window.prompt(settings.guiStrings.JOB_PROMPT, 1));
		return $http.put('/job?job_id=' + job_id);
	}]);

	app.controller('GlobalController', function() {
		this.attributes = settings.attributes;
	});

	app.controller('RulesController', ['$http', '$log', 'SetJob', function ($http, $log, SetJob) {
		var rulesCtrl = this;
		rulesCtrl.rules = [];

		SetJob.then(function () {
			$http.get('/rules').success(function (data) {
				$log.log('Get rules request success');
				$log.log(data);
				rulesCtrl.rules = data;
			});
		});
		
	}]);

	app.controller('FilterController', ['$http', '$rootScope', '$log', 'SetJob', function($http, $rootScope, $log, SetJob) {
		var filterCtrl = this;
		filterCtrl.currentFilter = {};

		SetJob.then(function () {
			filterCtrl.getFilter();
		});

		filterCtrl.getFilter = function () {
			$http.get('/filter').success(function (data) {
				$log.log('Get filter request success');
				$log.log(data);
				filterCtrl.currentFilter = data;

				var attributes = filterCtrl.currentFilter.attributes;
				for (var attributeIndex in attributes) {
					var attribute = attributes[attributeIndex];
					attribute.field = attribute.str;
				}
				$rootScope.$emit('FilterUpdate', []);
			});
		};

		filterCtrl.setFilter = function () {
			var setFilterArgs = "";
			var attributes = filterCtrl.currentFilter.attributes;
			var firstArg = true;
			for (var attributeIndex in attributes) {
				var attribute = attributes[attributeIndex];
				if (firstArg) {
					setFilterArgs += '?';
					firstArg = false;
				} else {
					setFilterArgs += '&';
				}
				setFilterArgs += attribute.type.toLowerCase() + '=' + attribute.field;
			}

			$http.put('/filter' + setFilterArgs).success(function () {
				$log.log('Set filter request success');
				$log.log(setFilterArgs);
				filterCtrl.getFilter();
			});
		};

	}]);

	app.controller('HitsTableController', ['$http', '$rootScope', '$log', 'SetJob', function ($http, $rootScope, $log, SetJob) {
		var hitsCtrl = this;
		hitsCtrl.NAV_SIZE = 5; 		// how many elements in nav. always an odd number
		hitsCtrl.PAGE_SIZE = 10;	// how many hits in every page

		SetJob.then(function () {
			hitsCtrl.requestPage();
		});

		hitsCtrl.requestPage = function() {
			var startIndex = (hitsCtrl.page - 1) * hitsCtrl.PAGE_SIZE,
				endIndex = startIndex + hitsCtrl.PAGE_SIZE;
			$http.get('/hits?startIndex=' + startIndex + '&endIndex=' + endIndex).success(function (data) {
				hitsCtrl.numOfPages = Math.ceil(data.total / hitsCtrl.PAGE_SIZE);
				hitsCtrl.allHits = data.hits;
			});
		};

		hitsCtrl.nearPages = function () {
			var numOfPages = hitsCtrl.numOfPages,
				page = hitsCtrl.page,
				NAV_SIZE = hitsCtrl.NAV_SIZE;

			// Edges
			if (numOfPages <= NAV_SIZE)
				return range(1, numOfPages + 1);
			if (page <= NAV_SIZE / 2)
				return range(1, NAV_SIZE + 1);
			if (page >= numOfPages - NAV_SIZE / 2)
				return range(numOfPages - NAV_SIZE + 1, numOfPages + 1);

			// Normal case
			return range(page - 2, page + 3);
		};

		hitsCtrl.nextPage = function () {
			hitsCtrl.setPage(hitsCtrl.page + 1);
		};
		hitsCtrl.prevPage = function () {
			hitsCtrl.setPage(hitsCtrl.page - 1);
		};

		hitsCtrl.setPage = function (newPage) {
			if (newPage <= hitsCtrl.numOfPages && newPage >= 1)  {
				hitsCtrl.page = newPage;
				hitsCtrl.requestPage();
			}
		};

		hitsCtrl.getPage = function () {
			$http.get('/hits?page=' + hitsCtrl.page).success(function (data) {
				$log.log('Get hits request success');
				$log.log(data);
				hitsCtrl.allHits = data;
			});
		};

		hitsCtrl.refresh = function () {
			hitsCtrl.page = 1;
			hitsCtrl.numOfPages = 10;
			hitsCtrl.allHits = [];
			hitsCtrl.getPage();
		};

		$rootScope.$on('FilterUpdate', function () {
			hitsCtrl.refresh();
		});

	}]);

	app.controller('SuggestionController', ['$http', '$log', 'SetJob', function ($http, $log, SetJob) {
		var sugCtrl = this;

		SetJob.then(function() {
			$http.get('/suggestions').success(function (data) {
				sugCtrl.allSuggestions = data.suggestions;
			});
		});

	}]);

	// Taken from http://codepen.io/WinterJoey/pen/sfFaK
	app.filter('capitalize', function() {
		return function(input, all) {
			var reg = (all) ? /([^\W_]+[^\s-]*) */g : /([^\W_]+[^\s-]*)/;
			return (!!input) ? input.replace(reg, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
		}
	});

})();


/******************* UTILS ******************/


// Python `range`
function range(a ,b) {
	var ans = [];
	if (a >= b) return a;
	for (var num = a; num < b; num++)
		ans.push(num);
	return ans;
}


