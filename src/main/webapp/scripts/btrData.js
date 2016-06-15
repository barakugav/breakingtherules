(function (angular) {

	var app = angular.module('btrData', []);

	app.factory('BtrData', ['$http', function ($http) {

		function setJob(jobName) {
			return $http.put('/job?job_name=' + jobName);
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
			setJob: setJob,
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