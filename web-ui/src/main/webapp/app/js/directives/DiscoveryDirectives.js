'use strict';
/*
ironholdApp.directive('msgviewMain', function() {
	return {
		restrict: 'C',
		link: function($scope, $elem, $attrs) {
			$scope.$watch(function() { 
				return ($('.msgview_bottom').height());
			}, function(oldVal, newVal) {
				if (oldVal !== newVal) {
					var attachmentsHeight = angular.element('.msgview_bottom').height();
					angular.element('.msgview_middle').css('bottom', attachmentsHeight + 'px' ).height( angular.element('.msgview_middle').height() );
					$scope.initCustomScrollbars('.msgview_middle');	
				}
			});
		}
	}
});*/
