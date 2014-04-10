(function () {
   'use strict';

ironholdApp.controller('UserActionsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);
    $scope.username = logInService.getUsername();
    $scope.clientKey = logInService.getClientKey();

    logInService.onLogOut(function() {
	$state.go('login');
    });

    $scope.clickLogOut = function() {
	logInService.logOut();
    };
});


}());