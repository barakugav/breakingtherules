(function() {
	
	var app = angular.module('BreakingTheRules', []);

	app.controller('HitStatController', ['$http', '$log', function ($http, $log) {
		
		var hitsCtrl = this;
		hitsCtrl.allHits = [];
		hitsCtrl.title = 'Hits';
		
		$http.get('/hits').success(function (data) {
			$log.log('Get request success');
			$log.log(data);
			hitsCtrl.allHits = data;
			hitsCtrl.title = "Hits after update";
		});


	}]);


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



})();
