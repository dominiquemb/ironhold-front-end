'use strict';

ironholdApp.controller('UserActionsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    logInService.onLogOut(function() {
	$state.go('login');
    });

    $scope.clickLogOut = function() {
	logInService.logOut();
    };
});
