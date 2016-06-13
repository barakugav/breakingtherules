'use strict';

/****************** Settings ************************/

var settings = {

	guiStrings: {
		JOB_PROMPT: 'To begin, enter the job id:'
	},
	
	attributes: [
		'source', 'destination', 'service'
	]

};

var events = {
	// global events
	RULES_CHANGED: 'rulesChanged',
	FILTER_UPDATE: 'filterUpdate',
	SUGGESTION_CHOSEN: 'suggestionChosen',

	// specific events
	INPUT_CLEARED: 'inputCleared',
	PAGE_CHANGE: 'pageChange',

};

/**************** Main Angular ********************/

(function (angular, $) {
	
	var app = angular.module('BreakingTheRules', ['btrData', 'ngRoute']);

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

		update();

		return {
			update: update,
			onReady: onReady,

			getOriginalRule: getOriginalRule,
			getCreatedRulesCount: getCreatedRulesCount,
			getTotalHitsCount: getTotalHitsCount,
			getCoveredHitsCount: getCoveredHitsCount,
			getFilteredHitsCount: getFilteredHitsCount
		};
	}]);

	app.controller('MainController', ['$rootScope', 'BtrData', 'Notify', 'StatusMonitor', function($rootScope, BtrData, Notify, StatusMonitor) {
		this.attributes = settings.attributes;

		$rootScope.$on(events.FILTER_UPDATE, function () {
			StatusMonitor.update();
		})
		$rootScope.$on(events.RULES_CHANGED, function () {
			StatusMonitor.update();
		});
	}]);

	app.controller('ChooseJobController', ['BtrData', '$location', function (BtrData, $location) {
		this.submit = function () {
			BtrData.setJob(this.jobId).then(function () {
				$location.url('/main');
			});
		};
	}]);

	app.controller('ProgressCtrl', ['$rootScope', 'BtrData', 'Notify', 'StatusMonitor', function ($rootScope, BtrData, Notify, StatusMonitor) {
		var progCtrl = this;
		progCtrl.rules = [];

		progCtrl.init = function () {
			progCtrl.updateRules();
			StatusMonitor.onReady(function () {
				progCtrl.originalRule = StatusMonitor.getOriginalRule();
			});
		};

		progCtrl.updateRules = function () {
			BtrData.getRules().success(function (data) {
				progCtrl.rules = data;
			});
		};

		progCtrl.getCoveredPercentage = function () {
			return 100 * StatusMonitor.getCoveredHitsCount() / StatusMonitor.getTotalHitsCount();
		};
		progCtrl.getUncoveredPercentage = function () {
			return 100 - progCtrl.getCoveredPercentage();
		}

		progCtrl.deleteRule = function (index) {
			BtrData.deleteRuleByIndex(index).then(function () {
				Notify.info('Rule deleted. Updating statistics...');
				progCtrl.updateRules();
				$rootScope.$emit(events.RULES_CHANGED);
			});
		};

		$rootScope.$on(events.RULES_CHANGED, function () {
			progCtrl.updateRules();
		});

		progCtrl.init();
	}]);

	app.controller('FilterController', ['$rootScope', 'BtrData', 'Notify', function($rootScope, BtrData, Notify) {
		var filterCtrl = this;
		filterCtrl.filter = {};
		filterCtrl.hasFilter = false;

		filterCtrl.init = function () {
			filterCtrl.updateFilter();
		};

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
				$rootScope.$emit(events.FILTER_UPDATE, filterCtrl.hasFilter);
			});
		};

		filterCtrl.setFilter = function () {
			var setFilterArgs = '';
			var attributes = filterCtrl.filter.attributes;
			
			BtrData.putNewFilter(attributes).success(function () {
				filterCtrl.updateFilter();
			});
		};

		filterCtrl.createRule = function () {
			BtrData.postRule().then(function () {
				Notify.success('New rule created! Updating statistics...');
				$rootScope.$emit(events.RULES_CHANGED);
			});
		};

		$rootScope.$on(events.RULES_CHANGED, function () {
			filterCtrl.filter.attributes.forEach(function (att) {
				att.field = 'Any';
			});
			filterCtrl.setFilter();
		});

		$rootScope.$on(events.SUGGESTION_CHOSEN, function (event, sug) {
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

		$rootScope.$on(events.INPUT_CLEARED, function () {
			filterCtrl.setFilter();
		});

		filterCtrl.init();
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

		$rootScope.$on(events.FILTER_UPDATE, function (event, isntEmpty) {
			hitsCtrl.refresh();
			hitsCtrl.hasFilter = isntEmpty;
		});

		$scope.$on(events.PAGE_CHANGE, function (event, pageNum) {
			hitsCtrl.setPage(pageNum);
		});
	}]);

	app.controller('SuggestionController', ['BtrData', '$rootScope', 'StatusMonitor', function (BtrData, $rootScope, StatusMonitor) {
		var sugCtrl = this;

		sugCtrl.refresh = function () {
			BtrData.getSuggestions().success(function (data) {
				console.log(data);
				sugCtrl.allSuggestions = settings.attributes.map(function (attrName) {
					for (var i = 0; i < data.length; i++) {
						if (data[i].type == attrName)
							return data[i];
					}
				});
			});
		};

		sugCtrl.addToFilter = function (suggestion) {
			$rootScope.$emit(events.SUGGESTION_CHOSEN, suggestion);
		};

		sugCtrl.filteredHitsCount = function () {
			return StatusMonitor.getFilteredHitsCount();
		}

		$rootScope.$on(events.FILTER_UPDATE, function () {
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

	app.directive('clearableInput', ['$window', function ($window) {
		return {
			restrict: 'AE',
			link: function (scope, element, attr) {
				var button = $window.document.createElement('i');
				button.classList.add('glyphicon');
				button.classList.add('glyphicon-remove');
				var input = element.find('input');
				if (!input || input.length === 0)
					throw new Error('clearableInput does should have an input element inside');
				input = angular.element(input);
				button.addEventListener('click', function () {
					input.val('');
					input.triggerHandler('input');
					scope.$emit(events.INPUT_CLEARED, input);
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

	app.directive('loadingBar', function() {
		return {
			restrict: 'E',
			templateUrl: './components/loading-bar.html'
		};
	});

	app.filter('capitalize', function() {
		// Taken from http://codepen.io/WinterJoey/pen/sfFaK
		return function(input, all) {
			var reg = (all) ? /([^\W_]+[^\s-]*) */g : /([^\W_]+[^\s-]*)/;
			return (!!input) ? input.replace(reg, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
		};
	});

})(angular, jQuery);


/******************* UTILS ******************/


// Python `range`
function range(a ,b) {
	var ans = [];
	if (a >= b) return [a];
	for (var num = a; num < b; num++)
		ans.push(num);
	return ans;
}


