'use strict';

ironholdApp.controller('FacetController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFacets = [];
    $scope.facets = [];
    $scope.showFacets = false;

    $scope.unselectAllFacets = function() {
        angular.forEach($scope.selectedFacets, function(facet) {
            facet.selected = false;
        });
        $scope.selectedFacets = [];
    }

    $rootScope.$on('toggleFacet', function(evt, facet) {
	$scope.toggleFacet(facet);
    });

    $scope.toggleFacet = function(facet, facetGroupCode) {
console.log(facet.selected);
	facet.selected = !facet.selected;
        if (facet.selected) {
            $scope.selectedFacets.push(facet);
        } else {
            $scope.selectedFacets.remove(facet);
        }

	$scope.$emit('facetToggled', facet, $scope.selectedFacets);
    }

    $rootScope.$on('facets', function(evt, facetList) {
	$scope.facets = facetList;
	if (facetList.length > 0) {
		$scope.showFacets = true;
	}
    });
});
