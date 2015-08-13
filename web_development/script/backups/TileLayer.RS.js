L.TileLayer.RS = L.TileLayer.Canvas.extend({
	options: {
		tileSize:'256',
		radius:'4'
	},

	initialize: function (options, data) {
		L.Util.setOptions(this, options);
		this.options.async=true;
		
	},
	drawTile: function(canvas, tilePoint, zoom) {	
		this.ctx = canvas.getContext('2d');
		this._getPoints(tilePoint);
		
		this.tileDrawn(canvas);
	},
	addData: function (dataset) {
		this.data = dataset;
	},
	_getBounds: function(tilePoint) {

		var size = this.options.tileSize;
		var radius = this.options.radius;
		
		var nwPoint = tilePoint.multiplyBy(size);
		
		var swPoint = nwPoint.add(new L.Point(0,size));
		var nePoint = nwPoint.add(new L.Point(size, 0));
		
		swPoint = swPoint.subtract(new L.Point(radius, radius));
		nePoint = nePoint.subtract(new L.Point(radius, radius));
		
		var bounds = new L.LatLngBounds(this._map.unproject(swPoint), this._map.unproject(nePoint));
		return bounds;
	},

	_getPoints: function(tilePoint) {
		len = this.data.features.length;

		var bounds = this._getBounds(tilePoint); 

		for (var i = 0; i < len; i++) {	
			var temp = this.data.features[i].geometry.coordinates;
			var canvasPoint = this._canvasCoords(temp, tilePoint);

			if(bounds.contains(new L.LatLng(temp[1], temp[0]))) {
				this._drawPoint(canvasPoint, tilePoint);
			}
		}
	},

	_drawPoint: function (canvasPoint, tilePoint) {
		this.ctx.beginPath();
		this.ctx.arc(canvasPoint[0],canvasPoint[1], this.options.radius, 0, 2 * Math.PI, false);
		this.ctx.closePath();
		this.ctx.fillStyle = 'rgba(255, 0, 0, 0.5)';
		this.ctx.fill();
		this.ctx.stroke();
	},
	_canvasCoords: function(geojsonElement, tilePoint) {

		var p = this._map.project(new L.LatLng(geojsonElement[1], geojsonElement[0]));
		var start = tilePoint.multiplyBy(this.options.tileSize);
		
		var x = Math.round(p.x - start.x);
		var y = Math.round(p.y - start.y);
		
		var point =  [x, y];
		
		return point;
	},
	
});