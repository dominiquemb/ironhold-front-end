'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, $state, logInService) {
    $scope.currentlyVisible = 'login';
    $scope.formSubmitted = false;
    $scope.formInvalid = false;

    var restMessagesService = Restangular.setBaseUrl('http://localhost:8080/users');

    $scope.makeVisible = function(elemName) {
	    $scope.currentlyVisible = elemName;
    }

    $scope.submit = function() {
	$scope.formSubmitted = true;
	$scope.logIn();
    }

    $scope.isFormSubmitted = function() {
	    return $scope.formSubmitted;
    }

    $scope.isFormInvalid = function() {
	    return $scope.formInvalid;
    }

    $scope.isVisible = function(elemName) {
	    return $scope.currentlyVisible === elemName;
    }

    $scope.verifyResetCode = function(code) {
	    // This is a placeholder for now
	    $scope.makeVisible('forgot-password-step-3');
    }

    $scope.logIn = function() {
        restMessagesService.one($scope.clientKey).one($scope.username).post("", $scope.password, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
            if (result.payload.success) {
		$rootScope.logIn();
	    	/* This redirection should be improved later */
		$state.go('main.discovery');
	    	/* */
		$scope.formInvalid = false;
	    }
	    else {
		$scope.formInvalid = true;
	    }

            return $rootScope.isLoggedIn();
        });
    }
});
