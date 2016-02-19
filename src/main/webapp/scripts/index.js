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
	
	var app = angular.module('BreakingTheRules', ['btrData']);

	// Set job before ng-app begins
	app.factory('SetJob', ['$http', function ($http) {
		var job_id = parseInt(window.prompt(settings.guiStrings.JOB_PROMPT, 1));
		return $http.put('/job?job_id=' + job_id);
	}]);

	app.controller('GlobalController', ['$scope', 'BtrData', function($scope, BtrData) {
		this.attributes = settings.attributes;

		$scope.$on('ruleCreateRequest', function () {
			BtrData.postRule().then(function () {
				$scope.$broadcast('rulesChanged');
			});
		});
	}]);

	app.controller('RulesController', ['$rootScope', '$scope', 'BtrData', 'SetJob', function ($rootScope, $scope, BtrData, SetJob) {
		var rulesCtrl = this;
		rulesCtrl.rules = [];

		rulesCtrl.updateRules = function () {
			BtrData.getRules().success(function (data) {
				rulesCtrl.rules = data;
			});
		};

		rulesCtrl.delete = function (id) {
			BtrData.deleteRuleById(id).then(function () {
				rulesCtrl.updateRules();
				$rootScope.$broadcast('rulesChanged');
			});
		};

		$scope.$on('rulesChanged', function () {
			rulesCtrl.updateRules();
		});

		SetJob.then(function () {
			rulesCtrl.updateRules();
		});	
	}]);

	app.controller('FilterController', ['$scope', 'BtrData', '$rootScope', 'SetJob', function($scope, BtrData, $rootScope, SetJob) {
		var filterCtrl = this;
		filterCtrl.filter = {};
		filterCtrl.hasFilter = false;

		SetJob.then(function () {
			filterCtrl.updateFilter();
		});

		filterCtrl.updateFilter = function () {
			BtrData.getFilter().success(function (data) {
				filterCtrl.filter = data;

				var attributes = filterCtrl.filter.attributes;
				filterCtrl.hasFilter = false;

				attributes.forEach(function (attr) {
					if (attr.str && attr.str != 'Any') {
						filterCtrl.hasFilter = true;
						attr.field = attr.str;
					}
					else
						attr.field = '';
				});
				$rootScope.$emit('filterUpdate', filterCtrl.hasFilter);
			});
		};

		filterCtrl.setFilter = function () {
			var setFilterArgs = '';
			var attributes = filterCtrl.filter.attributes;
			
			BtrData.putNewFilter(attributes).success(function () {
				filterCtrl.updateFilter();
			});
		};

		$scope.$on('rulesChanged', function () {
			filterCtrl.filter.attributes.forEach(function (att) {
				att.field = 'Any';
			})
			filterCtrl.setFilter();
		});

		$scope.$on('suggestionChosen', function (event, sug) {
			var matchFound = false;
			filterCtrl.filter.attributes.forEach(function (filterAttr) {
				if (filterAttr.type === sug.attribute.type) {
					matchFound = true;
					filterAttr.field = sug.attribute.str;
					filterCtrl.setFilter();
				}
			});
			if (!matchFound) {
				throw new Error('A suggestion was chosen, but no filter attribute matched the suggestion attribute type.');
			}
		});

		$scope.$on('inputCleared', function () {
			filterCtrl.setFilter();
		});

	}]);

	app.controller('HitsTableController', ['$scope', 'BtrData', '$rootScope', function ($scope, BtrData, $rootScope) {
		var hitsCtrl = this;
		hitsCtrl.NAV_SIZE = 5; 		// how many elements in nav. always an odd number
		hitsCtrl.PAGE_SIZE = 10;	// how many hits in every page

		hitsCtrl.requestPage = function() {
			var startIndex = (hitsCtrl.page - 1) * hitsCtrl.PAGE_SIZE,
				endIndex = startIndex + hitsCtrl.PAGE_SIZE;
			BtrData.getHits(startIndex, endIndex).success(function (data) {
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

		$rootScope.$on('filterUpdate', function (event, isntEmpty) {
			hitsCtrl.refresh();
			hitsCtrl.hasFilter = isntEmpty;
		});

		$scope.$on('pageChange', function (event, pageNum) {
			hitsCtrl.setPage(pageNum);
		});

	}]);

	app.controller('SuggestionController', ['BtrData', '$rootScope', 'SetJob', function (BtrData, $rootScope, SetJob) {
		var sugCtrl = this;

		SetJob.then(function () {
			sugCtrl.refresh();
		});

		sugCtrl.refresh = function () {
			BtrData.getSuggestions().success(function (data) {
				sugCtrl.allSuggestions = data;
			});
		};

		sugCtrl.addToFilter = function (suggestion) {
			$rootScope.$broadcast('suggestionChosen', suggestion);
		};

		$rootScope.$on('filterUpdate', function () {
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


	app.directive('clearableInput', ['$window', '$rootScope', function ($window, $rootScope) {
		return {
			restrict: 'AE',
			link: function (scope, element, attr) {
				var button = $window.document.createElement('i');
				button.classList.add('glyphicon');
				button.classList.add('glyphicon-remove');
				var input = element.find('input');
				if (!input || input.length == 0)
					throw new Error('clearableInput does should have an input element inside');
				input = angular.element(input);
				button.addEventListener('click', function () {
					input.val('');
					input.triggerHandler('input');
					scope.$emit('inputCleared', input);
				});
				element[0].appendChild(button);
			}
		};
	}]);

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


