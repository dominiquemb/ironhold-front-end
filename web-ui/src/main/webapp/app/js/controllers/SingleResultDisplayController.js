(function () {
   'use strict';

ironholdApp.controller('SingleResultDisplayController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService) {
    logInService.confirmLoggedIn($state);
	
    $scope.showContainer = false;
    $scope.currentMessage = false; 
    $scope.showPreviewToolbar = false;
    $scope.mode = 'text';
    $scope.modeData = {};
    $scope.limitedTabs = false;
    $scope.middleSectionHeight = 0;
    $scope.bottomSectionHeight = 0;
    $scope.topSectionHeight = 0;

    $scope.getFileType = function(name) {
	var ext = name.toLowerCase().split('.');
	ext = ext[ext.length-1];
	if (!(parseInt(ext[0]) === "NaN" || ext.length > 1)) {
		ext = "ext-" + ext;
	}
	return ext;
    };

    $scope.$watch(function() {
	return Math.round($('.msgview_middle').offset().top) + $('.msgview_bottom').height() + 185;
	},
	function(newval) {
		if (newval > $('body').height()) {
			$('.wrapper').css('min-height', newval);
		}
     });
			
    $scope.$watch(function() {
		return $('.sub-tab-content-inner').text();
	}, function(newval, oldval) {
		if (newval !== oldval) {
			if ($scope.activeTab === $scope.tabName) {
				$scope.$emit('initCustomScrollbars', '.sub-tab-content');
    				$scope.adjustMiddleSection();
			}
		}
     });
/*
    $scope.$watch(function() {
		return $('.msgview_bottom').height();
        },
        function(newval, oldval) {
            //jshint unused:false
		    if ($scope.activeTab === $scope.tabName) {
    			$scope.adjustMiddleSection();
    		}
     });
*/
    $rootScope.$on('pageResized', function() {
	$scope.adjustMiddleSection();
    });

    $scope.adjustMiddleSection = function() {
		console.log('Tab content height:');
		console.log($('.msgview .tab-content').height());
	
		console.log('Controlbar height:');
		console.log($('.msgview .controlbar').height());

		console.log('Top section height:');
		console.log($('.msgview_top').outerHeight(true));

		var msgviewHeight = $('.msgview .tab-content').height() - $('.msgview .controlbar').height() - $('.msgview_top').outerHeight(true) - $('.msgview_main').outerHeight(true) + 4;

		if ($('.msgview_bottom').height() == null) {
			msgviewHeight -= 4;
		}

		console.log('Height of both middle section and bottom section:');
		console.log(msgviewHeight);

		$scope.bottomSectionHeight = $('.msgview_bottom').outerHeight(true);

		console.log('Botttom section height:');
		console.log($scope.bottomSectionHeight);

		if ($scope.bottomSectionHeight == null) {
				$scope.middleSectionHeight = msgviewHeight;
		} else {
                $scope.middleSectionHeight = msgviewHeight - $scope.bottomSectionHeight;
		}


		console.log('Middle section height:');
		console.log($scope.middleSectionHeight);

		$('.msgview_middle').height($scope.middleSectionHeight);

    };

    $rootScope.$on('highlightActive', function(evt, offOrOn) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.limitedTabs = offOrOn;
	}
    });

    $scope.isModeActive = function(mode) {
        if ($scope.activeTab === $scope.tabName) {
            return $scope.mode === mode;
        }
    };

    $scope.downloadMessage = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.$emit('downloadMessage', $scope.currentMessage);
        }
    };

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
		$state.go('loggedin.main.text');
	}
    });

    $rootScope.$on('modeData', function(evt, results) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.modeData[results.mode] = [];
		$scope.modeData[results.mode] = results.payload;
		setTimeout(function() {
					$('.msgview_bottom').height( $('.msgview_bottom').height() + 'px' );
					$('.msgview_bottom .jspVerticalBar').css('visibility', 'visible');
					$scope.$emit('initCustomScrollbars', '.msgview_bottom');
				}, 200);

	}
    });

    $scope.textTab = function() {
		$scope.$emit('modeRequest', {
			mode: '',
			date: new Date($scope.currentMessage.formattedIndexedMailMessage.messageDate),
			messageId: $scope.currentMessage.formattedIndexedMailMessage.messageId,
			criteria: {'criteria': $scope.inputSearch}
		});
    };

    $scope.auditTab = function() {
		var curDate = new Date($scope.currentMessage.formattedIndexedMailMessage.messageDate),
		msgId = $scope.currentMessage.formattedIndexedMailMessage.messageId;

		$scope.$emit('modeRequest', {
			mode: 'sources',
			date: curDate,
			messageId: msgId
		});

		$scope.$emit('modeRequest', {
			mode: 'audit',
			date: curDate,
			messageId: msgId
		});

		$scope.$emit('modeRequest', {
            mode: 'logs',
            date: curDate,
            messageId: msgId
        });
    };
	

    $scope.requestSubTabData = function(mode) {
		$('.msgview_bottom .jspVerticalBar').css('visibility', 'hidden');
		$('.msgview_bottom').height('');
		$('.msgview_bottom').height('auto !important');

		if (typeof $scope[mode + 'Tab'] === 'function') {
			$scope[mode + 'Tab']();
		}
		else {
			$scope.$emit('modeRequest', {
				mode: mode,
				date: new Date($scope.currentMessage.formattedIndexedMailMessage.messageDate),
				messageId: $scope.currentMessage.formattedIndexedMailMessage.messageId,
				criteria: {'criteria': $scope.inputSearch}
			});
		}
    };

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
    };

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


}());
