(function () {
   'use strict';

ironholdApp.controller('ExternalSubmitController', function ($rootScope, $scope) {
	$rootScope.$on('formValidate', function(evt, obj) {
		if ($scope.activeTab === $scope.tabName) {
			obj.callback($scope[obj.formName].$valid);
		}
	});
});


}());
