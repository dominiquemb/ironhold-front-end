(function () {
   'use strict';

ironholdApp.controller('ErrorsController', function ($http, $resource, $window, $rootScope, $scope, $modal) {
    $scope.showErrors = false;
    $scope.showWarnings = false;
    $scope.errors = [];
    $scope.warnings = [];
    $scope.techErrors = [];
    $scope.modalError = false;
    $scope.modal = false;
 
    $scope.showErrorDetails = function(err) {
	$scope.modalError = err;
	$scope.modal = $modal.open({
		templateUrl: 'views/Modals/TechError.html',
		scope: $scope
	});
    };

    $scope.closeErrorDetails = function() {
	$scope.modal.close();
    };

    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.errors = [];
	    $scope.warnings = [];
	}
    });

    $rootScope.$on('technicalError', function(evt, err) {
	if ($scope.activeTab === $scope.tabName) {
		var id = $scope.techErrors.length;
		
		$scope.techErrors.push({
			id: id,
			message: err
		});

		$timeout(function() {
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
		
		$timeout(function() {
			$scope.clearError(id);
		}, 5000);
	}
    });

    $scope.clearError = function(id) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.errors.splice(id, 1);
	}
    };

    $scope.clearTechError = function(id) {
	if ($scope.activeTab === $scope.tabName) {
	    $scope.techErrors.splice(id, 1);
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

		$timeout(function() {
			$scope.clearWarning(id);
		}, 2000);
	}
    });
});


}());
