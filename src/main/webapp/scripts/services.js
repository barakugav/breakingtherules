(function (angular, $) {
	
	var app = angular.module('BreakingTheRules');

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

	app.factory('GUI', ['Notify', function (Notify) {
		var alertActivated = false;

		// Makes sure a different bootbox alert isn't yet active
		function boot(alertType, arg1, arg2) {
			if (alertActivated) return;
			alertActivated = true;
			if (arg1.callback) {
				var userCallback = arg1.callback;
				arg1.callback = function (value) {
					alertActivated = false;
					userCallback(value);
				}
				bootbox[alertType](arg1);
			}
			else {
				var userCallback = arg2;
				arg2 = function (value) {
					alertActivated = false;
					if (userCallback) 
						userCallback(value); // should not happen in case of bootbox.alert('hi')
				}
				bootbox[alertType](arg1, arg2);
			}
		}

		return {
			notify: Notify,
			alert: function (arg1, arg2) {
				boot('alert', arg1, arg2);
			},
			confirm: function (arg1, arg2) {
				boot('confirm', arg1, arg2);
			},
			prompt: function (arg1, arg2) {
				boot('prompt', arg1, arg2);
			},
			hideDialogs: function () {
				bootbox.hideAll();
			}
		};
	}]);

	app.factory('CurrentJob', ['BtrData', '$window', function (BtrData, $window) {
		return {
			getCached: function () {
				if ($window.localStorage)
					return $window.localStorage.getItem('jobName');
				return null;
			},
			set: function (newJobName) {
				if ($window.localStorage)
					$window.localStorage.setItem('jobName', newJobName);
				return BtrData.setJob(newJobName);
			}
		};
	}]);

	app.factory('StatusMonitor', ['BtrData', 'ErrorHandler', function (BtrData, ErrorHandler) {
		var status = {};
		var curPromise;

		function update() {
			var newPromise = BtrData.getStatus();
			newPromise.then(function (response) {
				status = response.data;
			}, ErrorHandler.standard);
			curPromise = newPromise;
			return newPromise;
		}
		function onReady(f) {
			curPromise.then(f, f);
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

	app.factory('ErrorHandler', ['GUI', 'CurrentJob', '$location', '$window', 'BtrData', function (GUI, CurrentJob, $location, $window, BtrData) {

		var httpCodes = {
			UNAUTHORIZED: 401,
			INTERNAL_ERROR: 500,
			NOT_FOUND: 404
		};

		function sessionExpired() {
			var cachedJob = CurrentJob.getCached()
			if (cachedJob) {
				GUI.confirm({
					title: 'Session expired.',
					message: 'Your session has expired. Continue using the job named "' 
						+ cachedJob + '"? Click OK to continue or Cancel to' 
						+ ' return to the landing page and choose a different job.',
					callback: function (ans) {
						if (ans) {
							CurrentJob.set(cachedJob);
							$window.location.reload();
						}
						else {
							$location.url('/');
							$window.location.reload();
						}
					}
				});	
			}
			else {
				GUI.alert({
					title: 'Job not set.',
					message: 'You haven\'t yet set a job. Navigating to landing page.',
					callback: function () {
						$location.url('/');
					}
				});	
			}
		}

		function noConnection() {
			GUI.alert({
				title: 'Could not connect to server.',
				message: 'Problems connecting to the server. Please make sure the server is on '
					+ 'and your connection is stable.'
			});
		}


		// This is the standard handler for AJAX errors.
		// i.e. getStatus().then(function() { }, standardErrorHandler)
		function standardErrorHandler(error)  {
			if (error.status === httpCodes.UNAUTHORIZED) sessionExpired();
			else if (error.status === httpCodes.NOT_FOUND) noConnection();
			else GUI.alert('An unknown error occured.');
		}


		return {
			standard: standardErrorHandler
		};
	}]);

})(angular, jQuery);