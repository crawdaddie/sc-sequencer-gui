SequencerGrid {
	classvar mainGridColor;
	classvar subdivisionColor;
  classvar noteNames;
  classvar notes;

	var <tick;
  var pianoRollRenderer;

	*new {
		^super.new.init()
	}

	*initClass {
		mainGridColor = Color.grey(0.7, 1);
		subdivisionColor = Color.grey(0.7, 0.5);
    notes = "wbwbwwbwbwbw".reverse;
    noteNames = ["c","c#","d","d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"].reverse;
	}

	init {}

  renderPianoRoll { arg row, canvasBounds, yOffset, gap;
    if (notes[row % 12] == $b, {
      Pen.addRect(Rect(0, yOffset, canvasBounds.width, gap));
      Pen.color = Color.grey(0.7, 0.5);
      Pen.draw;
    });
    Pen.stringInRect(noteNames[row % 12], Rect(0,yOffset, 20, gap), font: Theme.font, color: Theme.grey);
  }


	drawYGrid { arg origin, canvasBounds, zoom, pianoRoll;
		var gap = Theme.verticalUnit * zoom.y;
		var yOffset = origin.y + (0 - origin.y).roundUp(gap);
    var row = 0;

		Pen.strokeColor_(mainGridColor);
     
		while ({ yOffset < canvasBounds.height }) {
      if (pianoRoll, { this.renderPianoRoll(row, canvasBounds, yOffset, gap) });

		  Pen.strokeColor_(mainGridColor);
			Pen.line(Point(0, yOffset), Point(canvasBounds.width, yOffset));
			Pen.stroke;
			yOffset = yOffset + gap;
      row = row + 1;
		}
	}

	drawXGrid { arg quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions;
		var gap = quantX * zoom.x;
		var timingOffsetPixels = timingOffset * gap;
		var initXOffset = origin.x + (0 - origin.x).roundUp(gap) - timingOffsetPixels;
		var xOffset = initXOffset;
		
		var minorGap = gap / quantSubdivisions;
		var initSubOffset = origin.x + (0 - origin.x).roundUp(minorGap) - timingOffsetPixels; 
		var subOffset = initSubOffset;
		// var tickNum = 0;
		tick = minorGap;

		
		Pen.strokeColor_(mainGridColor);
		while ({ xOffset < canvasBounds.width }) {
			Pen.line(Point(xOffset, 0), Point(xOffset, canvasBounds.height));
			// Pen.stroke;
			xOffset = xOffset + gap;
		};


		Pen.strokeColor_(subdivisionColor);
		while ({ subOffset < canvasBounds.width }) {
			Pen.line(Point(subOffset, 0), Point(subOffset, canvasBounds.height));
			
			subOffset = subOffset + minorGap;
			// tickNum = tickNum + 1;
		}; 
		Pen.stroke;
	}

	renderView { arg quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions = 1, pianoRoll = false;
		this.drawYGrid(origin, canvasBounds, zoom, pianoRoll);
		this.drawXGrid(quantX, origin, timingOffset, canvasBounds, zoom, quantSubdivisions);
	}

	render { arg ctx;
		^this.performWithEnvir('renderView', ctx)
	}
  onClose {
    // no-op
  }
}
