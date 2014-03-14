'use strict';

ironholdApp.controller('FooterController', function ($http, $resource, $window, $rootScope, $scope, $location, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.searchMatches;
    $scope.selectedFacets;
    $scope.searchTime;

    $rootScope.$on('totalResultsChange', function(evt, result) {
	$scope.totalMessages = result;
    });

    $rootScope.$on('updateFooter', function(evt, results) {
	var newFilters = (results.selectedFacets === $scope.selectedFacets) ? false : true;
	if ($scope.searchMatches > 0 && newFilters) {
		$scope.afterFilter = results.searchMatches;
	}
	else {
	        $scope.searchMatches = results.searchMatches;
	}
        $scope.searchTime = results.searchTime;
    });



});
