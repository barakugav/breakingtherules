(function (angular, $) {
	
	var app = angular.module('BreakingTheRules');
	
	app.directive('pageTurner', function () {
		return {
			restrict: 'E',
			templateUrl: './components/page-turner.html',
			scope: {
				navSize: '=',
				currentPage: '=',
				numOfPages: '='
			},
			link: function (scope, element, attr) {
				scope.nearPages = function () {
					var numOfPages = scope.numOfPages,
						page = scope.currentPage,
						NAV_SIZE = scope.navSize;
					// Edges
					if (numOfPages <= NAV_SIZE)
						return range(1, numOfPages + 1);
					if (page <= NAV_SIZE / 2)
						return range(1, NAV_SIZE + 1);
					if (page >= numOfPages - NAV_SIZE / 2)
						return range(numOfPages - NAV_SIZE + 1, numOfPages + 1);

					// Normal case
					return range(page - 2, page + 3);
				};
			}
		};
	});

	app.directive('clearableInput', ['$window', 'Constants', function ($window, Constants) {
		return {
			restrict: 'AE',
			link: function (scope, element, attr) {
				var button = $window.document.createElement('i');
				button.classList.add('glyphicon');
				button.classList.add('glyphicon-remove');
				var input = element.find('input');
				if (!input || input.length === 0)
					throw new Error('clearableInput does should have an input element inside');
				input = angular.element(input);
				button.addEventListener('click', function () {
					input.val('');
					input.triggerHandler('input');
					scope.$emit(Constants.events.INPUT_CLEARED, input);
				});
				element[0].appendChild(button);
			}
		};
	}]);

	app.directive('onEnterKey', function() {
	    return  {
	    	restrict: 'A',
	    	link: function (scope, element, attrs) {

		        element.bind("keydown keypress", function(event) {
		            var keyCode = event.which || event.keyCode;

		            // If enter key is pressed
		            if (keyCode === 13) {
		                scope.$apply(function() {
	                        // Evaluate the expression
		                    scope.$eval(attrs.onEnterKey);
		                });

		                event.preventDefault();
		            }
		        });
		    }
		};
	});

	app.directive('loadingBar', function() {
		return {
			restrict: 'E',
			templateUrl: './components/loading-bar.html'
		};
	});

})(angular, jQuery);


// Python `range`
function range(a ,b) {
	var ans = [];
	if (a >= b) return [a];
	for (var num = a; num < b; num++)
		ans.push(num);
	return ans;
}
