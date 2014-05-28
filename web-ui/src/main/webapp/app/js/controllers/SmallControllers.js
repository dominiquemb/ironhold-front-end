(function () {
   'use strict';

ironholdApp.controller('ExternalSubmitController', function ($rootScope, $scope) {
	$scope.formInvalid = false;
	$rootScope.$on('formValidate', function(evt, obj) {
		if ($scope.activeTab === $scope.tabName) {
			var form = $scope[obj.formName];
			if (!form.$valid) {
				$scope.formInvalid = true;
			}
			obj.callback($scope[obj.formName].$valid);
		}
	});
});


}());
