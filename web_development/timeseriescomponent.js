//Global Variables to support construction of range queries
var rectX, mousedownpos, mov = 0;
var offsetX;
var offsetY;
var ctx;
var isRange = false, isMoving= false, isMouseDown= false, isDrawing = false;
var rangeWidth;

//Variables to implement zoom on timmeseries
var scrollTimes = 0;
var clipStart, clipEnd;
var maxValues = new Array();
var minValues = new Array();

var posTimeSeries;

//Time series JSON
var timeseriesJSON;

//Context
var granularity;
var dataset;

var temp = 0;

//To be used on the scroll bar position issue
var scrollPosition = 0;


function getTimeSeriesJSON(url) {
	$.getJSON(url, function( data ) {
		timeseriesJSON = data;

		// Update min and max values for each time series
		updateMinMaxValues();

		initTimeSeries(0,0,false);

		// Variables need for timeseries zoom feature
		clipStart = 0;
		clipEnd = timeseriesJSON[0].data.length;

		// Variables for canvas interaction
		isMouseDown = false;
		isRange = false;
		isMoving = false;
		isDrawing = false;
		rectX = 0;
		mov = 0;
	});
}

function updateMinMaxValues() {
	for (var j = 0; j < timeseriesJSON.length; j++) {
		var min = 0, max = 0;
		for (var i = 0; i < timeseriesJSON[0].data.length; i++) {
			var value = timeseriesJSON[j].data[i].value;
			if (value > max) max = value;
			if (value < min) min = value;
		}
		minValues[j] = min;
		maxValues[j] = max;
	}
}


//Cria os elementos html necessarios para representar todas as series temporais
function initTimeSeries(indexBegin, indexEnd, sliced) {
	$("#timeseries").empty(); //To clear div
	interactionTimeCanvas(sliced);
	computeTimeHTMLCode(indexBegin,indexEnd,sliced);
	$("#timeseries").scrollTop(scrollPosition);
}

function interactionTimeCanvas(sliced) {
	canvas = document.createElement('canvas');
	canvas.id = 'chartscanvas';
	canvas.height = $('#timeseries')[0].scrollHeight;
	canvas.width = $('#timeseries').width();
	canvas.style.position="absolute";
//	canvas.style.zIndex="100";
	var timeDiv = $('#timeseries');
	timeDiv.append(canvas);
	ctx = canvas.getContext('2d');

	var offset = $("#chartscanvas").offset();
	offsetX = offset.left;
	offsetY = offset.top;
	canvas.onselectstart = function () { return false; }

	// Handle with the event mouse move
	$('#chartscanvas').on('mousemove', function(e){

		if(isMouseDown) {
			if(isRange)
				isMoving = true;
			else {
				isDrawing = true;
			}
		}

		var x = parseInt(e.clientX - offsetX);
		ctx.clearRect(0, 0, this.width, this.height);

//		console.log("height: " + this.height);

		ctx.lineWidth = 0.5;
		ctx.beginPath();
		ctx.moveTo(x,0);
		ctx.lineTo(x,this.height);
		ctx.stroke();


		if(!sliced)
			posTimeSeries = Math.round((x * timeseriesJSON[0].data.length) / timeseriesWidth);
		else {
			var range = (clipEnd - clipStart);
			posTimeSeries = Math.round((x * range) / timeseriesWidth) + clipStart;
		}

		for (var j = 0; j < timeseriesJSON.length; j++) {

			if(posTimeSeries >=0 && posTimeSeries <= timeseriesJSON[0].data.length) {
				$("#numelements" + j + ":text").val(timeseriesJSON[j].data[posTimeSeries].value);
				$("#legend:text").val(timeseriesJSON[j].data[posTimeSeries].DateTime);
			}
		}

		// Draw a rectangle
		if(isDrawing) {
			rangeWidth = x-mousedownpos;
			ctx.fillStyle = 'rgba(205,201, 201, 0.5)';
			ctx.fillRect(mousedownpos,0,rangeWidth,this.height);
			ctx.fill();
			return;
		}
		else if(isMoving) {
			mov = x - mousedownpos;
			ctx.clearRect(0, 0, this.width, this.height);
			ctx.fillStyle = 'rgba(205,201, 201, 0.5)';
			ctx.fillRect(rectX + mov,0,rangeWidth,this.height);
			ctx.fill();
			return;
		}
		if(isRange) {
			ctx.fillStyle = 'rgba(205,201, 201, 0.5)';
			ctx.fillRect(rectX,0,rangeWidth,this.height);
			ctx.fill();
			return;
		}

	});

	//handle with the event mouse down
	$("#chartscanvas").mousedown(function (e) {
		isMouseDown= true;
		mousedownpos = parseInt(e.clientX - offsetX);
	});

	//handle with the event mouse up
	$("#chartscanvas").mouseup(function (e) {
		isMouseDown = false;

		if(isDrawing) {
			isRange = true;
			isDrawing= false;
			rectX = mousedownpos + mov;

			var posInit = Math.round((rectX * timeseriesJSON[0].data.length) / timeseriesWidth) + 1;
			var posEnd = Math.round(((rectX + rangeWidth) * timeseriesJSON[0].data.length) / timeseriesWidth) + 1;

			requesturl = "http://"+ myip + ":" + myport + "/spatialdata?posInit=" + posInit + "&posEnd=" + posEnd + "&posEnd=-1&granularity=" + granularity + "&tableName=" + dataset;
			getGeoJSON(requesturl);
		}

		if(isMoving) {
			rectX = rectX + mov;
			isRange = true;
			isDrawing= false;
			isMoving = false;

			var posInit = Math.round((rectX * timeseriesJSON[0].data.length) / timeseriesWidth) + 1;
			var posEnd = Math.round(((rectX + rangeWidth) * timeseriesJSON[0].data.length) / timeseriesWidth) + 1;

			requesturl = "http://"+ myip + ":" + myport + "/spatialdata?posInit=" + posInit + "&posEnd=" + posEnd + "&posEnd=-1&granularity=" + granularity + "&tableName=" + dataset;
			getGeoJSON(requesturl);
		}

		if(!isRange) {
			var x =  parseInt(e.clientX - offsetX);
			if(!sliced) {
				posTimeSeries = Math.round((x * timeseriesJSON[0].data.length) / timeseriesWidth);
			}
			else {
				var range = (clipEnd - clipStart) + 1;
				posTimeSeries = Math.round((x * range) / timeseriesWidth) + (clipStart + 1);
			}

			for (var j = 0; j < timeseriesJSON.length; j++) {
				if(posTimeSeries >=0 && posTimeSeries <= timeseriesJSON[0].data.length) {
					$("#numelements" + j + ":text").val(timeseriesJSON[j].data[posTimeSeries].value);
					$("#legend:text").val(timeseriesJSON[j].data[posTimeSeries].DateTime); 

				}
			}

//			console.log("query espacial instante tempo");
			requesturl = "http://"+ myip + ":" + myport + "/spatialdata?posInit=" + (posTimeSeries + 1) + "&posEnd=-1&granularity=" + granularity + "&tableName=" + dataset;
			getGeoJSON(requesturl);
		}
	});

	$("#chartscanvas").dblclick(function (e) {
		isMouseDown = false;
		isRange = false;
		isMoving = false;
		isDrawing = false;
		rectX = 0;
		mov = 0;
	});

	$("#timeseries").on('mousewheel', function(e) {
		e = e ? e : window.event;
		
		clearTimeout($.data(this, 'timer'));

		isRange = false;
		isMoving = false;
		isDrawing = false;
		rectX = 0;
		mov = 0;

		//zooming 
		if(e.originalEvent.wheelDelta /120 > 0) {
			$.data(this, 'timer', setTimeout(function() {
				timeserieszoom("zoomin",e);
			}, 150));

		}
		//zoomout
		else {
			$.data(this, 'timer', setTimeout(function() {
				timeserieszoom("zoomout",e);
			}, 150));
		}
		
		cancelEvent(e);
		scrollPosition = $("#timeseries").scrollTop();
		
	});
}

function cancelEvent(e) {
	e = e ? e : window.event;
	if(e.stopPropagation)
		e.stopPropagation();
	if(e.preventDefault)
		e.preventDefault();
	e.cancelBubble = true;
	e.cancel = true;
	e.returnValue = false;
	return false;
}

function timeserieszoom(type,e) {

	var x = parseInt(e.clientX - offsetX);

	var range = (clipEnd - clipStart) + 1;
	var midPoint = Math.round((x * range) / timeseriesWidth) + (clipStart + 1);
	var timeserieslength = timeseriesJSON[0].data.length;

//	console.log("range: " + range);
//	console.log("mid point: " + midPoint);
//	console.log("timeserieswidht: " + timeseriesWidth);

	if(type == "zoomin") scrollTimes++;
	else scrollTimes--;

	if(scrollTimes <= 0) {
		initTimeSeries(0,0,false);
		scrollTimes = 0;
		clipStart = 0;
		clipEnd = timeseriesJSON[0].data.length;
		return;
	}

//	console.log("scroll Times: " + scrollTimes);
//	console.log("timeseriesWidth: " + timeserieslength);

	var range = Math.round(timeserieslength / (scrollTimes + 1)*2);

//	console.log("range: " + range)

	clipStart = Math.round(midPoint - (range /2));
	if(clipStart < 0)
		clipStart = 0;

	clipEnd = Math.round(midPoint + (range /2));
	if(clipEnd > timeseriesJSON[0].data.length)
		clipEnd = timeseriesJSON[0].data.length;

//	console.log("begins: " + clipStart + " ends: " +  clipEnd);
	initTimeSeries(clipStart,clipEnd,true);
}

function sleep(milliseconds) { 
	var start = new Date().getTime(); 
	for (var i = 0; i < 1e7; i++) { 
		if ((new Date().getTime() - start) > milliseconds){ break; } 
	} 
} 

function getTimeSeries() {

	dataset = ($("#dataset").val() != undefined) ? $("#dataset").val() : undefined;
	granularity = ($("#granularity").val() != undefined) ? $("#granularity").val() : undefined;
	// if there is no area restrictions
	if(!isRestricted) { 
//		console.log("dataset: " + dataset);
//		console.log("granularity: " + granularity);

		requestTimeUrl = "http://"+ myip + ":" + myport + "/timedata?tableName=timeseries" +  dataset + granularity + "&isRestricted=false&granularity=" + granularity;
		getTimeSeriesJSON(requestTimeUrl);
	}
	else {
		requestTimeUrl = "http://"+ myip + ":" + myport + "/timedata?tableName=timeseries" +  dataset + granularity + "&isRestricted=true&granularity=" + granularity;
		console.log("time request url: " + requestTimeUrl);
		getTimeSeriesJSON(requestTimeUrl);
	}
}

function computeTimeHTMLCode(indexBegin, indexEnd, sliced) {

	var begins = 0;
	var ends = timeseriesJSON[0].data.length;

	var colors = new Array('#FF0000', '#0000FF', '#FF00FF', '#FF9900', '#000000', '#336600', '#9933FF', '#AC7B62');
	var values = new Array();
	var index = 0;
	timeseriesWidth = Math.round($('#timeseries').innerWidth());
	var widthTimeSerie = Math.round($('#timeseries').innerWidth()).toString() + "px";

	var legendName = '<span style="position: absolute;	right:150px;font-size: 10pt" class="text">Date:</span>'; 
	var legendDate = '<input type="text" id="legend" style="position: absolute;	right: 1px;border: 0;font-size: 9pt" />';
	$('#timeseries').append(legendName);
	$('#timeseries').append(legendDate);

	if(sliced) {
		begins = indexBegin;
		ends = indexEnd;
	}


	for (var j = 0; j < timeseriesJSON.length; j++) {

		var temp = 0;
		for (var i = begins; i <  ends; i++) {
			values[temp] = timeseriesJSON[j].data[i].value;
			temp++;
		}

		var nameTimeSerie = timeseriesJSON[j].type;
		var elementName = '<span  style="font-size: 10pt" class="text">' + nameTimeSerie +": "+ '</span>'; 
		var inputValue = '<input type="text" id="numelements' + j +'" style="border: 0;font-size: 9pt" />';
		var elementTimeSerie = '<span id="sparkline'+j+'">&nbsp;</span>';

		$('#timeseries').append(elementName);
		$('#timeseries').append(inputValue);
		$('#timeseries').append(elementTimeSerie);

		$("#sparkline"+j).sparkline(values, {
			id: 1,
			type: 'line',
			width: widthTimeSerie,
			height: '50px',
			lineColor: colors[j],
			fillColor: colors[j],
			chartRangeMax: maxValues[j]
		});
	}

	canvas.height = $('#timeseries')[0].scrollHeight;
}