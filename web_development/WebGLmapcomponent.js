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


// Variables under the context of Web GL 
var gl;
var pointProgram;
var pointArrayBuffer;

var pixelsToWebGLMatrix = new Float32Array(16);
var mapMatrix = new Float32Array(16);
var pi_180 = Math.PI / 180.0;
var pi_4 = Math.PI * 4;


//Funcao que carrega o mapa e afins
function init() {
	init_map();
	zoomMap = map.getZoom();
	defineSaveForm();
}


function createMapCanvas() {
	mapCanvas = document.createElement('canvas');
	mapCanvas.id = 'mapCanvas'
		mapCanvas.style.position = 'absolute';
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

	map.on('move', drawPoints);
	createMapCanvas();
	
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

function resize() {
	gl.viewport(0, 0, map.getContainer().offsetWidth, map.getContainer().offsetHeight);
}

function drawCanvas(geojson) {
//	clearCanvas();
	points = geojson;
	
	// Draw using WebGL
	webGLStart(null);
	pixelsToWebGLMatrix.set([2/mapCanvas.width, 0, 0, 0, 0, -2/mapCanvas.height, 0, 0, 0, 0, 0, 0, -1, 1, 0, 1]);
	
	drawPoints();
	
	numberFeatures = 0;
	mapCanvas.style.zIndex='10';
}


function webGLStart() {
	var canvas = document.getElementById("mapCanvas");
	
	initGL(canvas);
	resize();
	initShaders();
	initBuffers();
	
}

var pi_180 = Math.PI / 180.0;
var pi_4 = Math.PI * 4;

function latLongToPixelXY(latitude, longitude) {

	var sinLatitude = Math.sin(latitude * pi_180);
	var pixelY = (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) /(pi_4)) * 256;
	var pixelX = ((longitude + 180) / 360) * 256;

	var pixel =  { x: pixelX, y: pixelY};
	
	return pixel;
}


function initBuffers(e) {
	//Custom Load
	var rawData = new Float32Array( (2 * points.features.length) );
	for (var i = 0; i < points.features.length; i++) {
		var loc = points.features[i].geometry.coordinates;
		
		var pixelCoordinate = latLongToPixelXY(loc[1], loc[0] );
		
		rawData[(i * 2)] = pixelCoordinate.x;
		rawData[(i * 2) + 1] = pixelCoordinate.y;
	}
	
	// create webgl buffer, bind it, and load rawData into it
	pointArrayBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, pointArrayBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, rawData, gl.STATIC_DRAW);

	// enable the 'worldCoord' attribute in the shader to receive buffer
	var attributeLoc = gl.getAttribLocation(pointProgram, 'worldCoord');
	gl.enableVertexAttribArray(attributeLoc);

	// tell webgl how buffer is laid out (pairs of x,y coords)
	gl.vertexAttribPointer(attributeLoc, 2, gl.FLOAT, false, 0, 0);
}


function initGL(canvas) {
	try {
		gl = canvas.getContext('webgl');
		gl.viewport(0, 0, canvas.width, canvas.height);
		
		gl.disable(gl.DEPTH_TEST);
	} catch(e) {
	}
	if (!gl) {
		alert("Could not initialise WebGL, sorry :-( ");
	}
}

function initShaders() {
	var fragmentShader = getShader(gl, "pointFragmentShader");
	var vertexShader = getShader(gl, "pointVertexShader");
	
	// link shaders to create our program
	pointProgram = gl.createProgram();
	gl.attachShader(pointProgram, vertexShader);
	gl.attachShader(pointProgram, fragmentShader);
	gl.linkProgram(pointProgram);

	gl.useProgram(pointProgram);

	gl.aPointSize = gl.getAttribLocation(pointProgram, "aPointSize");
}


function getShader(gl, id) {
	var shaderScript = document.getElementById(id);
	if (!shaderScript) {
		return null;
	}

	var str = "";
	var k = shaderScript.firstChild;
	while (k) {
		if (k.nodeType == 3)
			str += k.textContent;
		k = k.nextSibling;
	}

	var shader;
	if (shaderScript.type == "x-shader/x-fragment") {
		shader = gl.createShader(gl.FRAGMENT_SHADER);
	} else if (shaderScript.type == "x-shader/x-vertex") {
		shader = gl.createShader(gl.VERTEX_SHADER);
	} else {
		return null;
	}

	gl.shaderSource(shader, str);
	gl.compileShader(shader);

	if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
		alert(gl.getShaderInfoLog(shader));
		return null;
	}

	return shader;
}  

function scaleMatrix(matrix, scaleX, scaleY) {
	// scaling x and y, which is just scaling first two columns of matrix
	matrix[0] *= scaleX;
	matrix[1] *= scaleX;
	matrix[2] *= scaleX;
	matrix[3] *= scaleX;

	matrix[4] *= scaleY;
	matrix[5] *= scaleY;
	matrix[6] *= scaleY;
	matrix[7] *= scaleY;
}

function translateMatrix(matrix, tx, ty) {
	// translation is in last column of matrix
	matrix[12] += matrix[0]*tx + matrix[4]*ty;
	matrix[13] += matrix[1]*tx + matrix[5]*ty;
	matrix[14] += matrix[2]*tx + matrix[6]*ty;
	matrix[15] += matrix[3]*tx + matrix[7]*ty;
}

function drawPoints(e) {
	if (points == null) return;
		
	gl.clear(gl.COLOR_BUFFER_BIT);
	gl.enable(gl.BLEND);
	gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
	
	var currentZoom = map.getZoom();
	var pointSize = Math.max(currentZoom - 5.0, 1.0);
	gl.vertexAttrib1f(gl.aPointSize, pointSize);

	/**
	 * We need to create a transformation that takes world coordinate
	 * points in the pointArrayBuffer to the coodinates WebGL expects.
	 * 1. Start with second half in pixelsToWebGLMatrix, which takes pixel
	 *     coordinates to WebGL coordinates.
	 * 2. Scale and translate to take world coordinates to pixel coords
	 * see https://developers.google.com/maps/documentation/javascript/maptypes#MapCoordinate
	 */
	
	// copy pixel->webgl matrix
	mapMatrix.set(pixelsToWebGLMatrix);
	
	// Scale to current zoom (worldCoords * 2^zoom)
	var scale = Math.pow(2, currentZoom);
	scaleMatrix(mapMatrix, scale, scale);
	
	var offset = latLongToPixelXY(map.getBounds().getNorthWest().lat, map.getBounds().getNorthWest().lng);
	translateMatrix(mapMatrix, -offset.x, -offset.y);
	
	// attach matrix value to 'mapMatrix' uniform in shader
	var matrixLoc = gl.getUniformLocation(pointProgram, 'mapMatrix');
	gl.uniformMatrix4fv(matrixLoc, false, mapMatrix);

	// draw!
	gl.drawArrays(gl.POINTS, 0, points.features.length);
}


/**** BELOW ARE FUNCTIONS TO DRAW MAP USING JUST HTLM 5 CANVAS ****/

/*function drawMapCanvas(e) {
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
*/


/*function drawRectangle(imageData, x, y, r, g, b, a) {

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

*/

