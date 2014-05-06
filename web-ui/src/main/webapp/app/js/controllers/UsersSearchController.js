(function () {
   'use strict';

ironholdApp.controller('UsersSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService) {
    logInService.confirmLoggedIn($state);

    $scope.users = [];
    $scope.pageNum = 0;
    $scope.selectedPsts = [];
    $scope.psts = [];
    $scope.newUser = {
	loginUser: {
		'mainRecipient': {}, 
		'recipients': []
	},
	roles: []
    };
    $scope.currentUser = false;

    $scope.userView = function() {
	$state.go('loggedin.main.userview');
    };

    $scope.toggleRoleName = function(evt, role, user) {
	if ($scope.activeTab === $scope.tabName) {
		var checkbox = evt.target;

		if (checkbox.checked) {
			if (role === 'NONE') {
				user.roles = [];
				user.loginUser.rolesBitMask = 0;
			}
			else {
				user.roles.push(role);
				user.loginUser.rolesBitMask = user.loginUser.rolesBitMask | $scope.allRoles[role];
			}
		}
		else {
			if (role !== 'NONE') {
				angular.forEach(user.roles, function(roleVal, roleKey) {
					if (roleVal === role) {
						user.roles.splice(roleKey, 1);
						user.loginUser.rolesBitMask = user.loginUser.rolesBitMask & ~$scope.allRoles[role];
					}
					if (roleVal === 'SUPER_USER') {
						user.roles.splice(roleKey, 1);
						user.loginUser.rolesBitMask = user.loginUser.rolesBitMask & ~$scope.allRoles['SUPER_USER'];
					}
				});
			}
			else {
				user.roles.push('SUPER_USER');
				user.loginUser.rolesBitMask = user.loginUser.rolesBitMask | $scope.allRoles['SUPER_USER'];
			}
		}
	}
    };

    $scope.toggleRoleBitmask = function(evt, role, user) {
	if ($scope.activeTab === $scope.tabName) {
		var checkbox = evt.target;

		if (checkbox.checked) {
			user.rolesBitMask = $scope.currentUser.loginUser.rolesBitMask | $scope.allRoles[role];
		}
		else {
			user.rolesBitMask = $scope.currentUser.loginUser.rolesBitMask & ~$scope.allRoles[role];
		}
	}
    };

    $scope.hasRoleBitmask = function(role, user) {
	if ($scope.activeTab === $scope.tabName) {
		return (user.rolesBitMask & $scope.allRoles[role]) === $scope.allRoles[role];
	}
    };

    $scope.hasRoleName = function(role, user) {
	if ($scope.activeTab === $scope.tabName) {
		if (role === 'NONE') {
			return (user.roles.length === 0);
		}
		else {
			return (user.roles.indexOf(role) !== -1 || user.roles.indexOf('SUPER_USER') !== -1);
		}
	}
    };

    $rootScope.$on('submitUser', function(evt, user) {
	if ($scope.activeTab === $scope.tabName) {
		if (user.confirmedPassword === user.hashedPassword) {
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
				.then(function() {
					$scope.userView();
				});
		}
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
