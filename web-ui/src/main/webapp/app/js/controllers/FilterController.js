(function () {
   'use strict';

ironholdApp.controller('FilterController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFilters = [];
    $scope.filters = [];
    $scope.showFilters = false;

    $rootScope.$on('reset', function() {
	    $scope.selectedFilters = [];
	    $scope.filters = [];
	    $scope.showFilters = false;
    });

    $scope.unselectAllFilters = function() {
        if ($scope.activeTab === $scope.tabName) {
            angular.forEach($scope.selectedFilters, function(filter) {
                filter.selected = false;
            });
            $scope.selectedFilters = [];
            $scope.$emit('facetToggled', null, $scope.selectedFilters);
        }
    };

    $scope.enableFilter = function(filter) {
        if ($scope.activeTab === $scope.tabName) {
            if (filter.selected) {
                $scope.selectedFilters.push(filter);
            } else {
                $scope.selectedFilters.remove(filter);
            }

            $scope.$emit('filterToggled', filter);
        }
    };

    $scope.disableFilter = function(filter) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.$emit('toggleFacet', filter);
        }
    };

    $rootScope.$on('facetToggled', function(evt, facet) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.enableFilter(facet);
        }
    });

    $rootScope.$on('filters', function(evt, filterList) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.filters = filterList;
            if (filterList.length > 0) {
                $scope.showFilters = true;
            }
        }
    });
});


}());