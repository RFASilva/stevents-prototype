var myip = "localhost";
var myport = "8080";
var dataset = "fires_portugal"; //fires_portugal
var granularity = "month";
var graph;
var xPadding = 50; // espacamento nas labels do eixo dos y
var yPadding = 20; // Espaçamento 

var timeseries;
var xaxis = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"];
var ts;


function getMaxY() {
	var max = 0;

	for(var i = 0; i < ts.data.length; i++) {
		if(ts.data[i].value > max) {
			max = ts.data[i].value;
		}
	}

	max += 10 - max % 10;
	
	max += 100;
	return max;
}

function getXPixel(val) {
	return ((graph.width() - xPadding) / xaxis.length) * val + (xPadding * 1.5);
}

function getYPixel(val) {
	return graph.height() - (((graph.height() - yPadding) / getMaxY()) * val) - yPadding;
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


	for(var i = 0; i < xaxis.length; i++) {
		c.fillText(xaxis[i], getXPixel(i), graph.height() - yPadding + 20);
	}

	//Draw y axes legend
	c.textAlign = "right"
		c.textBaseline = "middle";

	for(var i = 0; i < getMaxY(); i += 400) {
		c.fillText(i, xPadding - 10, getYPixel(i));
	}

	c.fillStyle = 'rgba(255,0, 0, 0.2)';

	for(var i = 0; i < ts.data.length; i ++) {  
		c.beginPath();
		
		var date = ts.data[6].DateTime;
		var month = ts.data[i].DateTime.split("-")[1];
		
		var pos = parseInt(month) - 1;
		
		c.arc(getXPixel(pos), getYPixel(ts.data[i].value), 5, 0, Math.PI * 2, true);
		c.fill();
	}
}


function getData() {
	requestTimeUrl = "http://"+ myip + ":" + myport + "/timedata?tableName=timeseries" +  dataset + granularity + "&isRestricted=false&granularity=" + granularity;
	getTimeSeriesJSON(requestTimeUrl);
}

function getTimeSeriesJSON(url) {
	$.getJSON(url, function( data ) {
		timeseries = data;
		ts = timeseries[6];
		drawGraph();
		// Update min and max values for each time series
//		updateMinMaxValues();

//		initTimeSeries(0,0,false);

	});
}