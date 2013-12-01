$(window).load(function() {
	(function() {
		var originalAddClass = $.fn.addClass;

		$.fn.addClass = function() {
			var result = originalAddClass.apply(this, arguments);
			$(this).trigger('classAdded', arguments);
			return result;
		}
	})();

	(function() {
		var originalRemoveClass = $.fn.removeClass;

		$.fn.removeClass = function() {
			var result = originalRemoveClass.apply(this, arguments);
			$(this).trigger('classRemoved', arguments);
			return result;
		}
	})();

	setTimeout(function() {
		$('.scrollbar-hidden').jScrollPane({
			showArrows: true,
			verticalArrowPositions: 'split',
			horizontalArrowPositions: 'split'
		});

	}, 0);

	$(function()
	{
	    var bars = '.jspHorizontalBar, .jspVerticalBar';
	    $('.scrollbar-hidden').bind('jsp-initialised', function (event, isScrollable) {
		
		//hide the scroll bar on first load
		$(this).find(bars).hide();
	    
	    }).jScrollPane().hover(
	    
		//hide show scrollbar
		function () {
		    $(this).find(bars).stop().fadeTo('fast', 0.9);
		},
		function () {
		    $(this).find(bars).stop().fadeTo('fast', 0);
		}
	    );                
	});
/*
	var initMsgPreview = function() {
		var msgPreviewHeight = $(window).innerHeight() - ( $('.msgview_middle').offset().top + $('.msgview_bottom').outerHeight(true) + $('.footer').outerHeight(true) );
		if (msgPreviewHeight > 0) {
			$('.msgview_middle').css('height', msgPreviewHeight);
		}
	},

	initResultList = function() {
		var resultListHeight = $(window).innerHeight() - ( $('.results').offset().top + $('.footer').outerHeight(true) );
		if (resultListHeight > 0) {
			$('.message-list-container').css('height', resultListHeight);
		}
	}

	initMsgPreview();
	initResultList();
*/
});

$.fn.textResize = function(settings) {
	if ($(this).data('textResize') != true) {
		$(this).data('textResize', true);
		$(this).data('textResizeOriginalText', $(this).text().trim());
	}
	var charsPerLine = Math.floor( settings.containerWidth / settings.charPxlWidth),
	text = (settings.text) ? settings.text : $(this).text().trim(),
	totalLines = Math.floor(text.length / charsPerLine),
	totalHeight = Math.floor(settings.fontSize * totalLines),
	desiredLines = Math.floor(settings.desiredHeight / settings.fontSize),
	maxChars = (settings.trailingDots) ? Math.floor((charsPerLine * desiredLines)) - 3 : Math.floor((charsPerLine * desiredLines));

	if (totalHeight > settings.desiredHeight) {
		var result = text.split("").splice(0, maxChars).join("") + ( (settings.trailingDots) ? "..." : "");
		$(this).html(result);
	}
	else if (settings.text) {
		$(this).html(text);
	}
	return this;
}
