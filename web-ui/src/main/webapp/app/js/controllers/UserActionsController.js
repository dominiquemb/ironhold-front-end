'use strict';

ironholdApp.controller('UserActionsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.clickLogOut = function() {
	logInService.logOut();
	if (logInService.confirmLoggedIn($state) === false) {
		$state.go('login');
	}
    };
});
