var GUI_STRINGS = {
		JOB_PROMPT: "To begin, enter the job id:"
};


(function() {
	
	var app = angular.module('BreakingTheRules', []);

	// Set job before ng-app begins
	app.factory('SetJob', ['$http', function ($http) {
		var job_id = parseInt(window.prompt(GUI_STRINGS.JOB_PROMPT, 1));
		return $http.put('/job?job_id=' + job_id);
	}]);


	app.controller('HitsTableController', ['$http', '$log', 'SetJob', function ($http, $log, SetJob) {
		
		var hitsCtrl = this;
		hitsCtrl.allHits = [];

		SetJob.then(function() {
			$http.get('/hits').success(function (data) {
				$log.log('Get request success');
				$log.log(data);
				hitsCtrl.allHits = data;
			});
		});

	}]);


	app.controller('RulesController', ['$http', '$log', 'SetJob', function ($http, $log, SetJob) {
		
		var rulesCtrl = this;
		rulesCtrl.rules = [];

		SetJob.then(function() {
			$http.get('/rules').success(function (data) {
				$log.log('Get request success');
				$log.log(data);
				rulesCtrl.rules = data;
			});
		});
		
	}]);

})();






/******************* UNIMPORTANT ******************/

function getHitsMock(callback) {
	callback( [{
			id: 0,
			sourceIp: '127.0.0.1',
			destIp: '127.0.0.1',
			service: 'TCP 80'
		}, {
			id: 1,
			sourceIp: '127.0.0.1',
			destIp: '127.0.0.1',
			service: 'TCP 80'
		}, {
			id: 2,
			sourceIp: '127.0.0.1',
			destIp: '127.0.0.1',
			service: 'TCP 80'
		}, {
			id: 3,
			sourceIp: '127.0.0.1',
			destIp: '127.0.0.1',
			service: 'TCP 80'
		}, {
			id: 4,
			sourceIp: '127.0.0.1',
			destIp: '127.0.0.1',
			service: 'TCP 80'
		}
	] );
}
