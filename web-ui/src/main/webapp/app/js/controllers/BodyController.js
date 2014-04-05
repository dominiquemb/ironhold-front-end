(function () {
   'use strict';


ironholdApp.controller('BodyController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    $scope.isLoggedIn = false;

    if (logInService.confirmLoggedIn($state)) {
	$scope.isLoggedIn = true;
    }
});


}());