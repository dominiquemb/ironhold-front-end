(function () {
   'use strict';

ironholdApp.controller('ErrorsController', function ($http, $resource, $window, $rootScope, $scope) {
    $scope.showErrors = false;
    $scope.showWarnings = false;
    $scope.errors = [];
    $scope.warnings = [];
    $scope.techErrors = [];
 
    $scope.showErrorDetails = function(err) {
	err.showErrorPopup = true;	
    };

    $scope.closeErrorDetails = function(err) {
	err.showErrorPopup = false;
    };

    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.errors = [];
	    $scope.warnings = [];
	}
    });

    $rootScope.$on('technicalError', function(err) {
	if ($scope.activeTab === $scope.tabName) {
		var id = $scope.techErrors.length;
		
		$scope.techErrors.push({
			id: id,
			message: err
		});

		setTimeout(function() {
			$scope.clearError(id);
		}, 5000);
	}
    });

    $rootScope.$on('error', function(evt, msg) {
	if ($scope.activeTab === $scope.tabName) {
		var id = $scope.errors.length;
		$scope.errors.push({
			id: id,
			message: msg
		});
		
		setTimeout(function() {
			$scope.clearError(id);
		}, 5000);
	}
    });

    $scope.clearError = function(id) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.errors.splice(id, 1);
	}
    };

    $scope.clearWarning = function(id) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.warnings.splice(id, 1);
	}
    };

    $rootScope.$on('warning', function(evt, msg) {
	if ($scope.activeTab === $scope.tabName) {
		var id = $scope.warnings.length;
		$scope.warnings.push({
			id: id,
			message: msg
		});

		setTimeout(function() {
			$scope.clearWarning(id);
		}, 2000);
	}
    });
});


}());
