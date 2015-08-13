var widthC;

self.addEventListener('message', function(e) {
	var width = e.data.width;
	widthC = width;
	var height = e.data.height;

	var pixels = new Uint8ClampedArray((width*height)*4);
	var points = e.data.points;
	
	if(points.length > 0) {
		console.log("working... " + points.length);
		for (var i = 0; i < points.length; i++) {

			var pixelCoordinate = {x: points[i].x - e.data.centerX, y: points[i].y - e.data.centerY};
//			console.log(pixelCoordinate.x)
//			console.log(pixelCoordinate.y)
			var a = 120;
			var r = 255;
			var g = 0;
			var b = 0;

			drawRectangle(pixels, pixelCoordinate.x, pixelCoordinate.y, r, g, b, a);
		}

		
		var results =  {
				pixels: pixels,
				centerX: e.data.centerX,
				centerY: e.data.centerY,
				width: e.data.width,
				height: e.data.height
		};
		self.postMessage(results);
	}
	else self.postMessage(-1);
		

}, false);

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


function setPixel(img, x, y, r, g, b, a) {

	//find the pixel index based on it's coordinates
	index = (x + y * widthC) * 4;
	img[index + 0] = r;
	img[index + 1] = g;
	img[index + 2] = b;
	img[index + 3] = a;
}

function hexToR(h) {return parseInt((cutHex(h)).substring(0,2),16)}
function hexToG(h) {return parseInt((cutHex(h)).substring(2,4),16)}
function hexToB(h) {return parseInt((cutHex(h)).substring(4,6),16)}
function cutHex(h) {return (h.charAt(0)=="#") ? h.substring(1,7):h}