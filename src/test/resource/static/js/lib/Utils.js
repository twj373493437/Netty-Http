var Utils = {
	getBasePath: function(){
		var pathName = window.location.pathname.substring(1);
        var webName = pathName == '' ? '' : pathName.substring(0, pathName.indexOf('/'));
        
        if (!webName || webName == "" || webName == 'undefined') {
            return window.location.protocol + '//' + window.location.host + '/';
        }
        else {
            return window.location.protocol + '//' + window.location.host + '/' + webName + '/';
        }
        
	}
}
