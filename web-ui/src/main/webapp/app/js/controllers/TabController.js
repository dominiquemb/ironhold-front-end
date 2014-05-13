(function () {
   'use strict';

ironholdApp.controller('TabController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, $state, logInService, usersService, messagesService, rolesService) {
    logInService.confirmLoggedIn($state);
    $scope.activeTab = 'search';
    $scope.pageSize = 20;
    $scope.roles = [];
    $scope.allRoles = {};
    $scope.disableTechErrors = true;

console.log(rolesService);

    rolesService.getUserRoles(function(result) {
	$scope.roles = result;
    });

    rolesService.getAllRoles(function(result) {
	$scope.allRoles = result;
    });

    $scope.$emit('activeTab', $scope.activeTab);

    $(window).on('beforeunload', function() {
	$(window).scrollTop(0);
    });

    window.onresize = function(){
        if ($scope.scrollbars) {
                $scope.reinitScrollbars();
		$scope.$emit('pageResized');
                $scope.$apply();
        }
    };

    $scope.isState = function(state) {
	return $state.current.name.indexOf(state) !== -1;
    };

    $scope.hasRole = function(role) {
	return ($scope.roles.indexOf(role) !== -1 || $scope.roles.indexOf('SUPER_USER') !== -1);
    };

    $scope.clickEvent = function() {
	$scope.$emit('clickEvent');
    };

    $scope.evalKey = function(evt) {
	if (evt.which === 40) {
		$scope.$emit('selectBelowMessage');	
	}
	else if (evt.which === 38) {
		$scope.$emit('selectAboveMessage');
	}
    };

    $rootScope.$on('pageChange', function(evt, info) {
        $scope.currentPage = info.page;
        $scope.$emit('updateSearch', {
                inputSearch: info.inputSearch
        });
        $scope.reinitScrollbars();
    });

    $rootScope.$on('reinitScrollbars', function() {
	$scope.reinitScrollbars();
    });

    $scope.reinitScrollbars = function() {
        angular.forEach($('.scrollbar-hidden'), function(container, key) {
            try {
                $('.scrollbar-hidden').eq(key).data('jsp').reinitialise();
            } catch(err) {}
        });
    };

    $rootScope.$on('initCustomScrollbars', function(evt, selector) {
	$scope.initCustomScrollbars(selector);
    });

    $scope.initCustomScrollbars = function(selector) {
        $scope.scrollbars = true;
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split',
                        showArrows: true
                });
        }, 0);
    };

    $rootScope.$on('modeRequest', function(evt, data) {
	if (data.mode === 'headers' || data.mode === 'body') {
		messagesService
			.one(data.date.getFullYear())
			.one(data.date.getMonth() + 1)
			.one(data.date.getDate())
			.one(data.messageId)
			.one(data.mode)
			.get(data.criteria)
			.then(function(result) {
				$scope.$emit('modeData', {
					mode: data.mode,
					payload: result.payload
				});
			}, function(err) {
				$scope.$emit('technicalError', err);
				$scope.$emit('modeData', {
					mode: data.mode,
					error: err,
					payload: []
				});
			});
	}
	else {
		messagesService
			.one(data.messageId)
			.one(data.mode)
			.get(data.criteria)
			.then(function(result) {
				$scope.$emit('modeData', {
					mode: data.mode,
					payload: result.payload
				});
			}, function(err) {
				$scope.$emit('technicalError', err);
				$scope.$emit('modeData', {
					mode: data.mode,
					error: err,
					payload: []
				});
			});
	}
    });

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
    };

    $scope.setActiveTab = function(tab) {
	$scope.$emit('removeInstructions');
	$scope.activeTab = tab;
    	$scope.$emit('activeTab', $scope.activeTab);
    };

    $scope.isActiveTab = function(tab) {
	return $scope.activeTab === tab;
    };
});

}());
