'use strict';

ironholdApp.controller('FilterController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFilters = [];
    $scope.filters = [];
    $scope.showFilters = false;

    $scope.unselectAllFilters = function() {
        angular.forEach($scope.selectedFilters, function(filter) {
            filter.selected = false;
	    $scope.$emit('toggleFacet', filter);
        });
	$scope.selectedFilters = [];
    }

    $scope.enableFilter = function(filter, filterGroupCode) {
	if (filter.selected) {
	        $scope.selectedFilters.push(filter);
        } else {
            $scope.selectedFilters.remove(filter);
        }

	$scope.$emit('filterToggled', filter);
    }

    $scope.disableFilter = function(filter) {
	$scope.$emit('toggleFacet', filter);	
    }

    $rootScope.$on('facetToggled', function(evt, facet) {
	$scope.enableFilter(facet);
    });

    $rootScope.$on('filters', function(evt, filterList) {
	$scope.filters = filterList;
	if (filterList.length > 0) {
		$scope.showFilters = true;
	}
    });
});
