SoundfileCanvasObject : SequenceableCanvasObject { 
  *new { arg item, canvasProps;
    ^super.new(item, canvasProps).init(item, canvasProps).initSf(item, canvasProps);
  }
  initSf { arg item, canvasProps;
    // props.waveform = Waveform(item.soundfile.soundfile, canvasProps.zoom.x);
    props.soundfile = item.soundfile;
  }

  getProps { arg item, canvasProps;
    var baseProps = super.getProps(item, canvasProps);
    ^baseProps.putAll((
      // waveformObjects: if (item.soundfile.notNil, {
      //   Mod(item.soundfile).getWaveform(
      //     canvasProps.zoom.x * canvasProps.bps,
      //     canvasProps['redraw']
      //   )
      // }, {
      //   (
      //     selectedWaveformObject: (waveform: [], complete: true),
      //     previousWaveformObject: (waveform: [], complete: true)
      //   )
      // }),
      startPos: item.startPos,
    ))
  }
  getItemParams { arg item, canvasProps;
    var baseParams = super.getItemParams(item, canvasProps);
    ^baseParams.putAll((
      soundfile: item.soundfile,
      startPos: props.startPos,
    ))
  }
  
  renderView { arg props, canvasProps; 
    super.renderView(props, canvasProps);
    this.renderWaveform(props, canvasProps);
  }
  
  onDragStart { arg aMouseAction;
    props.initialStartPos = props.startPos;
  }

  dragProps { arg aMouseAction;
    if (aMouseAction.modifiers == 524288) {
      var currentWF = props.waveformObjects.selectedWaveformObject;
      var waveform = currentWF.waveform;
      var waveformSize = waveform.size;
      var startPosInPixelFrames = (props.initialStartPos * waveformSize);
      var newStartPosInPixelFrames = startPosInPixelFrames - aMouseAction.mouseDelta.x;
      var newStartPos = newStartPosInPixelFrames / waveformSize;
      ^(startPos: newStartPos.clip(0.0, 1.0))
    }
    ^super.dragProps(aMouseAction);
  }

  renderWaveform { arg props, canvasProps; 

    var renderBounds = props.renderBounds;
    // renderBounds, origin, zoom, canvasBounds, color, label, selected, waveformObjects, startPos, canvasProps;
    // var currentWF = waveformObjects.selectedWaveformObject;
    // var waveform = if ((currentWF.status.notNil && currentWF.status), {
    //   currentWF.waveform
    // }, {
    //   waveformObjects.previousWaveformObject.waveform
    // });
    var wf = Waveform(props.soundfile.getData, props.soundfile.soundfile, canvasProps.zoom.x);
    var waveform = wf.array;

		var height = renderBounds.height;
		var waveformColor = props.color.multiply(Color(0.5, 0.5, 0.5));

		Pen.smoothing = true;
		Pen.strokeColor = waveformColor;

		if (waveform.size > 0, {
			var middlePoint = (renderBounds.leftTop + renderBounds.leftBottom) / 2;
			var waveformSize = waveform.size;
			var framesToRender = ((1 - props.startPos) * waveformSize).floor.asInteger;
			var firstFrame = (props.startPos * waveformSize).floor.asInteger;
      var amp = item.use { ~amp.value ?? 1 };

			min(renderBounds.width, framesToRender).do { arg index;
				var data = waveform[index + firstFrame];
				var max = middlePoint + Point(0, data[0] * height * amp / 2);
				var min = middlePoint + Point(0, data[1] * height * amp / 2);

				Pen.line(max, min);
				Pen.fillStroke;
				middlePoint.x = middlePoint.x + 1;
			}
    });
	}
  getItemEditView {
		var view = super.getItemEditView
			.putSpec('startPos', ControlSpec(0, 1, 'lin'));
		^view;
	}
  getContextMenuActions {
    var actions = super.getContextMenuActions();
    ^actions ++ [
      MenuAction("edit soundfile", {
        var sf = Mod(item.soundfile).soundfile;
        SoundfileEditor(sf, sf.numFrames * item.startPos, sf.numFrames); 
      });
    ];
  }
}
