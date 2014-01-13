'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = true;
    $scope.currentMessage = false;

    $rootScope.$on('selectMessage', function(evt, message) {
	$scope.currentMessage = message;
    });

});
