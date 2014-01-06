'use strict';

ironholdApp.controller('LoginController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, $state, logInService) {
    $scope.currentlyVisible = 'login';
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

    $scope.logIn = function() {
        restMessagesService.one($scope.clientKey).one($scope.username).post("", $scope.password, {"Accept": "application/json", "Content-type" : "application/json"}).then(function(result) {
            if (result.payload.success) {
		$rootScope.logIn();
		$state.go('main.discovery');
	    }
	    /* This redirection after login should be improved later */
	    // $rootScope.confirmLoggedIn($state);
	    /* */

            return $rootScope.isLoggedIn();
        });
    }

    $scope.logOut = function() {
        $scope.loggedIn = false;
    }
});
