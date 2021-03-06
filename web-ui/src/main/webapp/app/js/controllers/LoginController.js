(function () {
   'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, $state, logInService) {
    if (logInService.confirmLoggedIn($state)) {
	$state.go('loggedin.main.text');
    }

    $scope.currentlyVisible = 'login';
    $scope.formSubmitted = false;
    $scope.formInvalid = false; 
    $scope.disableClientKey = false;
    $scope.clientKeyAutofilled = false;
    $scope.rememberMeVal = true;

    $scope.processClientKey = function(ck) {
	if (ck.length) {
/*
		switch (ck) {
			case 'glt':
				$scope.clientKeyAutofilled = true;
				return ck;
			default: 
				return '';
		}
*/
		$scope.clientKeyAutofilled = true;
		return ck;
	}
    };

    $scope.clientKey = $scope.processClientKey(location.getSubdomain());

    $(document).ready(function() {
	$('input').on('keydown', function() {
		$('input').trigger('input');
	});
    });

    $scope.makeVisible = function(elemName) {
	    $scope.currentlyVisible = elemName;
    };

    $scope.submit = function() {
	$scope.formSubmitted = true;
	$scope.logIn($scope.clientKey, $scope.username);
    };

    $scope.isFormSubmitted = function() {
	    return $scope.formSubmitted;
    };

    $scope.isFormInvalid = function() {
	return $scope.formInvalid;
    };

    $scope.isVisible = function(elemName) {
	    return $scope.currentlyVisible === elemName;
    };

    $scope.verifyResetCode = function() {
	    $scope.makeVisible('forgot-password-step-3');
    };

    $scope.logIn = function() {
        Restangular.one('login').one($scope.clientKey).one($scope.username).post("", $scope.password, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
            if (result.payload.success) {
		logInService.logIn($scope.clientKey, $scope.username, $scope.password, $scope.rememberMeVal);

	    	/* This redirection should be improved later */
		$state.go('loggedin.main.text');
	    	/* */
		$scope.formInvalid = false;
	    }
	    else {
		$scope.formInvalid = true;
	    }

            return logInService.isLoggedIn();
        });
    };
});


}());
