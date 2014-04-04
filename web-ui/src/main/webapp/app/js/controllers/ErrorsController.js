'use strict';

ironholdApp.controller('ErrorsController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state) {
    $scope.showErrors = false;
    $scope.showWarnings = false;
    $scope.errors = [];
    $scope.warnings = [];
 
    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.errors = [];
	    $scope.warnings = [];
	}
    });

    $rootScope.$on('error', function(evt, msg) {
	var id = $scope.errors.length;
	$scope.errors.push({
		id: id,
		message: msg
	});
	
	setTimeout(function() {
		$scope.clearError(id);
	}, 5000);
    });

    $scope.clearError = function(id) {
	$scope.errors.splice(id, 1);
    }

    $scope.clearWarning = function(id) {
	$scope.warnings.splice(id, 1);
    }

    $rootScope.$on('warning', function(evt, msg) {
	var id = $scope.warnings.length;
	$scope.warnings.push({
		id: id,
		message: msg
	});

	setTimeout(function() {
		$scope.clearWarning(id);
	}, 2000);
    });
});
