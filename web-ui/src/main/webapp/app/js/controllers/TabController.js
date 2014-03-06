'use strict';

ironholdApp.controller('TabController', function ($http, $resource, $window, $rootScope, $scope, $location, $state, logInService) {
    logInService.confirmLoggedIn($state);
    $scope.activeTab = 'search';
    $scope.tabsList = ['search', 'discovery', 'folders', 'logs', 'users', 'settings'];

    $scope.getClass = function (path) {
/*
        if ($location.path().endsWith(path) || $location.path().indexOf(path) > 0) {
            return true
        } else {
            return false;
        }
*/
	if ($scope.isActiveTab(path)) {
	    return true;
	}
	else {
	    return false;
	}
    }

    $scope.setActiveTab = function(tab) {
	$scope.activeTab = tab;
    }

    $scope.isActiveTab = function(tab) {
	return $scope.activeTab == tab;
    }
});
