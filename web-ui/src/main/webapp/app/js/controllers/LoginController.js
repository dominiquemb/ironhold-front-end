'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout) {
    $scope.currentlyVisible = 'login';
    $scope.loggedIn = false;

    $scope.makeVisible = function(elemName) {
	$scope.currentlyVisible = elemName;
    }

    $scope.isVisible = function(elemName) {
	return $scope.currentlyVisible === elemName;
    }

    $scope.isLoggedIn = function() {
	return $scope.loggedIn;
    }

    $scope.logIn = function() { 
	// This is temporary and is only to simulate being logged in
	$scope.loggedIn = true;
    }

    $scope.logOut = function() {
        $scope.loggedIn = false;
    }
});