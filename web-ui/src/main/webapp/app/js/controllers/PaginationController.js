'use strict';

ironholdApp.controller('PaginationController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state) {
    $scope.currentPage = 1;
    $scope.showPagination = false;
 
    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showPagination = false;
		$scope.currentPage = 1;
	}
    });

    $rootScope.$on('results', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showPagination = true;
	}
    });

    $rootScope.$on('activeTab', function(evt, tab) {
	if ($scope.activeTab === $scope.tabName) {
        	$scope.tabName = tab;
	}
    });

    $scope.$on('search', function(evt, info) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showPagination = true;
		$scope.inputSearch = info.inputSearch;
	}
    });

    $scope.goTo = function(page) {
	if ($scope.activeTab === $scope.tabName) {
		if (page > 0) {
		    $scope.currentPage = page;
		    $scope.$emit('pageChange', {
			page: page,
			inputSearch: $scope.inputSearch
		    });
		}
	}
    }
});
