'use strict';

ironholdLogin.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout) {
    $scope.currentlyVisible = null;

    $scope.makeVisible = function(elemName) {
	$scope.currentlyVisible = elemName;
    }

    $scope.isVisible = function(elemName) {
console.log('ffdsfsd');
	return $scope.currentlyVisible === elemName;
    }
});
