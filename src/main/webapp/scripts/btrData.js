(function (angular) {

	var app = angular.module('btrData', ['ngFileUpload']);

	app.factory('BtrData', ['$http', 'Upload', function ($http, Upload) {

		function getAllJobs() {
			return $http.get('/job');
		}

		function setJob(jobName) {
			return $http.put('/job?job_name=' + jobName);
		}

		function startJob(jobName, hitsFile) {
			// var data = new FormData();
	        // data.append('job_name', jobName);
	        // data.append('hits_file', hitsFile);
	        // return $http.post('/job', data, {
	        //     transformRequest: angular.identity,
	        //     headers: {'Content-Type': undefined}
	        // });
	        // console.log(hitsFile);
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
			return $http.get('/rule');
		}

		function postRule() {
			return $http.post('/rule');
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
			console.log('Getting suggestions');
			return $http.get('/suggestions');
		}

		function getHits(startIndex, endIndex) {
			return $http.get('/hits?startIndex=' + startIndex + '&endIndex=' + endIndex);
		}

		function getFilter() { 
			return $http.get('/filter');
		}

		function getStatus() {
			return $http.get('/status');
		}

		function deleteRuleByIndex(index) {
			return $http.delete('/rule?index=' + index);
		}

		return {
			getAllJobs: getAllJobs,
			setJob: setJob,
			startJob: startJob,
			getRules: getRules,
			postRule: postRule,
			putNewFilter: putNewFilter,
			getSuggestions: getSuggestions,
			getHits: getHits,
			getFilter: getFilter,
			getStatus: getStatus,
			deleteRuleByIndex: deleteRuleByIndex
		};
	}]);

})(angular);