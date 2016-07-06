(function (angular, $) {
	
	var app = angular.module('BreakingTheRules');

	app.controller('MainController', ['$rootScope', 'BtrData', 'Notify', 'StatusMonitor', 'Constants', function($rootScope, BtrData, Notify, StatusMonitor, Constants) {
		this.attributes = Constants.attributes;

		$rootScope.$on(Constants.events.FILTER_UPDATE, function () {
			StatusMonitor.update();
		})
		$rootScope.$on(Constants.events.RULES_CHANGED, function () {
			StatusMonitor.update();
		});
	}]);

	app.controller('ChooseJobController', ['BtrData', '$location', '$scope', function (BtrData, $location, $scope) {
		var ChJobCtrl = this;

		function patience() {
			alert('Loading all your hits... Please wait...')
		}
		this.init = function () {
			ChJobCtrl.hitsFile = null;
			ChJobCtrl.allJobs = [];	
			BtrData.getAllJobs().then(function (jobs) {
				ChJobCtrl.allJobs = jobs.data.map(function (name) {
					return { name: name };
				});
			});
		}
		this.existing = function () {
			patience();
			BtrData.setJob(ChJobCtrl.existingJob.name).then(function () {
				$location.url('/main');
			});
			return false;
		};
		this.new = function () {
			if (!ChJobCtrl.hitsFile) {
				alert('You must first choose a hits file.')
				return;
			}
			patience();
			BtrData.startJob(ChJobCtrl.newJobName, ChJobCtrl.hitsFile).then(function () {
				BtrData.setJob(ChJobCtrl.newJobName).then(function () {
					$location.url('/main');
				});
			}, function (error) {
				alert('Uh oh! Error in creating new job. See console for more info.');
				console.error(error);
			}, function (progressEvt) {
				console.log('Upload progress: ' + progressEvt.loaded / progressEvt.total);
			});	
			return false;
		}
		this.chooseFile = function (file) {
			ChJobCtrl.hitsFile = file;
		};
		this.init();
	}]);

	app.controller('ProgressController', ['$rootScope', 'BtrData', 'Notify', 'StatusMonitor','Constants', function ($rootScope, BtrData, Notify, StatusMonitor, Constants) {
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
				$rootScope.$emit(Constants.events.RULES_CHANGED);
			});
		};

		$rootScope.$on(Constants.events.RULES_CHANGED, function () {
			progCtrl.updateRules();
		});

		progCtrl.init();
	}]);

	app.controller('FilterController', ['$rootScope', 'BtrData', 'Notify', 'Constants', function($rootScope, BtrData, Notify, Constants) {
		var filterCtrl = this;
		filterCtrl.filter = {};
		filterCtrl.hasFilter = false;

		filterCtrl.init = function () {
			console.log('Init filter controller');
			filterCtrl.updateFilter();
		};

		filterCtrl.updateFilter = function () {
			console.log('Updating filter...');
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
				$rootScope.$emit(Constants.events.FILTER_UPDATE, filterCtrl.hasFilter);
			});
		};

		filterCtrl.setFilter = function () {
			var setFilterArgs = '';
			var attributes = filterCtrl.filter.attributes;
			
			BtrData.putNewFilter(attributes).success(function () {
				console.log('Setting new filter');
				filterCtrl.updateFilter();
			});
		};

		filterCtrl.createRule = function () {
			BtrData.postRule().then(function () {
				Notify.success('New rule created! Updating statistics...');
				$rootScope.$emit(Constants.events.RULES_CHANGED);
			});
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

	app.controller('HitsTableController', ['$scope', 'BtrData', '$rootScope', 'Constants', function ($scope, BtrData, $rootScope, Constants) {
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

		$rootScope.$on(Constants.events.FILTER_UPDATE, function (event, isntEmpty) {
			hitsCtrl.refresh();
			hitsCtrl.hasFilter = isntEmpty;
		});

		$scope.$on(Constants.events.PAGE_CHANGE, function (event, pageNum) {
			hitsCtrl.setPage(pageNum);
		});
	}]);

	app.controller('SuggestionController', ['BtrData', '$rootScope', 'StatusMonitor', 'Constants', function (BtrData, $rootScope, StatusMonitor, Constants) {
		var sugCtrl = this;

		sugCtrl.refresh = function () {
			BtrData.getSuggestions().success(function (data) {
				console.log(data);
				sugCtrl.allSuggestions = Constants.attributes.map(function (attrName) {
					for (var i = 0; i < data.length; i++) {
						if (data[i].type.toUpperCase() == attrName.toUpperCase())
							return data[i];
					}
				});
			});
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

		$rootScope.$on(Constants.events.FILTER_UPDATE, function () {
			console.log('Filter updated. Getting suggestions.');
			sugCtrl.refresh();
		});
	}]);

})(angular, jQuery);
