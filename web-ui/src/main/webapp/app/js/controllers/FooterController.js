'use strict';

ironholdApp.controller('FooterController', function ($http, $resource, $window, $rootScope, $scope, $location, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    $rootScope.$on('totalResultsChange', function(evt, result) {
	$scope.totalMessages = result;
    });

    $rootScope.$on('updateFooter', function(evt, results) {
        $scope.searchMatches = results.searchMatches;
        $scope.searchTime = results.searchTime;
    });



});
