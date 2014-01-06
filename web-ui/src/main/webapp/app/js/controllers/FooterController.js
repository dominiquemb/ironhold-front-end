'use strict';

ironholdApp.controller('FooterController', function ($http, $resource, $window, $rootScope, $scope, $location, Restangular, searchResultsService, $state, logInService) {
    $rootScope.confirmLoggedIn($state);

    var restMessagesService = Restangular.setBaseUrl('http://localhost:8080/messages');

    restMessagesService.one("demo","count").get().then(function(result) {
        $scope.totalMessages = result.payload.matches;
    });

    $scope.$on('handleSearchResultsChange', function() {
        $scope.searchMatches = searchResultsService.searchMatches;
        $scope.searchTime = searchResultsService.searchTime;
    });



});
