'use strict';

ironholdApp.controller('FooterController', function ($http, $resource, $window, $rootScope, $scope, $location, searchResultsService) {

    $scope.$on('handleSearchResultsChange', function() {
        $scope.searchMatches = searchResultsService.searchMatches;
        $scope.searchTime = searchResultsService.searchTime;
    });


});