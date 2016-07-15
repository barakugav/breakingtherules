(function (angular) {

	var app = angular.module('btrData', ['ngFileUpload']);

	app.factory('BtrData', ['$http', 'Upload', function ($http, Upload) {

		// There is a problem when the first requests are sent simultaneously.
		// See: http://stackoverflow.com/questions/13015583/
		// Other requests must wait for the first to finish
		var firstRequest = null;
		function afterFirstRequest(asyncFunc) {
			return {
				then: function (success, failure) {
					if (!firstRequest) {
						firstRequest = asyncFunc();
						firstRequest.then(success, function (error) {
							firstRequest = null; // The first request failed. Try another.
							failure(error);
						});
					}
					else {
						firstRequest.then(function () {
							asyncFunc().then(success, failure);
						});
					}
				}
			};
		}

		function getAllJobs() {
			return afterFirstRequest(function () {
				return $http.get('/job');
			});
		}

		// Do not use directly. Only using CurrentJob service.
		function setJob(jobName) {
			return $http.put('/job?job_name=' + jobName);
		}

		function startJob(jobName, hitsFile) {
			/* Different way to do it:
			var data = new FormData();
	        data.append('job_name', jobName);
	        data.append('hits_file', hitsFile);
	        return $http.post('/job', data, {
	            transformRequest: angular.identity,
	            headers: {'Content-Type': undefined}
	        });
	        console.log(hitsFile); */
	        hitsFile.upload = Upload.upload({
	        	url: '/job',
	        	data: {
	        		'job_name': jobName,
	        		'hits_file': hitsFile
	        	}
	        });
	        return hitsFile.upload;
		}

		function getRules() {
			return afterFirstRequest(function () {
				return $http.get('/rule');
			});
		}

		function postRule() {
			return afterFirstRequest(function () {
				return $http.post('/rule');
			});
		}

		function putNewFilter(filterAttributes) {
			var urlStr = '/filter?';

			filterAttributes.forEach(function (attr) {
				if (!attr.field) attr.field = 'Any';
			})
			urlStr = filterAttributes.reduce(function (previousValue, currentAttr) {
				return previousValue + currentAttr.type.toLowerCase() + '=' + currentAttr.field + '&';
			}, urlStr);
			urlStr = urlStr.slice(0, -1);

			return $http.put(urlStr);
		}

		function getSuggestions() {
			return afterFirstRequest(function () {
				return $http.get('/suggestions');
			});
		}

		function setPermissiveness(permissiveness) {
			return afterFirstRequest(function () {
				return $http.put('/permissiveness?permissiveness=' + permissiveness);
			});
		}

		function getHits(startIndex, endIndex) {
			return afterFirstRequest(function () {
				return $http.get('/hits?startIndex=' + startIndex + '&endIndex=' + endIndex);
			});
		}

		function getFilter() { 
			return afterFirstRequest(function () {
				return $http.get('/filter');
			});
		}

		function getStatus() {
			return afterFirstRequest(function () {
				return $http.get('/status');
			});
		}

		function deleteRuleByIndex(index) {
			return afterFirstRequest(function () {
				return $http.delete('/rule?index=' + index);
			});
		}
		

		return {
			getAllJobs: getAllJobs,
			setJob: setJob,
			startJob: startJob,
			getRules: getRules,
			postRule: postRule,
			putNewFilter: putNewFilter,
			getSuggestions: getSuggestions,
			setPermissiveness: setPermissiveness,
			getHits: getHits,
			getFilter: getFilter,
			getStatus: getStatus,
			deleteRuleByIndex: deleteRuleByIndex,
		};
	}]);

	

})(angular);