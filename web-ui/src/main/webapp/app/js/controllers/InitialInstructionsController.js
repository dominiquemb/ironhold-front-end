(function () {
   'use strict';

ironholdApp.controller('InitialInstructionsController', function ($http, $resource, $window, $rootScope, $scope) {
    $scope.removeInstructions = false;

    $rootScope.$on('removeInstructions', function() {
	$scope.removeInstructions = true;
    });
});


}());
