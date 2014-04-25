(function () {
   'use strict';

ironholdApp.controller('PaginationController', function ($http, $resource, $window, $rootScope, $scope, $timeout) {
    $scope.currentPage = 1;
    $scope.showPagination = false;
    $scope.pageSize = 20;
    $scope.totalPages = 0;
 
    $rootScope.$on('reset', function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.reset();
        }
    });

    $scope.reset = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.showPagination = false;
            $scope.currentPage = 1;
	}
    };

    $rootScope.$on('searchPreviewData', function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.reset();	
	}
    });

    $rootScope.$on('facetToggled', function() {
	$scope.goTo('1');
    });

    $scope.editPageNumber = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.totalPages = Math.ceil($scope.searchMatches / $scope.pageSize);
            $scope.editingPageNumber = true;
            $timeout(function() {
                $('.editable-page-number').focus();
            }, 10);
        }
    };

    $rootScope.$on('activeTab', function(evt, tab) {
        if ($scope.activeTab === $scope.tabName) {
                $scope.tabName = tab;
        }
    });

    $rootScope.$on('search', function(evt, info) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = info.inputSearch;
        }
    });

    $rootScope.$on('updateSearchbar', function(evt, info) {
        if ($scope.activeTab === $scope.tabName) {
	    if (info) {
		    if (info.searchMatches > 0) {
			$scope.totalPages = Math.ceil($scope.searchMatches / $scope.pageSize);
			$scope.showPagination = true;
		    }
	    }
        }
    });

    $scope.goTo = function(page) {
        if ($scope.activeTab === $scope.tabName) {
            page = parseInt(page);
            if (page > 0) {
            $scope.totalPages = Math.ceil($scope.searchMatches / $scope.pageSize);
                if (page <= $scope.totalPages) {
                    $scope.currentPage = page;
                    $scope.$emit('pageChange', {
                    page: page-1,
                    inputSearch: $scope.inputSearch
                    });
                    $scope.editingPageNumber = false;
                }
                else {
                $scope.$emit('error', 'Specified value is out of range. Search results contain a total of ' + $scope.totalPages + ' pages.');
                }
            }
            $scope.newPage = null;
        }
    };
});


}());
