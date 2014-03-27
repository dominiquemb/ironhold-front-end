'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = false;
    $scope.currentMessage = false; 
    $scope.showPreviewToolbar = false;
    $scope.mode = 'text';
    $scope.modeData = {};
    $scope.limitedTabs = false;
/*
    $scope.$watch(function() {
                if ($('.msgview_bottom').length > 0) {
                        return $('.msgview_bottom').height();
                }
                else return 0;
        },
        function(newval, oldval) {
		if ($scope.activeTab === $scope.tabName) {
			var msgviewHeight = $('.msgview .tab-content').height() - $('.msgview .controlbar').height();
			if (newval > (msgviewHeight/2)) {
				$('.msgview_bottom').height(msgviewHeight/2);
				$scope.$emit('initCustomScrollbars', '.msgview_bottom');
				$scope.$emit('reinitScrollbars');
				newval = msgviewHeight/2;
console.log(newval);
			}

			if (newval > oldval) {
console.log(newval);
				if ($scope.currentMessage) {
					$('.msgview_middle').height(
						$('.msgview_middle').height() - newval
					);
					$scope.$emit('reinitScrollbars');
				}
			}
			if (oldval > newval) {
				if ($scope.currentMessage) {
					$('.msgview_middle').height(
						$('.msgview_middle').height() + oldval
					);
					$scope.$emit('reinitScrollbars');
				}
			}
		}
     });
*/
    $rootScope.$on('highlightActive', function(evt, offOrOn) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.limitedTabs = offOrOn;
	}
    });

    $scope.isModeActive = function(mode) {
	if ($scope.activeTab === $scope.tabName) {
		return $scope.mode === mode;
	}
    }

    $scope.downloadMessage = function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.$emit('downloadMessage', $scope.currentMessage);
	}
    }

    $scope.downloadAttachment = function(attachment) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.$emit('downloadAttachment', {
			message: $scope.currentMessage,
			attachment: attachment
		});
	}
    };	

    $rootScope.$on('reset', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentMessage = false;
		$scope.showPreviewToolbar = false;
		$scope.mode = 'text';
		$scope.modeData = {};
		$scope.showContainer = false;
	}
    });

    $rootScope.$on('modeData', function(evt, results) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.modeData[results.mode] = [];
		$scope.modeData[results.mode] = results.payload;
	}
    });

    $scope.requestSubTabData = function(mode) {
		if (mode === 'text') {
			mode = '';
		}

		$scope.$emit('modeRequest', {
			mode: mode,
			date: new Date($scope.currentMessage.formattedIndexedMailMessage.messageDate),
			messageId: $scope.currentMessage.formattedIndexedMailMessage.messageId,
			criteria: {'criteria': $scope.inputSearch}
		});
    }

    $rootScope.$on('mode', function(evt, mode) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.mode = mode;
		$scope.requestSubTabData(mode);
	}
    });

    $scope.switchMode = function(newMode) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.mode = newMode;
		$scope.$emit('mode', newMode);
	}
    }

    $rootScope.$on('selectMessage', function(evt, message) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.currentMessage = message;
		$scope.showPreviewToolbar = true;
		$scope.requestSubTabData($scope.mode);
	}
    });

    $rootScope.$on('search', function() {
	if ($scope.activeTab === $scope.tabName) {
		$scope.showContainer = true;
	}
    });

});
