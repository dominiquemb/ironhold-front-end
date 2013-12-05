'use strict';
var ironholdApp = angular.module('ironholdApp', ['ngRoute','ngResource','ngSanitize'])
    .config(function ($routeProvider, $locationProvider) {
        $routeProvider.when('/discovery',
            {
                templateUrl:'views/Discovery.html',
                controller: 'DiscoveryController'
            });
        $routeProvider.otherwise({redirectTo: '/discovery'});
        //$locationProvider.html5Mode(true);
     })

	.controller('dropdownCtrl', function($scope) {
		$scope.collapsed = true;
		$scope.toggleCollapse = function() {
			$scope.collapsed = !$scope.collapsed;
		}
	})

	.directive('dropdown', function() {
		return {
			restrict: 'A',
			scope: {
				dropdownElem: '=dataDropdownName'
			}
		}
	});


String.prototype.endsWith = function(suffix) {
    return this.toLowerCase().indexOf(suffix.toLowerCase(), this.length - suffix.length) !== -1;
};
