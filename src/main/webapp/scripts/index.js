var GUI_STRINGS = {
		JOB_PROMPT: 'To begin, enter the job id:'
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

		hitsCtrl.page = 1;
		hitsCtrl.numOfPages = 10;
		hitsCtrl.allHits = [];

		hitsCtrl.NAV_SIZE = 5; // always an odd number

		SetJob.then(function() {
			hitsCtrl.getPage();
		});

		hitsCtrl.getPage = function() {
			$http.get('/hits?page=' + hitsCtrl.page).success(function (data) {
				$log.log('Get request success');
				$log.log(data);
				hitsCtrl.allHits = data;
			});
		};

		hitsCtrl.nearPages = function () {
			var numOfPages = hitsCtrl.numOfPages,
				page = hitsCtrl.page,
			 	NAV_SIZE = hitsCtrl.NAV_SIZE;

			// Edges
			if (numOfPages <= NAV_SIZE)
				return range(1, numOfPages + 1);
			if (page <= NAV_SIZE / 2)
				return range(1, NAV_SIZE + 1);
			if (page >= numOfPages - NAV_SIZE / 2)
				return range(numOfPages - NAV_SIZE + 1, numOfPages + 1);

			// Normal case
			return range(page - 2, page + 3);
		}

		hitsCtrl.nextPage = function () {
			hitsCtrl.setPage(hitsCtrl.page + 1);
		};
		hitsCtrl.prevPage  = function () {
			hitsCtrl.setPage(hitsCtrl.page - 1);
		};

		hitsCtrl.setPage  = function (newPage) {
			if (newPage <= hitsCtrl.numOfPages && newPage >= 1)  {
				hitsCtrl.page = newPage;
				hitsCtrl.getPage();
			}
		};


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


/******************* UTILS ******************/


// Python `range`
function range(a ,b) {
	var ans = [];
	if (a >= b) return a;
	for (var num = a; num < b; num++)
		ans.push(num);
	return ans;
}



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
