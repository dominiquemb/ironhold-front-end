'use strict';

ironholdApp.controller('TabController', function ($http, $resource, $window, $rootScope, $scope, $location, $state, logInService) {
	$rootScope.confirmLoggedIn($state);
    $scope.getClass = function (path) {
        if ($location.path().endsWith(path) || $location.path().indexOf(path) > 0) {
            return true
        } else {
            return false;
        }
    }

    $scope.toggleCollapse = function(object, item) {
        return object[item] = !object[item];
    }

});
