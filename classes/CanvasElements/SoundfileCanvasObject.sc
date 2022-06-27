SoundfileCanvasObject : SequenceableCanvasObject { 
  *new { arg item, canvasProps;
    ^super.new(item, canvasProps).init(item, canvasProps);
  }

  getProps { arg item, canvasProps;
    var baseProps, module, ref;
    baseProps = super.getProps(item, canvasProps);
    ref = item.soundfileRef;

    module = Mod(ref.path, loader: ref.loaderPath);
    ^(baseProps ++ (
      soundfile: module,
      startPos: item.startPos,
    ))
  }

  getItemParams { arg item, canvasProps;
    var baseParams = super.getItemParams(item, canvasProps);
    
    ^baseParams.putAll((
      startPos: props.startPos,
      soundfileRef: props.soundfile.ref
    ))
  }
  
  renderView { 
    super.renderView(props, canvasProps);
    this.renderWaveform(props, canvasProps);
  }
  
  onDragStart { arg aMouseAction;
    props.initialStartPos = props.startPos;
  }

  dragProps { arg aMouseAction;
    if (aMouseAction.modifiers == 524288) {
      var wf = Waveform(props.soundfile.getData, props.soundfile.soundfile, canvasProps.zoom.x);
      var waveformSize = wf.array.size;
      var startPosInPixelFrames = (props.initialStartPos * waveformSize);
      var newStartPosInPixelFrames = startPosInPixelFrames - aMouseAction.mouseDelta.x;
      var newStartPos = newStartPosInPixelFrames / waveformSize;

      ^(startPos: newStartPos.clip(0.0, 1.0))
    }
    ^super.dragProps(aMouseAction);
  }

  renderWaveform { arg props, canvasProps; 
    var renderBounds, wf, waveform, height, waveformColor;
    renderBounds = props.renderBounds;
    wf = Waveform(props.soundfile.getData, props.soundfile.soundfile, canvasProps.zoom.x, canvasProps['redraw']);
    waveform = wf.array;
		height = renderBounds.height;
		waveformColor = props.color.multiply(Color(0.5, 0.5, 0.5));

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
