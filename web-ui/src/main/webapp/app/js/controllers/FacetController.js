'use strict';

ironholdApp.controller('FacetController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFacets = [];
    $scope.facets = [];
    $scope.showFacets = false;

    $scope.unselectAllFacets = function() {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach($scope.selectedFacets, function(facet) {
		    facet.selected = false;
		});
		$scope.selectedFacets = [];
	}
    }

    $rootScope.$on('toggleFacet', function(evt, facet) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.toggleFacet(facet);
	}
    });

    $scope.toggleFacet = function(facet, facetGroupCode) {
	if ($scope.activeTab === $scope.tabName) {
		facet.selected = !facet.selected;
		if (facet.selected) {
		    $scope.selectedFacets.push(facet);
		} else {
		    $scope.selectedFacets.remove(facet);
		}

		$scope.$emit('facetToggled', facet, $scope.selectedFacets);
		$scope.$emit('reinitScrollbars');
	}
    }

    $rootScope.$on('facets', function(evt, facetList) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.facets = facetList;
		if (facetList.length > 0) {
			$scope.showFacets = true;
		}
	}
    });
});
