(function () {
   'use strict';

ironholdApp.controller('UsersSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService) {
    logInService.confirmLoggedIn($state);

    $scope.users = [];
    $scope.pageNum = 0;
    $scope.selectedPsts = [];
    $scope.psts = [];
    $scope.currentUser = false;

    $scope.userView = function() {
	$state.go('loggedin.main.userview');
console.log($scope.roles);
    };

    $scope.toggleRole = function(evt, role) {
	if ($scope.activeTab === $scope.tabName) {
		var checkbox = evt.target;

		if (checkbox.checked) {
			$scope.currentUser.loginUser.bitMask = $scope.currentUser.loginUser.bitMask | $scope.roles[role];
		}
		else {
			$scope.currentUser.loginUser.bitMask = $scope.currentUser.loginUser.bitMask & ~$scope.roles[role];
		}
	}
    };
/*
    $scope.hasRole = function(role) {
	if ($scope.activeTab === $scope.tabName) {
		return (currentUser.loginUser.bitMask & $scope.roles[role]) === $scope.roles[role];
	}
    };
*/
    $rootScope.$on('submitUser', function(evt, user) {
	if ($scope.activeTab === $scope.tabName) {
		user.sources = $scope.selectedPsts;
		usersService
			.post(
				"",
				user,
				{
				"Accept": "application/json",
				"Content-type" : "application/json"
				}
			)
			.then(function(result) {
				console.log(result);
			});
	}
    });

    $rootScope.$on('searchHistoryRequest', function(evt, data) {
	if ($scope.activeTab === $scope.tabName) {
		usersService
			.one('searchHistory')
			.one(data.loginUser.username)
			.get()
			.then(function(result) {
				$scope.searchHistory = result.payload;
			});
	}
    });

    $rootScope.$on('pstRequest', function(evt, data) {
	if ($scope.activeTab === $scope.tabName) {
		usersService
			.one('psts')
			.get({
				'criteria': data.criteria,
				'page': data.page,
				'pageSize': data.pageSize
				})
			.then(function(result) {
				$scope.psts = result.payload || [];
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

    $scope.togglePst = function(evt, pst) {
	if ($scope.activeTab === $scope.tabName) {
		var p = null,
		checkbox = evt.target;

		if (checkbox.checked) {
			$scope.selectedPsts.push(pst);
		}
		else {
			for (p in $scope.selectedPsts) {
				if ($scope.selectedPsts[p].id === pst.id) {
					$scope.selectedPsts.splice(p, 1);
				}
			}	
		}
	}
    };

    $scope.isPstSelected = function(id) {
	if ($scope.activeTab === $scope.tabName) {
		var pst;
		for (pst in $scope.selectedPsts) {
			if ($scope.selectedPsts[pst].id === id) {
				return true;
			}
		}
		return false;
	}
    };

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
	    $scope.psts = [];
	    $scope.users = [];
	    $scope.pageNum = 0;
        }
    });

    $rootScope.$on('selectResultRequest', function(evt, user) {
        if ($scope.activeTab === $scope.tabName) {
            usersService
		.one(user.loginUser.username)
		.get()
		.then(function(result) {
			$scope.$emit('selectResultData', result.payload);
			$scope.$emit('selectUser', result.payload);
			$scope.selectedPsts = result.payload.loginUser.sources || [];
			$scope.currentUser = user;
			$scope.$emit('searchHistoryRequest', user);
			$scope.$emit('pstRequest', {
				'criteria': '*',
				'page': 1,
				'pageSize': 100
			});
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
	
			$scope.$emit('updateSearchbar');
            },
            function(err) {
                $scope.$emit('technicalError', err);
            });
        }
    });
});


}());
