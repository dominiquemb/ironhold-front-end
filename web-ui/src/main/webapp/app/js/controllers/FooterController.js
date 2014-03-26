'use strict';

ironholdApp.controller('FooterController', function ($http, $resource, $window, $rootScope, $scope, $location, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.searchMatches;
    $scope.selectedFacets;
    $scope.searchTime;
    $scope.archiveTotal;
    $scope.showFooterSearchStats = false;
    $scope.showAfterFilter = false;

    $scope.$watch(messagesService, function(newval, oldval) {
	if (newval) {
	    if ($scope.activeTab === $scope.tabName) {
		messagesService.one("count").get({criteria: '*'}).then(function(result) {
			$scope.archiveTotal = result.payload.matches;
		});
	    }
	}
    });

    $rootScope.$on('reset', function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.showFooterSearchStats = false;
		$scope.searchMatches = 0;
		$scope.selectedFacets = [];
		$scope.searchTime = 0;
		$scope.showAfterFilter = false;
	}
    });

    $rootScope.$on('updateFooter', function(evt, results) {
        if ($scope.activeTab === $scope.tabName) {
		var newFilters = (results.selectedFacets === $scope.selectedFacets) ? true : false;
		
		$scope.showFooterSearchStats = true;

		if ($scope.searchMatches > 0 && newFilters && results.selectedFacets.length > 0) {
			$scope.afterFilter = results.searchMatches;
			$scope.showAfterFilter = true;
		}
		else {
			$scope.searchMatches = results.searchMatches;
			$scope.showAfterFilter = false;
		}
		$scope.searchTime = results.searchTime;
	}
    });

});
