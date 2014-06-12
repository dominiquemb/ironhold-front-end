(function () {
   'use strict';

ironholdApp.controller('UsersSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, searchHistoryService) {
    logInService.confirmLoggedIn($state);

    $scope.users = [];
    $scope.pageNum = 0;
    $scope.selectedPsts = [];
    $scope.editedUser = false;
    $scope.editingName = false;
    $scope.psts = [];
    $scope.newUserTemplate = {
	    loginUser: {
		    'mainRecipient': {},
		    'recipients': []
	    },
	    roles: []
    };
    $scope.backUpUser = false;
    $scope.currentUser = false;

    $rootScope.$on('activeTab', function(evt, tab) {
	if (tab === 'users') {
		$scope.requestUserList();
		$scope.userView();
	}
    });

    $scope.editName = function() {
	    $scope.editingName = true;
    };

    $scope.addUser = function() {
	    if ($scope.activeTab === $scope.tabName) {
		    $scope.newUser = $scope.newUserTemplate;
		    $scope.$emit('showMsgView', true);
		    $state.go('loggedin.main.useradd');
		    $scope.selectedPsts = [];
		    $scope.$emit('mode', 'useradd', false);
		    $scope.$emit('pstRequest', {
			    'criteria': '*',
			    'page': 0,
			    'pageSize': 100
		    });
	    }
    };

    $scope.userView = function() {
	$state.go('loggedin.main.userview');
	$scope.$emit('mode', 'userview', false);
    };	

    $scope.editUser = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.backUpUser = $scope.currentUser;
            $state.go('loggedin.main.useredit');
            $scope.$emit('mode', 'useredit', false);
            $scope.$emit('pstRequest', {
                    'criteria': '*',
                    'page': 0,
                    'pageSize': 100
            });
        }
    };

    $scope.getOtherEmails = function(user) {
	if (user) {
		var emails = '';
		angular.forEach(user.recipients, function(email) {
			if (emails.length > 0) {
				emails += ', ' + email.address;
			}
			else {
				emails += email.address;
			}
		});

		return emails;
	}
    };

    $scope.userCancel = function(noselect) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentUser = $scope.backUpUser;
		$scope.$emit('restoreBackUpUser', $scope.backUpUser);
		if (!noselect) {
			$scope.$emit('selectResultRequest', $scope.backUpUser);
		}
		$state.go('loggedin.main.userview');
	}	
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
		if (user.loginUser.confirmedPassword === user.loginUser.hashedPassword) {
			if (user.otherEmails) {
				if (user.otherEmails.length > 0) {
					user.loginUser.recipients = [];
					angular.forEach(user.otherEmails.trim().split(','), function(recipient) {
						user.loginUser.recipients.push({
							'name': recipient.split('@')[0],
							'address': recipient,
							'domain': recipient.split('@')[1]
						});
					});
				}
			}
			user.loginUser.sources = $scope.extractPstIds($scope.selectedPsts);
			usersService
				.post(
					"",
					user.loginUser,
					{"oldUsername": user.oldUsername},
					{
					"Accept": "application/json",
					"Content-type" : "application/json"
					}
				)
				.then(function(result) {
					$scope.editingName = false;
					if (result.toString() === "true") {
						$scope.$emit('selectResultRequest', user);
						$scope.userView();
						$scope.requestUserList();
					}
					else {
						$scope.$emit('error', 'That username is already in use. Please type a different username.');
					}
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

    $scope.getUnselectedPsts = function() {
	if ($scope.activeTab === $scope.tabName) {
		var unselected = [];
		angular.forEach($scope.psts, function(unselectedPst) {
			var found = false;
			angular.forEach($scope.selectedPsts, function(selectedPst, key) {
				if (unselectedPst.id == selectedPst.id) {
					found = true;
				}
			});
			if (!found) {
				unselected.push(unselectedPst);
			}
			found = false;
		});
		return unselected;
	}
    };

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
// Mock data to make debugging easier
/*
				$scope.psts = [{"id":"adf26bdf-4857-4ce5-b7b5-bc32a1088a7e","pstFileName":"aaaaa","mailBoxName":"Bes.Admin","originalFilePath":"\\\\GLTSRV17\\d$\\DATA_USR\\Mail Archive\\EMS Export\\OLD EXPORT\\EXARCHIVES\\BesAdmin","commentary":null,"md5":"eed76dd072b8596ebd16e561fbd611af","hostname":"IH650IM001","size":425984,"started":"06/02/2013","finished":"06/02/2013","messages":49,"duplicates":49,"failures":0,"partialFailures":0,"maxSize":0,"compressedMaxSize":0,"messagesWithAttachments":19,"messagesWithoutAttachments":30,"typeMap":{"PSTMessage":49},"folderMap":{"/Top of Personal Folders/Sent Items":27,"/Top of Personal Folders/Spam Mail/Approved Sender List":1,"/Top of Personal Folders/Inbox":21},"compressedAverageSize":0.0,"averageSize":0.0,"medianSize":0.0,"medianCompressedSize":0.0,"completed":true},
				{"id":"adf26bdf-4857-4ce5-b7b5-bc32a1088a7f","pstFileName":"bbbbbbbb","mailBoxName":"Bes.Admin","originalFilePath":"\\\\GLTSRV17\\d$\\DATA_USR\\Mail Archive\\EMS Export\\OLD EXPORT\\EXARCHIVES\\BesAdmin","commentary":null,"md5":"eed76dd072b8596ebd16e561fbd611af","hostname":"IH650IM001","size":425984,"started":"06/02/2013","finished":"06/02/2013","messages":49,"duplicates":49,"failures":0,"partialFailures":0,"maxSize":0,"compressedMaxSize":0,"messagesWithAttachments":19,"messagesWithoutAttachments":30,"typeMap":{"PSTMessage":49},"folderMap":{"/Top of Personal Folders/Sent Items":27,"/Top of Personal Folders/Spam Mail/Approved Sender List":1,"/Top of Personal Folders/Inbox":21},"compressedAverageSize":0.0,"averageSize":0.0,"medianSize":0.0,"medianCompressedSize":0.0,"completed":true},
				{"id":"adf26bdf-4857-4ce5-b7b5-bc32a1088a7g","pstFileName":"cccccc","mailBoxName":"Bes.Admin","originalFilePath":"\\\\GLTSRV17\\d$\\DATA_USR\\Mail Archive\\EMS Export\\OLD EXPORT\\EXARCHIVES\\BesAdmin","commentary":null,"md5":"eed76dd072b8596ebd16e561fbd611af","hostname":"IH650IM001","size":425984,"started":"06/02/2013","finished":"06/02/2013","messages":49,"duplicates":49,"failures":0,"partialFailures":0,"maxSize":0,"compressedMaxSize":0,"messagesWithAttachments":19,"messagesWithoutAttachments":30,"typeMap":{"PSTMessage":49},"folderMap":{"/Top of Personal Folders/Sent Items":27,"/Top of Personal Folders/Spam Mail/Approved Sender List":1,"/Top of Personal Folders/Inbox":21},"compressedAverageSize":0.0,"averageSize":0.0,"medianSize":0.0,"medianCompressedSize":0.0,"completed":true}];
			$scope.selectedPsts = $scope.mapPsts(['adf26bdf-4857-4ce5-b7b5-bc32a1088a7e', 'adf26bdf-4857-4ce5-b7b5-bc32a1088a7f', 'adf26bdf-4857-4ce5-b7b5-bc32a1088a7g']) || [];
*/
//				$scope.selectedPsts = $scope.mapPsts($scope.selectedPsts);
				$scope.unselectedPsts = $scope.getUnselectedPsts();

			});
	}
    });

    $scope.extractPstIds = function(input) {
	if ($scope.activeTab === $scope.tabName) {
		var newPstArray = [];
		angular.forEach(input, function(pst) {
			newPstArray.push(pst.id);
		});
		return newPstArray;
	}
    };

    $scope.mapPsts = function(input) {
	if ($scope.activeTab === $scope.tabName) {
		if (input) {
			var newPstArray = [];
			angular.forEach(input, function(unmatchedPst) {
				angular.forEach($scope.psts, function(pst) {
					if (pst.id === unmatchedPst) {
						newPstArray.push(pst);
					}
				});
			});
			
			if (newPstArray.length > 0) {
				return newPstArray;
			}
			else {
				return $scope.selectedPsts;
			}
		}
		else {
			return $scope.selectedPsts;
		}
	}
    };

    $rootScope.$on('multipleDisplayControllerInit', function(evt, tab) {
	if ($scope.tabName === tab) {
		$scope.requestUserList();
	}
    });

    $scope.requestUserList = function() {
	if ($scope.activeTab === $scope.tabName) {
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
	    $scope.initCustomScrollbars('.scrollbar-hidden');	
	}
    };

    $rootScope.$on('activeTab', function(evt, tab) {
	    if (tab === $scope.tabName) {
		    $scope.onTabActivation();
	    }
    });

    $scope.selectPsts = function(psts) {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach(psts, function(selectedId) {
			angular.forEach($scope.unselectedPsts, function(availablePst, key) {
				if (selectedId === availablePst.id) {
					$scope.selectedPsts.push(availablePst);
					$scope.unselectedPsts.splice(key, 1);
				}
			});
		});
		psts = [];
	}
    };

    $scope.removePsts = function(psts) {
	if ($scope.activeTab === $scope.tabName) {
		angular.forEach(psts, function(selectedId) {
			angular.forEach($scope.selectedPsts, function(availablePst, key) {
				if (selectedId === availablePst.id) {
					$scope.unselectedPsts.push(availablePst);
					$scope.selectedPsts.splice(key, 1);
				}
			});
		});
		psts = [];
	}
    };

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
		    var result = searchHistoryService.getSearchHistory();
		    if (result) {
			    if (result.pending) {
					result.pending.then(function(data) {
						$scope.$emit('searchHistoryData', data);
					});
				}
			    else if (result.cached) {
				$scope.$emit('searchHistoryData', result);
			    }
		    } else {
			$scope.$emit('technicalError', 'There is no search history available');
		    }
	    }

	    if ($scope.activeTab === 'users') {
		$scope.userView();
	    }
    };
	
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

    $scope.getMaxNumResults = function() {
	return $scope.maxNumResults;
    };

    $rootScope.$on('maxNumResultsRequest', function(evt, user) {
        if ($scope.activeTab === $scope.tabName) {
		usersService
			.one('max')
			.one(user.loginUser.username)
			.get()
			.then(function(result) {
				$scope.maxNumResults = result.payload.matches;
			});
	}
    });

    $rootScope.$on('selectResultRequest', function(evt, user) {
        if ($scope.activeTab === $scope.tabName) {
            usersService
		.one(user.loginUser.username)
		.get()
		.then(function(result) {
	    		$scope.userCancel(true);
			$scope.$emit('selectResultData', result.payload);
			$scope.$emit('selectUser', result.payload);
/*			user.otherEmails = $scope.getOtherEmails(user.loginUser);
*/
			$scope.selectedPsts = $scope.mapPsts(user.loginUser.sources) || [];
			$scope.currentUser = user;
			$scope.$emit('maxNumResultsRequest', user);
			$scope.$emit('searchHistoryRequest', user);

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

    $scope.onTabActivation();

});


}());
