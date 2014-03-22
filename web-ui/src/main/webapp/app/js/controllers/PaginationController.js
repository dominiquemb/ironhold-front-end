'use strict';

ironholdApp.controller('PaginationController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state) {
    $scope.currentPage = 1;
    $scope.showPagination = false;
    $scope.totalPages;
 
    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showPagination = false;
		$scope.currentPage = 1;
	}
    });

    $scope.editPageNumber = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.totalPages = Math.ceil($scope.searchMatches / $scope.pageSize);
		$scope.editingPageNumber = true;
		setTimeout(function() {
			$('.editable-page-number').focus()
		}, 10);
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
		$scope.totalPages = Math.ceil($scope.searchMatches / $scope.pageSize);
		    if (page <= $scope.totalPages) { 
			    $scope.currentPage = page;
			    $scope.$emit('pageChange', {
				page: page,
				inputSearch: $scope.inputSearch
			    });
			    $scope.editingPageNumber = false;
		    }
		    else {
			$scope.$emit('error', 'Specified value is out of range');
		    }
		}
		$scope.newPage = null;
	}
    }
});
