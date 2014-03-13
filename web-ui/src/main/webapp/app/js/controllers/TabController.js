'use strict';

ironholdApp.controller('TabController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, $state, logInService) {
    logInService.confirmLoggedIn($state);
    $scope.activeTab = 'search';
    $scope.pageSize = 20;
    $scope.showPagination = false;

    $scope.$emit('activeTab', $scope.activeTab);

    window.onresize = function(){
        if ($scope.scrollbars) {
                $scope.reinitScrollbars();
                $scope.$apply();
        }
    }

    $scope.$watch(function() {
                if ($('.msgview_middle .jspPane').length > 0) {
                        return $('.msgview_middle .jspPane').height();
                }
                else return 0;
        },
        function(newval, oldval) {
                if (newval !== oldval) {
                        if ($('.msgview_middle .jspPane').length > 0) {
                                $scope.reinitScrollbars();
                        }
                }
     });

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
                $('.scrollbar-hidden').eq(key).data('jsp').reinitialise();
        });
    }

    $scope.initCustomScrollbars = function(selector) {
        $scope.scrollbars = true;
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split',
                        showArrows: true
                });

                $('.filter-list .jspContainer').mouseenter(function(){
                    $(this).find('.jspVerticalBar, .jspHorizontalBar').animate({opacity:1}, 400);
                });

                $('.filter-list .jspContainer').mouseleave(function(){
                    $(this).find('.jspVerticalBar, .jspHorizontalbar').animate({opacity:0}, 400);
                });
        }, 0);
    }

    $rootScope.$on('modeRequest', function(evt, data) {
        messagesService
                .one(data.messageId)
                .one(data.mode)
                .get(data.criteria)
                .then(function(result) {
                        $scope.$emit('modeData', {
                                mode: data.mode,
                                payload: result.payload
                        });
                });
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
    }

    $scope.setActiveTab = function(tab) {
	$scope.activeTab = tab;
    	$scope.$emit('activeTab', $scope.activeTab);
    }

    $scope.isActiveTab = function(tab) {
	return $scope.activeTab == tab;
    }
});
