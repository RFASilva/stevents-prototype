var test;

//Global Variables
var currentLayer  = null;
var points_bounds;

var timeseriesWidth;
var tiles = 0;

//Connection variables to the server
var myip = "localhost";
var myport = "8080";

var numberFeatures = 0;

var mapCanvas;
var map = null; 
var points;

//Restricted Area
var isRestricted = false;
var geometryRestriction;

//Funcao que carrega o mapa e afins
function init() {
	init_map();
	zoomMap = map.getZoom();
	defineSaveForm();
}


function createMapCanvas() {
	mapCanvas = document.createElement('canvas');
	mapCanvas.id = 'mapCanvas'
		mapCanvas.style.position = 'relative';
	mapCanvas.style.zIndex='-1';
	mapCanvas.height = map.getSize().y;
	mapCanvas.width = map.getSize().x;

	var mapDiv = map.getContainer();
	mapDiv.appendChild(mapCanvas);
}

function save(e) {
	 $("#saveForm").dialog( "open" );
}

function defineSaveForm() {
	$( "#saveForm" ).dialog({
	      autoOpen: false,
	      height: 280,
	      width: 350,
	      modal: true,
	      buttons: {
	        "Save": function() {
	            $( this ).dialog( "close" );
	        },
	        Cancel: function() {
	          $( this ).dialog( "close" );
	        }
	      },
	      close: function() {
	    	  $( this ).dialog( "close" );
	      }
	    });
}

function init_map() {

	if (map!= undefined) {
		map.remove();
		$("#map").empty();
		map = null;
	} 

//	map = new L.Map('map', {center: new L.LatLng(41, -93), zoom: 4}); //center usa
	map = new L.Map('map', {center: new L.LatLng(39.5, -8), 
		zoom: 7,
		contextmenu: true,
		contextmenuWidth: 140,
		contextmenuItems: [{
			text: 'Save Perspective',
			callback: save
		}]	}
	); 

	var googleLayer = new L.Google('ROADMAP');
	map.addLayer(googleLayer);

//	new L.marker(new L.LatLng(39, -99)).addTo(map); //center USA
//	new L.marker(new L.LatLng(39.5, -8)).addTo(map); //center Portugal

	createMapCanvas();

	map.on('zoomstart', clearCanvas);
	map.on('dragstart', clearCanvas);
	map.on('zoomend', drawMapCanvas);
	map.on('dragend', drawMapCanvas);


	// Initialise the FeatureGroup to store editable layers
	var drawnItems = new L.FeatureGroup();

	var options = {
			position: 'topleft',
			draw: {
				polyline: false,
				polygon: {
					showArea: true,
					zIndexOffset:50,
					allowIntersection: false, // Restricts shapes to simple polygons
					drawError: {
						color: '#e1e100', // Color the shape will turn when intersects
						message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
					},
					shapeOptions: {
						color: '#333366',
						fillOpacity: ' 0.1'
					}
				},
				marker: false,
				circle:false,
//				circle: {
//				zIndexOffset:50,
//				shapeOptions: {
//				color: '#ff0000',
//				clickable: true
//				}
//				}, 
				rectangle: {
					zIndexOffset:50,
					shapeOptions: {
						color: '#333366',
						fillOpacity: ' 0.1'
					}
				}
			},
			edit: {
				featureGroup: drawnItems
			}
	};

	var drawControl = new L.Control.Draw(options);
	map.addControl(drawControl);
	map.addLayer(drawnItems);

	map.on('draw:created', function (e) {
		var type = e.layerType,
		layer = e.layer;

		var dataset = ($("#dataset").val() != undefined) ? $("#dataset").val() : undefined;
		geometryRestriction = layer.toGeoJSON().geometry.coordinates.toString();
		isRestricted = true;
		console.log(geometryRestriction);
		
		contextUrl = "http://"+ myip + ":" + myport + "/context?isRestricted=true&geometry=" + geometryRestriction + "&dataset=" + dataset;
		$.post(contextUrl);
		
		drawnItems.addLayer(layer);
	});

	// TODO: Here, it is the assumption that i only have a geometry which can be false. To be improved :)
	map.on('draw:deleted', function (e) {
		var dataset = ($("#dataset").val() != undefined) ? $("#dataset").val() : undefined;
		var layers = e.layers;
		layers.eachLayer(function (layer) {
			//do whatever you want, most likely save back to db
		});

		isRestricted = false;
		contextUrl = "http://"+ myip + ":" + myport + "/context?isRestricted=false&geometry=null&dataset=" + dataset ;
		$.post(contextUrl);
		
		
	});

	map.on('draw:drawstart', function (e) {
		$( "#mapCanvas" ).remove();

	});

	map.on('draw:drawstop', function (e) {
		createMapCanvas();
	});

	map.on('draw:editstart', function (e) {
		$("#mapCanvas").remove();

	});

	map.on('draw:editstop', function (e) {
		createMapCanvas();
	});


	map.on('draw:deletestart', function (e) {
		$("#mapCanvas").remove();
	});

	map.on('draw:deletestop', function (e) {
		createMapCanvas();
	});
	
}

function clearCanvas(e) {
	var mapContext = mapCanvas.getContext("2d");
	mapContext.clearRect (0 , 0 , mapCanvas.width, mapCanvas.height );
	quadTree = null;
}


function drawCanvas(geojson) {
	clearCanvas();
	points = geojson;
	drawMapCanvas(null);
	numberFeatures = 0;
	mapCanvas.style.zIndex='10';
}

function drawMapCanvas(e) {
	if (points != null) { 
		console.log("draw map canvas");
		var mapContext = mapCanvas.getContext("2d");
		var bounds = map.getBounds();
		var maxLatitude = bounds.getNorth();
		var minLatitude = bounds.getSouth();
		var maxLongitude = bounds.getEast();
		var minLongitude = bounds.getWest();
		var imageData = mapContext.createImageData(mapCanvas.width, mapCanvas.height);


		for (var i = 0; i < points.features.length; i++) {
			var loc = points.features[i].geometry.coordinates;

//			if	inside bounding box
			if (loc[1] >= minLatitude && loc[1] <= maxLatitude && 
					loc[0] >= minLongitude && loc[0] <= maxLongitude) {

				var pixelCoordinate = map.latLngToContainerPoint( new L.LatLng(loc[1], loc[0]) );

				var a = 125;
				var r = 255;
				var g = 0;
				var b = 0;

				drawRectangle(imageData, pixelCoordinate.x, pixelCoordinate.y, r, g, b, a);
			}
		}

		mapContext.putImageData(imageData, 0, 0); 
	}
}

function getGeoJSON(url) {
	if(map!=null) {
		var start_time = new Date();
		console.log("request geodata: " + url);

		$.getJSON(url, function(data) {
			var getRequest_time = new Date();
			drawCanvas(data);

			var finishRequest_time = new Date();
			console.log("Apos a funcao de desenho: " + (finishRequest_time - start_time));

		});
	}
}


function drawRectangle(imageData, x, y, r, g, b, a) {

	//Stroke
	for(var i= x-2; i <= x+2; i++) {
		setPixel(imageData, i, y+2, 0, 0, 0, 255);
		setPixel(imageData, i, y-2, 0, 0, 0, 255);
	}

	//Stroke
	for(var i= y-2; i <=y+2; i++) {
		setPixel(imageData, x-2, i, 0, 0, 0, 255);
		setPixel(imageData, x+2, i, 0, 0, 0, 255);
	}

	//fill
	for(var i= x-1; i <= x+1; i++) {
		setPixel(imageData, i, y+1, r, g, b, a);
		setPixel(imageData, i, y-1, r, g, b, a);
	}

	//fill
	for(var i= y-1; i <=y+1; i++) {
		setPixel(imageData, x-1, i, r, g, b, a);
		setPixel(imageData, x+1, i, r, g, b, a);
	}

	setPixel(imageData, x, y, r, g, b, a);
}

function setPixel(imageData, x, y, r, g, b, a) {
	index = (x + y * imageData.width) * 4;
	imageData.data[index + 0] = r;
	imageData.data[index + 1] = g;
	imageData.data[index + 2] = b;
	imageData.data[index + 3] = a;
}


