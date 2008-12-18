// Sprachvariablen
var verbundserverstatus = new Array('erlaubt','erlaubt(kein sync)','gesperrt');
var ToolLang = "Um Hilfe zu erhalten, fahren sie mit der Maus auf den Bereich, über den sie mehr erfahren möchten.";
var WaitScreenLang = "Anfrage wird ausgeführt..."

/**
 * Fehlerkonsole
 * @param {Object} id
 */
var FailureConsole = Class.create({
    initialize: function(id){
        this.id = id;
        this.errorsVisible = false;
		$(this.id).setStyle({
			display: "none"
		});
		/*if(! ($(this.id).empty()) ){
			this.toggle();	
		}*/		
    },
    toggle: function(){
        if (this.errorsVisible) {
            Effect.BlindUp(this.id);
            this.errorsVisible = false;
        }
        else {
            Effect.BlindDown(this.id);
            this.errorsVisible = true;
        }
    }
});

/** Tooltip Element
 * Erstellt sich bein initialisieren selbst.
 * Hierzu ist zu Beachten das ALLE ID's aus dem CSS im 
 * Tooltipblock nur hier verwendet werden!
 */
var Tooltip = Class.create('Element', {
    initialize: function(){
    	this.status = false;
        this.toHTML();
        $('tooltip').hide();			
    },
	// Schaltet Tooltip an / aus
    toggle: function( content){
 		if (this.status){
			this.status = false;
			$('tooltip').hide();
		}else{
			this.status = true;
			this.showWith(ToolLang);
		}		
	},
	// Fügt Inhalt ein
    showWith: function( content){
		if (this.status ){
			$$('.tooltip #content')[0].update(content);
        	$('tooltip').show();
		}
    },
	// Tooltip wird zurückgesetzt
	hide:	function(){
		if (this.status) {			
			this.showWith(ToolLang);
       		//setTimeout('$$(\'.tooltip #content\')[0].update(ToolLang);', 2000);			
		}
	},
    toHTML: function(){
        document.write('<div class="tooltip" id="tooltip" style="">' +
        '<div id="row">' +
        '<div id="leftcorner"></div>' +
        '<div id="top"><img src="images/tooltip_arrow.png" /></div>' +
        '<div id="rightcorner"></div>' +
        '</div>' +
        '<div id="row">' +
        '<div id="left"></div>' +
        '<div id="content">' +
        
        '</div>' +
        '<div id="right"></div>' +
        '</div>' +
        '<div id="row">' +
        '<div id="leftdowncorner"></div>' +
        '<div id="down"></div>' +
        '<div id="rightdowncorner"></div>' +
        '</div>' +
        '</div>');
    }
});

/**
 * Waiting screen erstellen
 */
var Waiting = Class.create('Element', {
    initialize: function(){
        this.toHTML();
        $('waiting').hide();			
    },
	show: function(){
        $('waiting').show();
	},	
	hide: function(){
        $('waiting').hide();
	},	
	toHTML: function(){
        document.write('<div id="waiting" style="">' +
        '<div id="waitingsplash"><img src="images/wait.gif" /><br/>' +
		WaitScreenLang+
        '</div></div>');
    }
});

/**
 * Klasse zum Expandieren von Baumstrukturen
 * @param {Object} selfid	eigener ID name
 * @param {Object} targetid Ziel-ID zum ausklappen
 * @param {Object} basename Image-Basename
 */
var Expander = Class.create('Element', {	
    initialize: function(self, selfid,targetid, basename ){
		this.name=self;
		this.id=selfid;
		this.target=targetid;
		this.base=basename;
		this.toHTML();
		this.visible=false;	
    },
	pander:	function(){
		if(this.visible) {
			$(this.id).replace(	'<img id="' + this.id + '"	src="images/expand-' +
				this.base + '.png" alt="' + this.base + '" onclick="'+
				this.name + '.pander()" />');
			this.visible=false;
		}else{
			$(this.id).replace(	'<img id="' + this.id + '"	src="images/collapse-' +
				this.base + '.png" alt="' + this.base + '" onclick="'+
				this.name + '.pander()" />');
			this.visible=true;
		}
		Effect.toggle(this.target, 'blind', { duration: 0.2});
	},
	toHTML: function(){
        document.write('<img id="' + this.id + '"	src="images/' +
		 this.base + '.png" alt="' + this.base + 
		 '" onclick="'+ this.name + '.pander()" />');
    }
});
