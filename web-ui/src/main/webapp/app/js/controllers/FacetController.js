(function () {
   'use strict';

ironholdApp.controller('FacetController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);

    $scope.selectedFacets = [];
    $scope.facets = [];
    $scope.showFacets = false;

    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.selectedFacets = [];
	    $scope.facets = [];
	    $scope.showFacets = false;
	}
    });	

    $scope.unselectAllFacets = function() {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach($scope.selectedFacets, function(facet) {
		    facet.selected = false;
		});
		$scope.selectedFacets = [];
	}
    };

    $scope.collapseFacet = function(facet) {
	if ($scope.activeTab === $scope.tabName) {
		facet.isCollapsed = !facet.isCollapsed;
	}
    };

    $scope.isCollapsed = function(facet) {
	if ($scope.activeTab === $scope.tabName) {
		return facet.isCollapsed;
	}
    };

    $rootScope.$on('toggleFacet', function(evt, facet) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.toggleFacet(facet);
	}
    });

    $scope.toggleFacet = function(facet) {
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
    };

    $rootScope.$on('facets', function(evt, data) {
	if ($scope.activeTab === $scope.tabName) {
		var facet;
		for (facet in data.facets) {
			data.facets[facet].isCollapsed = false;
		}
		$scope.facets = data.facets;
		if (data.facets.length > 0 && data.matches > 0) {
			$scope.showFacets = true;
		}
	}
    });
});


}());
