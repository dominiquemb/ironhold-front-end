'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, $state, logInService) {
    if (logInService.confirmLoggedIn($state)) {
	$state.go('loggedin.main');
    }

    $scope.currentlyVisible = 'login';
    $scope.formSubmitted = false;
    $scope.formInvalid = false;

    var restMessagesService = Restangular.setBaseUrl('${rest-api.proto}://${rest-api.host}:${rest-api.port}/${rest-api.prefix}');

    $scope.makeVisible = function(elemName) {
	    $scope.currentlyVisible = elemName;
    }

    $scope.submit = function() {
	$scope.formSubmitted = true;
	$scope.logIn($scope.clientKey, $scope.username);
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
        restMessagesService.one('users').one($scope.clientKey).one($scope.username).post("", $scope.password, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
            if (result.payload.success) {
		logInService.logIn($scope.clientKey, $scope.username);
	    	/* This redirection should be improved later */
		$state.go('loggedin.main');
	    	/* */
		$scope.formInvalid = false;
	    }
	    else {
		$scope.formInvalid = true;
	    }

            return logInService.isLoggedIn();
        });
    }
});
