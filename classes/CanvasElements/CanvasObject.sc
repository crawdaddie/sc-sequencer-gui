CanvasObject {
	var props;

  onClose {
		Dispatcher.removeListenersForObject(this)
	}
  onDragStart { arg aMouseAction;
  }

	onDrag { arg aMouseAction;
	
	}

	onDragEnd { arg aMouseAction;
	
	}

	renderView {
	}

  resolveProps {
  }

	render { arg canvasProps;
    this.renderView(props, canvasProps)
	}

	select {
		props.selected = true
  }

	unselect {
		props.selected = false
	}

	selected {
		^props.selected;
	}

  getContextMenuActions {
    ^[
      MenuAction("cut", { props.postln }),
      MenuAction("copy", { props.postln }),
    ]
  }
}

SequenceableCanvasObject : CanvasObject {
  classvar widgetSize;
  var <id;
	var item;
	var props;
	var canvasProps;

  *initClass {
    widgetSize = 5;
  }

	*new { arg item, canvasProps;
		^super.new.init(item, canvasProps);
	}
  setItem { arg anItem;
    item = anItem;
    this.resolveProps;
  }

	init { arg anRxEvent, aCanvasProps;
    var p;
		item = anRxEvent;
    id = item.id;
    canvasProps = aCanvasProps;

		props = Props((
      color: Color.rand,
      selected: false,
      label: item.id.asString,
    ) ++ this.getProps(item, canvasProps))
    .onUpdate_({ arg ... args;
      this.resolveProps;
    });

    canvasProps.addDependant(props);
    this.addListeners(item);
	}

  renderView {
    var renderBounds = props.renderBounds;
		if (renderBounds.intersects(canvasProps.canvasBounds).not) { ^false };

		Pen.smoothing = true;
		Pen.addRect(renderBounds);
		Pen.color = props.color;
	  Pen.draw;

	  if (props.selected) {
			Pen.addRect(renderBounds);
   		Pen.strokeColor = Theme.darkGrey;
   		Pen.stroke;
		};

	  Pen.stringInRect(props.label, renderBounds, font: Theme.font, color: Theme.grey);
  }

  getProps { arg item, canvasProps; /*: Props */
    // translation item -> props
    ^(
			renderBounds: this.renderBounds(item, canvasProps.origin, canvasProps.zoom),
			redraw: canvasProps['redraw'],
		)
  }

  resolveProps {
    props.putAll(this.getProps(item, canvasProps))
  }

  getItemParams { arg props; /*: Item */
		var origin = canvasProps.origin;
		var zoom = canvasProps.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;

		
		var bounds = props.renderBounds
			.moveBy(-1 * origin.x, -1 * origin.y)
			.scaleBy(zoom.x.reciprocal, zoom.y.reciprocal);

		var itemParams = (
			time: bounds.left / xFactor,
			row: bounds.top / yFactor,
			sustain: bounds.width / xFactor
		);
    ^itemParams;
  }

  addObject { arg payload;
    "add object rect".postln;
    payload.postln;
    // var viewClass = this.getItemEmbedView(item);
    // var view = viewClass.new(item, props);
    // views = views.add(view);
    // canvas.refresh;
  }

  updateObject { arg payload;
    this.setItem(payload.object);
    canvasProps.redraw;
  }

  removeObject {
  }

  listen { arg type;
    Dispatcher.addListener(
      type, 
      this,
      { |p|
        if (p.id == item.id) { // item.id will remain stable throughout the underlying objects lifecycle
          // even if an object is 'updated' in an immutable way
          this.perform(type, p);
        } 
      }
    );
  }


  addListeners { arg object;
    this.listen('addObject');
    this.listen('updateObject');
    this.listen('removeObject');
 			//     , { arg payload;
			//       var item = payload.object;
			//       var viewClass = this.getItemEmbedView(item);
			// var view = viewClass.new(item, props);
			//       views = views.add(view);
			//       canvas.refresh();
			//     });
			//
			//     this.listen(Topics.objectDeleted, { arg payload;
			//       views = views.select({ arg view; view.id != payload.objectId });
			//       canvas.refresh();
			//     });
  }

  update { arg object, changer ... args;
    var action, payload;
    #action, payload = args;
    props.putAll(this.getProps(item, canvasProps));
    canvasProps.redraw();
  }

	contains { arg aPoint;
		^this.bounds(item, canvasProps.origin, canvasProps.zoom).contains(aPoint);
	}

	bounds { arg item, origin, zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;	
		var bounds = Rect(
			item.time * xFactor,
			item.row * yFactor,
			item.sustain * xFactor,
			yFactor
		);

		^bounds
			.scaleBy(zoom.x, zoom.y)
	}

	renderBounds { arg item, origin, zoom;
		^this.bounds(item, origin, zoom).moveBy(origin.x, origin.y);
	}

  onDragStart {
    props.initialBounds = props.renderBounds;
  }

  getMouseAction { arg aMouseAction
  /*
  * (
  *  modifiers: Number, 
  *  initialPosition: Point,
  *  mouseDelta: Point,
  *  position: Point,
  *  mouseMoveAction: Function,
  *  mouseUpAction: Function
  * )
  */
    ;
    var initialPosition = aMouseAction.initialCanvasPosition;
    var modifiers = aMouseAction.modifiers;
    var renderBounds = props.renderBounds;
    var modifierKey = Platform.keys.modKey;

    props.initialBounds = renderBounds;

    if (renderBounds.width < (widgetSize * 2)) {
      ^();
    };

    if ((modifiers == modifierKey) && (this.pointInLeftWidget(initialPosition, renderBounds))) {
      ^(
        mouseMoveAction: { arg ev; ev.selected.collect(_.resizeLeft(ev))}
      )
    };

    if ((modifiers == modifierKey) && (this.pointInRightWidget(initialPosition, renderBounds))) {
      ^(
        mouseMoveAction: { arg ev; ev.selected.collect(_.resizeRight(ev))}
      )
    };


    ^()
  }
  pointInRightWidget { arg aPoint, aRect;
    ^Rect(
      aRect.right - widgetSize,
      aRect.top,
      widgetSize,
      aRect.height,
    ).contains(aPoint)
  }
  
  pointInLeftWidget { arg aPoint, aRect;
    ^Rect(
      aRect.left,
      aRect.top,
      widgetSize,
      aRect.height,
    ).contains(aPoint)
  }
  
  dragProps { arg aMouseAction
  /*
  * (
  *  modifiers: Number, 
  *  initialPosition: Point,
  *  mouseDelta: Point,
  *  position: Point,
  *  mouseMoveAction: Function,
  *  mouseUpAction: Function
  * )
  */
    ;
    var renderBounds = this.renderBounds(item, canvasProps.origin, canvasProps.zoom);
		var delta = aMouseAction.mouseDelta;
		
		var newBounds = Rect(
			renderBounds.left + delta.x,
			renderBounds.top + delta.y,
			renderBounds.width,
			renderBounds.height,
		)
      .snapToRow(canvasProps)
      .snapToBeat(canvasProps);
    ^(
      renderBounds: newBounds,
    );
  }

	onDrag { arg aMouseAction;
    var dragProps = this.dragProps(aMouseAction);
    props.putAll(dragProps);
		canvasProps.redraw();
	}

  resizeRight { arg aMouseAction;
    var dragProps;
    var delta = aMouseAction.mouseDelta;
    var initialBounds = props.initialBounds;
  
    dragProps = (
      renderBounds: Rect(
        initialBounds.left,
        initialBounds.top,
        max(initialBounds.width + (delta.x), 10),
        initialBounds.height
      ) 
    );
    props.putAll(dragProps);
    canvasProps.redraw();
  }

  resizeLeft { arg aMouseAction;
    var dragProps;
    var delta = aMouseAction.mouseDelta;
    var newBounds = props.renderBounds;
    var initialBounds = props.initialBounds;
  
    dragProps = (
      renderBounds: Rect(
        initialBounds.left + (delta.x),
        initialBounds.top,
        max(initialBounds.width - (delta.x), 10),
        initialBounds.height
      ) 
    ); 
    props.putAll(dragProps);
    canvasProps.redraw();
  }

  copyTo { arg position, store, link = false;
    var newProps = props.copy;
    var bounds = newProps.renderBounds;
    var rxObject;
    var itemParams;

    newProps.putAll(
      (
        renderBounds: Rect(
          position.x,
          position.y,
          bounds.width,
          bounds.height
        )
        .snapToRow(canvasProps)
        .snapToBeat(canvasProps)
      )
    );
    itemParams = this.getItemParams(newProps);
    store.add(itemParams);
  }

	onDragEnd { arg aMouseAction;
    item.putAll(this.getItemParams(props));
	}

  getItemEditView {
    var view = EnvirGui(item)
      .putSpec(\row, ControlSpec(0, 128, \lin, 1, 0));
		view.viewForParam('id').visible_(false);
		view.parent.name = item.id;
		^view;
  }

  deleteFromStore { arg store;
    store.deleteItem(id);
  }

  getContextMenuActions {
    var actions = [
      MenuAction("edit", { this.getItemEditView }),
    ];
    if (item.src.notNil) {
      actions = actions.add(
        MenuAction("edit source", { item.getModule.open }),
      );
    };
    if (item.auxiliaryActions.notNil) {
      actions = actions ++ [MenuAction.separator] ++ item.auxiliaryActions
    };
    ^actions;
  }
}


