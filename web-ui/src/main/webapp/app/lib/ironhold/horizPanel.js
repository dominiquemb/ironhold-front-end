$(function() {
    $.fn.collapseColumn = function(toLose, toGain, isVertical) {
	var changingTo = (isVertical) ? "top" : "left";
        var size = toLose;
	var obj = {}; obj["margin-"+changingTo] = size;
        var gains = [];
        toGain.each(function() {
            gains.push($(this).width());
        });
        $(this).stop().animate(obj,
		{
			duration: 300, 
			step: function(k) {
		                for (var i = 0; i < gains.length; i++) {
					if (!isVertical) {
                    				toGain.eq(i).outerWidth(gains[i]-k);
					}
		                }
            		}
        	}
	);
	$(this).parent().addClass('slide-active');
        var k = $(this);
        return function() {
	    obj[Object.keys(obj).slice(0,1)[0]] = 0; 
            k.stop().animate(
		obj, 
		{
			duration: 300, 
			step: function(s) {
		                for (var i = 0; i < gains.length; i++) {
					if (!isVertical) {
                    				toGain.eq(i).outerWidth(gains[i]-s);
					}
		                }
	            	}
		}
	    );
	    k.parent().removeClass('slide-active');
        };
    };
});
