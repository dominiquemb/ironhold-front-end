'use strict';

ironholdApp.controller('TabController', function ($http, $resource, $window, $rootScope, $scope, $location) {

    $scope.getClass = function (path) {
        if ($location.path().endsWith(path) || $location.path().indexOf(path) > 0) {
            return true
        } else {
            return false;
        }
    }


});