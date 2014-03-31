'use strict';

ironholdApp.directive('pagination', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Pagination.html',
		controller: 'PaginationController'
	}
});

ironholdApp.directive('attachmentsView', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/AttachmentsView.html'
	}
});

ironholdApp.directive('messageHeader', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/MessageHeader.html'
	}
});

ironholdApp.directive('body', function() {
	return {
		restrict: "ACE",
		controller: 'BodyController'
	}
});

ironholdApp.directive('errors', function() {
	return {
		restrict: 'ACE',
		controller: 'ErrorsController'
	}
});

ironholdApp.directive('sortingPanel', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/SortingPanel.html',
	}
});

ironholdApp.directive('footer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Footer.html',
		controller: 'FooterController'
	}
});

ironholdApp.directive('searchTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SearchController'
	}
});

ironholdApp.directive('messageSearchTab', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'MessageSearchController'
	}
});

ironholdApp.directive('controlbar', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/Controlbar.html'
	}
});

ironholdApp.directive('discoveryTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'DiscoveryController'
	}
});

ironholdApp.directive('foldersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'FoldersController'
	}
});

ironholdApp.directive('logsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'LogsController'
	}
});

ironholdApp.directive('usersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'UsersController'
	}
});

ironholdApp.directive('settingsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SettingsController'
	}
});

