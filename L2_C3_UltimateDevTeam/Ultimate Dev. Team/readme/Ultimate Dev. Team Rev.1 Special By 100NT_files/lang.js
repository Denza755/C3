jQuery(function($){
  $.translate(function(){ 
  		function translateTo( destLang ){  
				                
        $('body').translate( 'russian', destLang, {
          fromOriginal:true,   //always translate from english (even after the page has been translated)   
		                         
			start: function(){ $('#throbber').show() },   // show loading icon
			complete: function(){ $('#throbber').hide() },  // hide loading icon          
		   error: function(){ $('#throbber').hide() }  // hide loading icon
        }); 
    }
    	  
	$('#flags2 span').click(function(){         
	var lang = $(this).attr('id');         
	translateTo( lang );
	if ( lang != 'russian')
		$.cookie('destLang', lang, {expires: 365, path: "/"});
	else
		$.cookie('destLang', 'russia', {expires: 0, path: "/"});

	return false;                            
	});
         
    var destLang = $.cookie('destLang'); //get previously translated language 
    if( destLang )  //if it was set then 
    	translateTo( destLang );   

  });
 })