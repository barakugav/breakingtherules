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

	app.controller('RulesController', ['$http', 'SetJob', function ($http, SetJob) {
		var rulesCtrl = this;
		rulesCtrl.rules = [];

		SetJob.then(function () {
			$http.get('/rules').success(function (data) {
				rulesCtrl.rules = data;
			});
		});
		
	}]);

	app.controller('FilterController', ['$http', '$rootScope', 'SetJob', function($http, $rootScope, SetJob) {
		var filterCtrl = this;
		filterCtrl.currentFilter = {};

		SetJob.then(function () {
			filterCtrl.updateFilter();
		});

		filterCtrl.updateFilter = function () {
			$http.get('/filter').success(function (data) {
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
				setFilterArgs +=
					attribute.type.toLowerCase() + 
					'=' + 
					(attribute.field ? attribute.field : "Any");
			}

			$http.put('/filter' + setFilterArgs).success(function () {
				filterCtrl.updateFilter();
			});
		};

	}]);

	app.controller('HitsTableController', ['$scope', '$http', '$rootScope', function ($scope, $http, $rootScope) {
		var hitsCtrl = this;
		hitsCtrl.NAV_SIZE = 5; 		// how many elements in nav. always an odd number
		hitsCtrl.PAGE_SIZE = 10;	// how many hits in every page

		hitsCtrl.requestPage = function() {
			var startIndex = (hitsCtrl.page - 1) * hitsCtrl.PAGE_SIZE,
				endIndex = startIndex + hitsCtrl.PAGE_SIZE;
			$http.get('/hits?startIndex=' + startIndex + '&endIndex=' + endIndex).success(function (data) {
				hitsCtrl.numOfPages = Math.ceil(data.total / hitsCtrl.PAGE_SIZE);
				hitsCtrl.numOfHits = data.total;
				hitsCtrl.allHits = data.data;
			});
		};

		hitsCtrl.setPage = function (newPage) {
			if (newPage <= hitsCtrl.numOfPages && newPage >= 1)  {
				hitsCtrl.page = newPage;
				hitsCtrl.requestPage();
			}
		};

		// On initiation and on filter change
		hitsCtrl.refresh = function () {
			hitsCtrl.page = 1;
			hitsCtrl.numOfPages = 10;
			hitsCtrl.allHits = [];
			hitsCtrl.requestPage();
		};

		$rootScope.$on('FilterUpdate', function () {
			hitsCtrl.refresh();
		});

		$scope.$on('pageChange', function (event, pageNum) {
			hitsCtrl.setPage(pageNum);
		});

	}]);

	app.controller('SuggestionController', ['$http', '$rootScope', 'SetJob', function ($http, $rootScope, SetJob) {
		var sugCtrl = this;

		SetJob.then(function() {
			sugCtrl.refresh();
		});

		sugCtrl.refresh = function() {
			$http.get('/suggestions').success(function (data) {
				sugCtrl.allSuggestions = data;
			});
		};

		$rootScope.$on('FilterUpdate', function () {
			sugCtrl.refresh();
		});

	}]);

	app.directive('pageTurner', function () {
		return {
			restrict: 'E',
			templateUrl: './components/page-turner.html',
			scope: {
				navSize: '=',
				currentPage: '=',
				numOfPages: '='
			},
			link: function (scope, element, attr) {
				scope.nearPages = function () {
					var numOfPages = scope.numOfPages,
						page = scope.currentPage,
						NAV_SIZE = scope.navSize;
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
			}
		};
	});

	app.directive('onEnterKey', function() {
	    return  {
	    	restrict: 'A',
	    	link: function (scope, element, attrs) {

		        element.bind("keydown keypress", function(event) {
		            var keyCode = event.which || event.keyCode;

		            // If enter key is pressed
		            if (keyCode === 13) {
		                scope.$apply(function() {
		                        // Evaluate the expression
		                    scope.$eval(attrs.onEnterKey);
		                });

		                event.preventDefault();
		            }
		        });
		    }
		};
	});


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


