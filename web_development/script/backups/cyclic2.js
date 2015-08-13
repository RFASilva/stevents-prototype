var myip = "localhost";
var myport = "8080";
var dataset = "fires_portugal"; //fires_portugal
var granularity = "month";
var graph;
var xPadding = 50; // espacamento nas labels do eixo dos y
var yPadding = 30; // Espaçamento 

var timeseries;
var xaxis = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"];
var ts;

var distinctYearValues;


Array.prototype.unique = function () {
	var arr = this;
	return $.grep(arr, function (v, i) {
		return $.inArray(v, arr) === i;
	});
}


function evalDistinctYears() {
	result = [];
	var j = 0;
	for(var i = 0; i < ts.data.length; i ++) {  
		var date = ts.data[i].DateTime;
		var year = ts.data[i].DateTime.split("-")[0];

		result[j] = parseInt(year);
		j++;

	}
	return result.unique();
}

function getXPixel(val) {
	return ((graph.width() - xPadding) / xaxis.length) * val + (xPadding * 1.5);
}

function getYPixel(val) {
	return ((graph.height() - yPadding) / distinctYearValues.length) * val + (yPadding*0.5);
}


function drawGraph() {
	graph = $('#graph');
	var c = graph[0].getContext('2d');

	c.lineWidth = 2;
	c.strokeStyle = '#333';
	c.font = 'italic 8pt sans-serif';
	c.textAlign = "center";

	//Draw x axes legend
	c.beginPath();
	c.moveTo(xPadding, 0);
	c.lineTo(xPadding, graph.height() - yPadding);
	c.lineTo(graph.width(), graph.height() - yPadding);
	c.stroke();

	//Draw y axes legend
	c.textAlign = "right"
		c.textBaseline = "middle";

	for(var i = 0; i < distinctYearValues.length; i++) {
		c.fillText(distinctYearValues[i], xPadding - 20, getYPixel(i));
	}

	

	for(var i = 0; i < xaxis.length; i++) {
		c.fillText(xaxis[i], getXPixel(i), graph.height() - yPadding + 20);
	}

	var range = getMaxValue() / 5; // Assuming that our minimum value is 0
	
	for(var i = 0; i < ts.data.length; i++) {  
		var date = ts.data[i].DateTime;
		var year = ts.data[i].DateTime.split("-")[0];
		var month = ts.data[i].DateTime.split("-")[1];
		
//		if(month >= 2)
//			continue;
		
		console.log(parseInt(year));
		console.log(parseInt(month));
		
		var posYear = distinctYearValues.indexOf(parseInt(year));
		var posMonth = parseInt(month) - 1;
		
		c.beginPath();
		c.rect(getXPixel(posMonth)-xPadding/2, getYPixel(posYear)-yPadding/2, xPadding*0.75, yPadding);
		c.fillStyle = getColor(ts.data[i].value, range);
		c.fill();
		c.lineWidth = 1;
		c.strokeStyle = getColor(ts.data[i].value, range);
		c.stroke();
	}
	
	//Draw the set of lines for each year
	for(var i = 0; i < xaxis.length; i++) {
		//Draw x axes legend
		c.beginPath();
		c.moveTo(getXPixel(i) - xPadding*0.5, 0 );
		c.lineTo(getXPixel(i) - xPadding*0.5, graph.height()-yPadding);
		c.lineWidth = 1;
		c.strokeStyle = 'black';
		c.stroke();
	}
}

function getMaxValue() {
	var max = 0;

	for(var i = 0; i < ts.data.length; i++) {
		if(ts.data[i].value > max) {
			max = ts.data[i].value;
		}
	}

	return max;
}

function getColor(value, range) {
	var rgbValues = ['rgb(255,255,0)', 'rgb(255,220,0)', 'rgb(255,174,0)' ,'rgb(255,96,0)','rgb(255,32,0)'];
	
	for(var i = 0; i < 5; i++) {
		var temp = i*range;
		if(value >=temp && value < temp + range)
			return rgbValues[i];
	}
	
	return -1;
	
}

function getData() {
	requestTimeUrl = "http://"+ myip + ":" + myport + "/timedata?tableName=timeseries" +  dataset + granularity + "&isRestricted=false&granularity=" + granularity;
	getTimeSeriesJSON(requestTimeUrl);
}

function getTimeSeriesJSON(url) {
	$.getJSON(url, function( data ) {
		timeseries = data;
		ts = timeseries[6];

		distinctYearValues = evalDistinctYears();

		drawGraph();
	});
}