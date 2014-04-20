(function () {
   'use strict';

ironholdApp.controller('UsersSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService) {
    logInService.confirmLoggedIn($state);

    $scope.users = [];
    $scope.pageNum = 0;

    $rootScope.$on('submitUser', function(evt, user) {
	if ($scope.activeTab === $scope.tabName) {
		usersService
//			.one(user.username)
			.post(user.username, user)
			.then(function(result) {
				console.log(result);
			});
	}
    });

    $rootScope.$on('activeTab', function(evt, tab) {
	if (tab === $scope.tabName) {
	    $scope.initCustomScrollbars('.scrollbar-hidden');
	
	    if ($scope.users.length === 0) {
		    usersService.get({
			    criteria: '*',
			    page: $scope.pageNum,
			    pageSize: $scope.pageSize
		    })
		    .then(function(result) {
				$scope.users = result.payload;
				$scope.initCustomScrollbars('.scrollbar-hidden');
				$scope.initialized();

				$scope.$emit('results', {
				'resultEntries': $scope.users
				});
		    },
		    function(err) {
			$scope.$emit('technicalError', err);
		    });
	    }
	}
    });

    $rootScope.$on('activeTab', function(evt, tab) {
	    if (tab === $scope.tabName) {
		    $scope.onTabActivation();
	    }
    });

    $scope.onTabActivation = function() {
		    searchResultsService.prepForBroadcast("-", "- ");

		    if (usersService) {
			    usersService.one("searchHistory").get().then(function(result) {
				$scope.$emit('searchHistoryData', result);
			    }, function(err) {
				$scope.$emit('technicalError', err);
			    });
		    }
    };

    $scope.onTabActivation();

    $scope.initialized = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.initialState = false;
        }
    };

    $rootScope.$on('reset', function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.initialState = true;
        }
    });

    $rootScope.$on('selectResultRequest', function(evt, username) {
        if ($scope.activeTab === $scope.tabName) {
            usersService
		.one(username)
		.get()
		.then(function(result) {
			$scope.$emit('selectResultData', result.payload);
			$scope.$emit('selectUser', result.payload);
                },
		function(err) {
			$scope.$emit('technicalError', err);
		});
        }
    });

    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = args.inputSearch;
            usersService.get({
		    criteria: args.inputSearch,
		    page: $scope.pageNum,
		    pageSize: $scope.pageSize
            })
            .then(function(result) {
                        $scope.users = result.payload;
                        $scope.initCustomScrollbars('.scrollbar-hidden');
                        $scope.initialized();

                        $scope.$emit('results', {
                        'resultEntries': $scope.users
                        });
            },
            function(err) {
                $scope.$emit('technicalError', err);
            });
        }
    });
});


}());
