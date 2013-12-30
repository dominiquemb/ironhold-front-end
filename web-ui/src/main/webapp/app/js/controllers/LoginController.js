'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular) {
    $scope.currentlyVisible = 'login';
    $scope.loggedIn = false;
    var restMessagesService = Restangular.setBaseUrl('http://localhost:8080/users');

    $scope.makeVisible = function(elemName) {
	    $scope.currentlyVisible = elemName;
    }

    $scope.isVisible = function(elemName) {
	    return $scope.currentlyVisible === elemName;
    }

    $scope.verifyResetCode = function(code) {
	    // This is a placeholder for now
	    $scope.makeVisible('forgot-password-step-3');
    }

    $scope.isLoggedIn = function() {
	    return $scope.loggedIn;
    }

    $scope.logIn = function() {
        restMessagesService.one($scope.clientKey).one($scope.username).post("", $scope.password, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
            $scope.loggedIn = result.payload.success;
            alert(result.payload.message);
        });
    }

    $scope.logOut = function() {
        $scope.loggedIn = false;
    }
});
