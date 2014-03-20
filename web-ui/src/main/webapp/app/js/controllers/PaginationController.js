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

    $scope.editPageNumber = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.editingPageNumber = true;
	}
    };

    $rootScope.$on('activeTab', function(evt, tab) {
	if ($scope.activeTab === $scope.tabName) {
        	$scope.tabName = tab;
	}
    });

    $rootScope.$on('search', function(evt, info) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = info.inputSearch;
	}
    });

    $rootScope.$on('updateSearchbar', function(evt, info) {
	if ($scope.activeTab === $scope.tabName) {
		if (info.searchMatches > 0) {
			$scope.showPagination = true;
		}
	}
    });

    $scope.goTo = function(page) {
	if ($scope.activeTab === $scope.tabName) {
		page = parseInt(page);
		if (page > 0) {
		    $scope.currentPage = page;
		    $scope.$emit('pageChange', {
			page: page,
			inputSearch: $scope.inputSearch
		    });
		    $scope.editingPageNumber = false;
		}
	}
    }
});
