(function (angular, $) {
	
	var app = angular.module('BreakingTheRules');

	app.controller('ChooseJobController', ['BtrData', 'ErrorHandler', 'CurrentJob', 'GUI', '$location', '$scope', function (BtrData, ErrorHandler, CurrentJob, GUI, $location, $scope) {
		var ChJobCtrl = this;

		// creates an alert and allows waiting for it
		function patience() {
			var listeners = [];
			GUI.alert('Loading all your hits... Please wait...', function () {
				listeners.forEach(function (callback) {
					callback();
				});
				listeners = null;
				$scope.$apply();
			});
			return {
				then: function (callback)  {
					if (listeners == null) callback();
					else listeners.push(callback);
				}
			};
		}

		this.init = function () {
			ChJobCtrl.hitsFile = null;
			ChJobCtrl.allJobs = [];	
			BtrData.getAllJobs().then(function (jobs) {
				ChJobCtrl.allJobs = jobs.data.map(function (name) {
					return { name: name };
				});
			}, ErrorHandler.standard);
		}
		this.existing = function () {
			var message = patience();
			CurrentJob.set(ChJobCtrl.existingJob.name).then(function () {
				message.then(function () {
					$location.url('/main');
				});
			}, ErrorHandler.standard);
			return false;
		};
		this.new = function () {
			if (!ChJobCtrl.hitsFile) {
				GUI.alert('You must first choose a hits file.')
				return;
			}
			var message = patience();
			BtrData.startJob(ChJobCtrl.newJobName, ChJobCtrl.hitsFile).then(
				function () {
					BtrData.setJob(ChJobCtrl.newJobName).then(function () {
						message.then(function () {
							$location.url('/main');
						});
					});
				}, 
				ErrorHandler.standard, 
				function (progressEvt) {
					console.log('Upload progress: ' + progressEvt.loaded / progressEvt.total);
				}
			);	
			return false;
		}
		this.chooseFile = function (file) {
			ChJobCtrl.hitsFile = file;
		};
		this.init();
	}]);

	app.controller('ProgressController', ['$rootScope', 'BtrData', 'ErrorHandler', 'GUI', 'StatusMonitor','Constants', function ($rootScope, BtrData, ErrorHandler, GUI, StatusMonitor, Constants) {
		var progCtrl = this;
		progCtrl.rules = [];

		progCtrl.init = function () {
			progCtrl.updateRules();
			StatusMonitor.onReady(function () {
				progCtrl.originalRule = StatusMonitor.getOriginalRule();
			});
		};

		progCtrl.updateRules = function () {
			BtrData.getRules().then(function (response) {
				progCtrl.rules = response.data;
			}, ErrorHandler.standard);
		};

		progCtrl.getCoveredPercentage = function () {
			return 100 * StatusMonitor.getCoveredHitsCount() / StatusMonitor.getTotalHitsCount();
		};
		progCtrl.getUncoveredPercentage = function () {
			return 100 - progCtrl.getCoveredPercentage();
		}

		progCtrl.deleteRule = function (index) {
			BtrData.deleteRuleByIndex(index).then(function () {
				GUI.notify.info('Rule deleted. Updating statistics...');
				progCtrl.updateRules();
				$rootScope.$emit(Constants.events.RULES_CHANGED);
			}, ErrorHandler.standard);
		};

		$rootScope.$on(Constants.events.RULES_CHANGED, function () {
			progCtrl.updateRules();
		});

		progCtrl.init();
	}]);

	app.controller('FilterController', ['$rootScope', 'BtrData', 'ErrorHandler', 'GUI', 'Constants', function($rootScope, BtrData, ErrorHandler, GUI, Constants) {
		var filterCtrl = this;
		filterCtrl.filter = {};
		filterCtrl.hasFilter = false;

		filterCtrl.init = function () {
			filterCtrl.updateFilter();
		};

		filterCtrl.updateFilter = function () {
			BtrData.getFilter().then(function (response) {
				filterCtrl.filter = response.data;

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
				$rootScope.$emit(Constants.events.FILTER_UPDATE, filterCtrl.hasFilter);
			}, ErrorHandler.standard);
		};

		filterCtrl.setFilter = function () {
			var setFilterArgs = '';
			var attributes = filterCtrl.filter.attributes;
			
			BtrData.putNewFilter(attributes).then(function () {
				console.log('Setting new filter');
				filterCtrl.updateFilter();
			}, ErrorHandler.standard);
		};

		filterCtrl.createRule = function () {
			BtrData.postRule().then(function () {
				GUI.notify.success('New rule created! Updating statistics...');
				$rootScope.$emit(Constants.events.RULES_CHANGED);
			}, ErrorHandler.standard);
		};

		$rootScope.$on(Constants.events.RULES_CHANGED, function () {
			filterCtrl.filter.attributes.forEach(function (att) {
				att.field = 'Any';
			});
			filterCtrl.setFilter();
		});

		$rootScope.$on(Constants.events.SUGGESTION_CHOSEN, function (event, sug) {
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

		$rootScope.$on(Constants.events.INPUT_CLEARED, function () {
			filterCtrl.setFilter();
		});

		filterCtrl.init();
	}]);

	app.controller('HitsTableController', ['$scope', 'BtrData', 'ErrorHandler', '$rootScope', 'Constants', function ($scope, BtrData, ErrorHandler, $rootScope, Constants) {
		var hitsCtrl = this;
		hitsCtrl.NAV_SIZE = 5; 		// how many elements in nav. always an odd number
		hitsCtrl.PAGE_SIZE = 10;	// how many hits in every page

		hitsCtrl.requestPage = function() {
			var startIndex = (hitsCtrl.page - 1) * hitsCtrl.PAGE_SIZE,
				endIndex = startIndex + hitsCtrl.PAGE_SIZE;
			BtrData.getHits(startIndex, endIndex).then(function (response) {
				hitsCtrl.numOfPages = Math.ceil(response.data.total / hitsCtrl.PAGE_SIZE);
				hitsCtrl.numOfHits = response.data.total;
				hitsCtrl.allHits = response.data.data;
			}, ErrorHandler.standard);
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

		$rootScope.$on(Constants.events.FILTER_UPDATE, function (event, isntEmpty) {
			hitsCtrl.refresh();
			hitsCtrl.hasFilter = isntEmpty;
		});

		$scope.$on(Constants.events.PAGE_CHANGE, function (event, pageNum) {
			hitsCtrl.setPage(pageNum);
		});
	}]);

	app.controller('SuggestionController', ['BtrData', 'ErrorHandler', '$rootScope', '$scope', 'StatusMonitor', 'Constants', function (BtrData, ErrorHandler, $rootScope, $scope, StatusMonitor, Constants) {
		var sugCtrl = this;
		sugCtrl.permissiveness = 50;

		// TODO get current permissiveness

		sugCtrl.refresh = function () {
			BtrData.getSuggestions().then(function (suggestions) {
				sugCtrl.allSuggestions = Constants.attributes.map(function (attrName) {
					var attributeIndex = null;
					for (var i = 0; i < suggestions.data.length; i++) {
						if (suggestions.data[i].type.toUpperCase() == attrName.toUpperCase()) {
							attributeIndex = i;
							return suggestions.data[attributeIndex];
						}
					}
				});
			}, ErrorHandler.standard);
		};

		sugCtrl.addToFilter = function (suggestion) {
			$rootScope.$emit(Constants.events.SUGGESTION_CHOSEN, suggestion);
		};

		sugCtrl.filteredHitsCount = function () {
			return StatusMonitor.getFilteredHitsCount();
		};

		sugCtrl.isInCurrentFilter = function (suggestion) {
			var suggestionAttr = suggestion.attribute;
			var filterAttrs = StatusMonitor.getFilter().attributes;

			var suggestionStr = suggestionAttr.str;
			var filterAttrStr = filterAttrs.find(function (attribute) {
				return attribute.type === suggestionAttr.type;
			}).str;

			return suggestionStr === filterAttrStr;
		};

		sugCtrl.permissivenessChangeHandler = function () {
			BtrData.setPermissiveness(sugCtrl.permissiveness).
				then(null, ErrorHandler.standard);
			sugCtrl.refresh();
		};
		sugCtrl.sliderOptions = {
			'stop': sugCtrl.permissivenessChangeHandler
		};

		$rootScope.$on(Constants.events.FILTER_UPDATE, function () {
			sugCtrl.refresh();
		});

	}]);

})(angular, jQuery);
