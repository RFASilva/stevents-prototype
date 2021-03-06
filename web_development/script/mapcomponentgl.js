




var gl;   // The webgl context, to be initialized in init().
var prog; // Identifies the webgl program.
var vertexAttributeBuffer;    // Identifies the databuffer where vertex coords are stored.
var vertexAttributeLocation;  // Identifies the vertex attribute variable in the shader program.
var pointSizeUniformLocation; // Identifies the uniform that controls the size of points.
var antialiasedLoc;           // Identifies the uniform that determines whether points are antialiased.
var transformUniformLocation; // Identifies the coordinate matrix uniform variable.

var pointRadius;   // The radius of the points; half the point size.  This is the min of 16 and half the maximum point size.
var pointCt = 10;  // The number of points to drawn.
var points = new Float32Array(2*pointCt);      // The coordinates of the points, which change from frame to frame.
var velocities = new Float32Array(2*pointCt);  // The velocities of the points, which determine how far they move in each frame.
                                               //   Velocities don't change, except they reverse when the ball hits a wall.



/**
 * Draw a rectangle, with object texture coords that will map the entire texture onto
 * the rectangle (assuming that object texture coordinates are used).
 */
function draw() {
   gl.clearColor(0,0,0,1);
   gl.clear(gl.COLOR_BUFFER_BIT);
   gl.bindBuffer(gl.ARRAY_BUFFER,vertexAttributeBuffer);
   gl.bufferData(gl.ARRAY_BUFFER, points, gl.DYNAMIC_DRAW);
   gl.vertexAttribPointer(vertexAttributeLocation, 2, gl.FLOAT, false, 0, 0);
   gl.enableVertexAttribArray(vertexAttributeLocation);
   gl.drawArrays(gl.POINTS, 0, pointCt);
}


/**
 * Called by init to create the points and their velocities.  The velocities
 * detrmine how fast the points move during the animation.  (Since the corrdinate
 * system that is used is standard pixel coordinates, the unit of measure is
 * pixels, and the velocity is given in pixels per second.)
 */
function createPoints() {
   var width = gl.canvas.width;
   var height = gl.canvas.height;
   
   
   for (var i = 0; i < pointCt; i++) {
       points[2*i] = pointRadius + Math.random()*(width-2*pointRadius);
       points[2*i+1] = pointRadius + Math.random()*(height-2*pointRadius);
//       velocities[2*i] = 30+120*Math.random();
//       if (Math.random() < 0.5)
//          velocities[2*i] = - velocities[2*i];
//       velocities[2*i+1] = 30+120*Math.random();
//       if (Math.random() < 0.5)
//          velocities[2*i+1] = - velocities[2*i+1];
   }
}


/**
 * Applies a coordinate transformation to the webgl context by setting the value
 * of the coordinateTransform uniform in the shader program.  The canvas will display
 * the region of the xy-plane with x ranging from xmin to xmax and y ranging from ymin
 * to ymax.  If ignoreAspect is true, these ranges will fill the canvas.  If ignoreAspect
 * is missing or is false, one of the x or y ranges will be expanded, if necessary,
 * so that the aspect ratio is preserved.  
 */
function coordinateTransform(xmin, xmax, ymin, ymax, ignoreAspect) {
      if ( ! ignoreAspect) {
         var displayAspect = gl.canvas.height / gl.canvas.width; 
         var requestedAspect = Math.abs((ymax-ymin)/(xmax-xmin));
         if (displayAspect > requestedAspect) {
            var excess= (ymax-ymin) * (displayAspect/requestedAspect - 1);
            ymin -= excess/2;
            ymax += excess/2;
         }
         else if (displayAspect < requestedAspect) {
            var excess = (xmax-xmin) * (requestedAspect/displayAspect - 1);
            xmin -= excess/2;
            xmax += excess/2;
         } 
      }
      var coordTrans = [
           2/(xmax-xmin),            0,                       0,
           0,                        2/(ymax-ymin),           0,
           -1 - 2*xmin/(xmax-xmin),  -1 - 2*ymin/(ymax-ymin), 1
      ];
      gl.uniformMatrix3fv( transformUniformLocation, false, coordTrans );
}



var animator;
var lastTime = 0;  // Time of previous call to doFrame();

/**
 *  Do one frame of an animation, and call setTimeout to schedule the next
 *  frame.  But don't do anything if animating is false.  In each frame,
 *  each point's current velocity is added to its current position.  If
 *  This puts the boundary of the ball of outside the canvas, then the
 *  velocity is modified to make the ball bounce off the wall.
 */
function doFrame(time) {  
   var deltaTime = (time - lastTime)/1000; // Time since last doFrame, in seconds.
   lastTime = time;
   for (var j = 0; j < pointCt; j++) {
      var i = 2*j;
      points[i] += deltaTime * velocities[i];
      if (points[i]-pointRadius < 0)
         velocities[i] = Math.abs(velocities[i]);
      else if (points[i] + pointRadius > gl.canvas.width)
         velocities[i] = - Math.abs(velocities[i]);
      var i = 2*j + 1;
      points[i] += deltaTime * velocities[i];
      if (points[i]-pointRadius < 0)
         velocities[i] = Math.abs(velocities[i]);
      else if (points[i] + pointRadius > gl.canvas.height)
         velocities[i] = - Math.abs(velocities[i]);
   }
   draw();
 }



/**
 * This function is called when the user changes the setting of a
 * checkbox that controlls whether the fragment shader anti-aliases
 * the boundary of the points that it draws.
 */
function doAntialias() { 
   var antialiased = document.getElementById("antialiascheckbox").checked? 1 : 0; 
   gl.uniform1f(antialiasedLoc, 1);
   draw();
}


/**
 * Initializes the WebGL program including the relevant global variables
 * and the WebGL state.  Calls draw() to draw the picture for the first time.
 */
function webGLStart() {
//   document.getElementById("animatecheckbox").checked = false; // (Required for reload in firefox.)
//   document.getElementById("antialiascheckbox").checked = true;
   try {
       gl = createWebGLContext("canvas");
       var vertexShaderSource = getElementText("vshader"); 
       var fragmentShaderSource = getElementText("fshader");
       prog = createProgram(gl,vertexShaderSource,fragmentShaderSource);
       
       gl.useProgram(prog);
       
       vertexAttributeLocation =  gl.getAttribLocation(prog, "vertexCoords");
       transformUniformLocation =  gl.getUniformLocation(prog, "coordinateTransform");
       pointSizeUniformLocation = gl.getUniformLocation(prog, "pointSize");
       antialiasedLoc = gl.getUniformLocation(prog, "antialiased");
       gl.uniform1f(antialiasedLoc, 1);
       coordinateTransform(0, gl.canvas.width, gl.canvas.height, 0);  // Let's me use standard pixel coords.
       
       
       vertexAttributeBuffer = gl.createBuffer();
       var pointSizeRange = gl.getParameter(gl.ALIASED_POINT_SIZE_RANGE);
       
       pointRadius = /*Math.min(pointSizeRange[1]/2, 16) */ 3;
       gl.uniform1f(pointSizeUniformLocation, pointRadius * 2);
       
       gl.blendFuncSeparate( gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA, gl.ZERO, gl.ONE );
       gl.enable(gl.BLEND);
       
       createPoints();
       
   }
   catch (e) {
      alert("Could not initialize WebGL! " + e);
      return;
   }
   draw();
}

/* Web GL Example to draw a triangle and a square xD
 * 
 * 
 var canvas;

var triangleVertexPositionBuffer;
var squareVertexPositionBuffer;

var gl;
var shaderProgram;
var mvMatrix = mat4.create();
var pMatrix = mat4.create();



function webGLStart() {
	var canvas = document.getElementById("canvas");
	initGL(canvas);

	initShaders();
	initBuffers();

	gl.clearColor(0.0, 0.0, 0.0, 1.0);
	gl.enable(gl.DEPTH_TEST);

	drawScene();
}

function initBuffers() {
	triangleVertexPositionBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, triangleVertexPositionBuffer);

	var vertices = [
	                0.0,  1.0,  0.0,
	                -1.0, -1.0,  0.0,
	                1.0, -1.0,  0.0
	                ];


	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);

	triangleVertexPositionBuffer.itemSize = 3;
	triangleVertexPositionBuffer.numItems = 3;



	squareVertexPositionBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, squareVertexPositionBuffer);
	vertices = [
	            1.0,  1.0,  0.0,
	            -1.0,  1.0,  0.0,
	            1.0, -1.0,  0.0,
	            -1.0, -1.0,  0.0
	            ];
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
	squareVertexPositionBuffer.itemSize = 3;
	squareVertexPositionBuffer.numItems = 4;

}

function drawScene() {
	gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	mat4.perspective(45, gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
	mat4.identity(mvMatrix);
	mat4.translate(mvMatrix, [-1.5, 0.0, -7.0]);

	gl.bindBuffer(gl.ARRAY_BUFFER, triangleVertexPositionBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, triangleVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

	setMatrixUniforms();
	gl.drawArrays(gl.TRIANGLES, 0, triangleVertexPositionBuffer.numItems);

	mat4.translate(mvMatrix, [3.0, 0.0, 0.0]);

	gl.bindBuffer(gl.ARRAY_BUFFER, squareVertexPositionBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, squareVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

	setMatrixUniforms();

	gl.drawArrays(gl.TRIANGLE_STRIP, 0, squareVertexPositionBuffer.numItems);
}

function initGL(canvas) {
	try {
		gl = canvas.getContext("experimental-webgl");
		gl.viewportWidth = canvas.width;
		gl.viewportHeight = canvas.height;
	} catch(e) {
	}
	if (!gl) {
		alert("Could not initialise WebGL, sorry :-( ");
	}
}

function initShaders() {
	var fragmentShader = getShader(gl, "shader-fs");
	var vertexShader = getShader(gl, "shader-vs");

	shaderProgram = gl.createProgram();
	gl.attachShader(shaderProgram, vertexShader);
	gl.attachShader(shaderProgram, fragmentShader);
	gl.linkProgram(shaderProgram);

	if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
		alert("Could not initialise shaders");
	}

	gl.useProgram(shaderProgram);

	shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
	gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);

	shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
	shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
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

function setMatrixUniforms() {
    gl.uniformMatrix4fv(shaderProgram.pMatrixUniform, false, pMatrix);
    gl.uniformMatrix4fv(shaderProgram.mvMatrixUniform, false, mvMatrix);
  }
*/

