//
// Add hidden fields to all forms in the document
//
// NOTE: This is the PRIMARY function in this library, and is called on page
//       load. All other methods are helpers.
//
jQuery(function() { 

  var data = getDataFromCookiesAndURL()

  // Iterate through all forms and add the lead source data to them
  var forms = jQuery('form');
    
  forms.each(function () {
      var form = jQuery(this);
	  
	  if (!/google/.test(form.attr('action'))) {
		  setHiddenField(form, "LeadSource", getLeadSource(data)['name'])
		  setHiddenField(form, "Lead_Source_Description__c", getLeadSource(data)['desc'])
		  setHiddenField(form, "utm_source__c", data['utmcsr'])
		  setHiddenField(form, "utm_medium__c", data['utmcmd'])
		  setHiddenField(form, "utm_term__c", data['utmctr'])
		  setHiddenField(form, "utm_content__c", data['utmcct'])
		  setHiddenField(form, "utm_campaign__c", data['utmccn'])
		  setHiddenField(form, "utm_adgroup__c", data['ag'])
		  setHiddenField(form, "gclid", data['utmgclid'])
	  }
	});
})

//
// Mine cookies and URL parameters for lead capture data
//
function getDataFromCookiesAndURL() {
  //  
  // Get the __utmz cookie value. This is the cookie that 
  // stores all campaign information. 
  // 
  var z = _uGC(document.cookie, '__utmz=', ';') 

  // 
  // The cookie has a number of name-value pairs. 
  // Each identifies an aspect of the campaign. 
  // 
  // TODO: Get the URL parameter if the cookie parameter is either not present or equal to "(not set)"
  //
  var obj = {}

  // campaign source
  obj['utmcsr'] = decodeURI(_uGC(z, 'utmcsr=', '|'))
  if (!obj['utmcsr'] || obj['utmcsr'] == '(not set)') {
    obj['utmcsr'] = decodeURI(getUrlParam('utm_source'))
  }

  // campaign medium
  obj['utmcmd'] = decodeURI(_uGC(z, 'utmcmd=', '|'))
  if (!obj['utmcmd'] || obj['utmcmd'] == '(not set)') {
    obj['utmcmd'] = decodeURI(getUrlParam('utm_medium'))
  }

  // campaign term (keyword)
  obj['utmctr'] = decodeURI(_uGC(z, 'utmctr=', '|'))
  if (!obj['utmctr'] || obj['utmctr'] == '(not set)') {
    obj['utmctr'] = decodeURI(getUrlParam('utm_term'))
  }

  // campaign content
  obj['utmcct'] = decodeURI(_uGC(z, 'utmcct=', '|'))
  if (!obj['utmcct'] || obj['utmcct'] == '(not set)') {
    obj['utmcct'] = decodeURI(getUrlParam('creative'))
  }

  // campaign name
  obj['utmccn'] = decodeURI(_uGC(z, 'utmccn=', '|'))
  if (!obj['utmccn'] || obj['utmccn'] == '(not set)') {
    obj['utmccn'] = decodeURI(getUrlParam('c'))
  }

  //
  // For Adwords, there are a number of ValueTrack
  // capture values in the URL.
  //
  obj['utmgclid']  = _uGC(z, 'utmgclid=', '|') // AdWords auto tagging UID
  obj['network']   = decodeURI(getUrlParam('network')) // The source of the click
  obj['placement'] = decodeURI(getUrlParam('placement')) // The domain of the click
  obj['creative']  = decodeURI(getUrlParam('creative')) // The UID for the creative
  obj['keyword']   = decodeURI(getUrlParam('keyword')) // The ad-triggering keyword
  obj['ag']        = decodeURI(getUrlParam('ag'))

  // 
  // The gclid is ONLY present when auto tagging has been enabled. 
  // All other variables, except the term variable, will be '(not set)'. 
  // Because the gclid is only present for Google AdWords we can 
  // populate some other variables that would normally 
  // be left blank. 
  // 
  if (obj['utmgclid']) { 
    obj['utmcsr'] = 'google' 
    obj['utmcmd'] = 'cpc' 
    //obj['utmctr'] = ''
    //obj['utmcct'] = ''
    //obj['utmccn'] = ''
  } 

  return obj
}

//
// This is a function that I "borrowed" from the urchin.js file.
// It parses a string and returns a value.  I used it to get
// data from the __utmz cookie
//
function _uGC(l,n,s) {
  if (!l || l=="" || !n || n=="" || !s || s=="") return "-";
  var i,i2,i3,c='';
  i=l.indexOf(n);
  i3=n.indexOf("=")+1;
  if (i > -1) {
    i2=l.indexOf(s,i); if (i2 < 0) { i2=l.length; }
    c=l.substring((i+i3),i2);
  }
  return c;
}

//
// Get a specific URL parameter from the current HREF
//
function getUrlParam(param) {
  param = param.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+param+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return null;
  else
    return results[1];
}

//
// Get the lead source name and description
//
function getLeadSource(data) {
  if (data['utmcmd'] == 'organic') {
    return {
      name: "Web - Organic",
      desc: data['utmcsr'] + " - " + data['utmctr'],
    }
  }
  else if (data['utmcmd'] == 'cpc' && data['network'] == 'null') {
    return {
      name: "Web - PPC",
      //desc: data['creative'] + " - " + data['utmctr'],
      desc: data['utmccn'] + " - " + data['ag'],
    }
  }
  else if (data['utmcmd'] == 'cpc' && data['network'] == 'd') {
    return {
      name: "Web - Display",
      desc: data['placement'],
    }
  }
  else if (data['utmcmd'] == '(none)') {
    return {
      name: "Web - Direct",
      desc: null,
    }
  }
  else if (data['utmcmd'] == 'referral') {
    return {
      name: "Web - Referrer",
      desc: data['utmcsr'],
    }
  }
  else if (data['utmcmd'] == 'socnet') {
    return {
      name: "Web - Social",
      desc: data['utmccn'] + " - " + data['utmcsr'],
    }
  }
  else {
    return {
      name: "Web - Campaign",
      desc: data['utmccn'] + " - " + data['utmcsr'],
    }
  }
}

//
// Creates an HTML Hidden Field or sets the value if the hidden field is already created
//
function setHiddenField(form, fieldName, fieldValue) {
    var field = form.find('input[name*="' + fieldName + '"]');
	
    if (field.length > 0) {
        field.each(function () {
			jQuery(this).val(fieldValue);
		});
    } else {
      var field = jQuery("<input type='hidden' name='" + fieldName + "' id='" + fieldName + "' value='" + fieldValue + "' />")
      jQuery(form).append(field)
    }
}
