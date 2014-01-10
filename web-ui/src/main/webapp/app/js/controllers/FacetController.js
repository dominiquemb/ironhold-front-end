'use strict';

ironholdApp.controller('FacetController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFacets = [];
    $scope.facets = [];

    $scope.unselectAllFacets = function() {
        angular.forEach($scope.selectedFacets, function(facet) {
            facet.selected = false;
        });
        $scope.selectedFacets = [];
    }

    $scope.toggleFacet = function(facet, facetGroupCode) {
	facet.selected = !facet.selected;
        if (facet.selected) {
            $scope.selectedFacets.push(facet);
        } else {
            $scope.selectedFacets.remove(facet);
        }

	$scope.$emit('facetToggled', facet);
    }

    $rootScope.$on('facets', function(evt, facetList) {
	$scope.facets = facetList;
    });
});
