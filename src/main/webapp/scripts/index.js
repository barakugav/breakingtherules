(function() {
	
	var app = angular.module('BreakingTheRules', []);

	app.controller("HitStatController", function () {
		var that = this;
		getHits(function (hits) {
			console.log(hits);
			that.allHits = hits;
		});
	});

	function getHits(callback) {

		getHitsMock(callback); // mock
		return;

		$.get('./hits', function (response) {
			// Sucessful GET
			callback(response.data);
		}).fail(function (error) {
			console.log("GET request failed.");
		});

	}

	function getHitsMock(callback) {
		callback( [{
				id: 0,
				sourceIp: "127.0.0.1",
				destIp: "127.0.0.1",
				service: "TCP 80"
			}, {
				id: 1,
				sourceIp: "127.0.0.1",
				destIp: "127.0.0.1",
				service: "TCP 80"
			}, {
				id: 2,
				sourceIp: "127.0.0.1",
				destIp: "127.0.0.1",
				service: "TCP 80"
			}, {
				id: 3,
				sourceIp: "127.0.0.1",
				destIp: "127.0.0.1",
				service: "TCP 80"
			}, {
				id: 4,
				sourceIp: "127.0.0.1",
				destIp: "127.0.0.1",
				service: "TCP 80"
			}
		] );
	}



})();
