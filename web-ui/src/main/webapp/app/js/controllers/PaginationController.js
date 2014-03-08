'use strict';

ironholdApp.controller('PaginationController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state) {
    $scope.currentPage = 1;

    $rootScope.$on('activeTab', function(evt, tab) {
        $scope.tabName = tab;
    });

    $scope.goTo = function(page) {
	if ($scope.activeTab === $scope.tabName) {
		if (page > 0) {
		    $scope.currentPage = page;
		    $scope.$emit('pageChange', page);
		}
	}
    }
});
