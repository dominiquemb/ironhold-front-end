'use strict';

ironholdApp.controller('PaginationController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state) {
    $scope.currentPage = 1;

    $scope.goTo = function(page) {
        if (page > 0) {
            $scope.currentPage = page;
            $scope.$emit('pageChange', page);
        }
    }
});
