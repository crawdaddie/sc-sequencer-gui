SequencerCanvas {
	var <canvas;
	var props; 

	/**
	 * (
	 *   quantX: 100, // number in px
	 *	 origin: 0@0, // point - represents where the 'current' viewport is
	 *	 timingOffset: 0, // at which 'global' offset is the represented collection of events
	 *	 zoom: 1@1, // point
	 * );
	 */

	// child canvas objects
	var grid;
	var views;

  var store;

	*new { arg store, timingContext = (bpm: 60), parent;
		^super.new.init(store, timingContext, parent);
	}

  addObject { arg payload;
    "add object canvas".postln;
    payload.postln;
    // var viewClass = this.getItemEmbedView(item);
    // var view = viewClass.new(item, props);
    // views = views.add(view);
    // canvas.refresh;
  }

  updateObject { arg payload;
    "update object canvas".postln;
    payload.postln;
    // var id, obj, v;
    // #id, obj = payload;
    // v = block {|break|
    //   views.do { arg v;
    //     if (v.id == id) {
    //       break.value(v)
    //     };
    //   }
    // };
    // v.setItem(obj);
    // canvas.refresh;
  }

  removeObject {
  }

  listen { arg type;
    Dispatcher.addListener(
      type, 
      this,
      { |p|
        if (p.id == store.id) {
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

  front {
    canvas.front;
  }

	init { arg aStore, timingContext, aParent;
		var parent, bounds;
		var title = format("sequencer - %", aStore.id);
    store = aStore;

		if (store.id == 1000) {
			title = title ++ " (top level)"
		};

		parent = aParent ?? Window.new(title, Rect(740, 455, 700, 400)).front;
		bounds = parent.view.bounds;
		canvas = UserView(parent, bounds);
		canvas.resize = 5;
		parent.acceptsMouseOver_(true);

		props = Props((
			quantX: 100,
      quantSubdivisions: 2,
			origin: 0@0,
			timingOffset: 0,
      bps: timingContext.bpm / 60,
			zoom: 1@1,
			redraw: { canvas.refresh; },
			canvasBounds: canvas.parent.bounds,
		));

		canvas.onResize = { arg c;
			props.canvasBounds = c.parent.bounds;
		};
		
		this.addChildViews(store.values.select(_.time.notNil));

		this.connectKeyActions;
		this.connectMouseActions;
    this.addListeners(store);

		canvas.onClose = { arg view;
			views.do(_.onClose);
      this.release;
			grid.onClose;
      store.removeDependant(this);
		};

		canvas.drawFunc = {
			grid.render(props);
			views.do(_.render(props));
		};

    canvas.canReceiveDragHandler_({ arg view; true });
    canvas.receiveDragHandler_({ arg view, x, y;
      var params = this.getNewItemParams(x, y);
      var item = View.currentDrag;
      var newItem = this.getDragItemEvent(View.currentDrag, params);
      store.addSequenceableItem(newItem);
    });


	}
  
  getDragItemEvent { arg currentDrag, params;
    if (currentDrag.class == String) {
      var mod = Mod(currentDrag, 'soundfile');
      if (mod.soundfile.notNil, {(
        params.putAll(
          (soundfile: currentDrag)
        )
      )})
    };

    if (currentDrag.soundfile.notNil, {
      ^Mod(currentDrag.soundfile)
        .getSFEvent
        .create(
          params.putAll((soundfile: currentDrag.soundfile))
        )
    }, { ^currentDrag });
  }

  getNewItemParams { arg x, y;
    var point = Point(x, y);
    var origin = props.origin;
		var zoom = props.zoom;
		var xFactor = Theme.horizontalUnit;
		var yFactor = Theme.verticalUnit;
    var itemParams;
		point = (point + Point(-1 * origin.x, -1 * origin.y)) * Point(zoom.x.reciprocal, zoom.y.reciprocal);

		itemParams = (
			time: (point.x / xFactor).round(1),
			row: (point.y / yFactor).round(1),
			//sustain: bounds.width / xFactor
		);
    ^itemParams;
  }

  getItemEmbedView { arg item;
    if (item.type == 'soundfile' || item.soundfile.notNil || item.soundfileRef.notNil) {
      ^SoundfileCanvasObject;
    };
    if (item.class == Store) {
      ^StoreCanvasObject;
    };
    ^SequenceableCanvasObject;
  }


  getContextMenuActions { arg mouseAction, clipboard;
    var pasteActions = if (clipboard.size != 0, {
      [
        MenuAction.separator,
        MenuAction("paste", {
          clipboard.do { arg view;
            view.copyTo(mouseAction.initialCanvasPosition, store);
          }
        }),
        MenuAction("paste linked", {
          clipboard.do { arg view;
            // view.copyTo(mouseAction.initialCanvasPosition, store, link: true);
          }
        })
      ]
    }, {[]});
    ^[
      Menu(
        MenuAction("sub"),
        MenuAction("menu"),
      ).title_("add item")
    ] ++ pasteActions;
  }

	connectKeyActions {
    // var keyActionManager = CanvasKeyActionManager(this, ProjectKeyActionManager());
	}

	connectMouseActions {
		var mouseAction;
    var clipboard;
		
		canvas.mouseDownAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
      var initialCanvasPosition = Point(mouseX, mouseY); /* this is a position relative to the
      window or canvas bounds (eg canvas objects can compare it to their own renderBounds props) */
			var position = this.translateMousePosition(mouseX, mouseY); /* this is a position relative to the origin*/ 
			var notSelected, selected;
			#notSelected, selected = views.partition(_.contains(position).not);

      views = notSelected ++ selected;
      mouseAction = if (selected.size > 0, 
				{
          var baseAction;
          canvas.setContextMenuActions(
            MenuAction(
              "copy",
              { clipboard = selected }
            ),
            MenuAction(
              "delete",
              { selected.do { arg selected; selected.deleteFromStore(store) } }
            ),
            *selected.last.getContextMenuActions()
          );

          selected.do({ arg view;
            view.select;
            view.onDragStart;
          });
          baseAction = ( modifiers: modifiers,
						initialPosition: position,
            initialCanvasPosition: initialCanvasPosition,
            selected: selected,
						mouseMoveAction: { arg ev; ev.selected.collect(_.onDrag(ev)) },
						mouseUpAction: { arg ev;
							ev.selected.do(_.unselect);
							ev.selected.collect(_.onDragEnd(ev))
						},
					);
          selected[0] !? baseAction.putAll(selected[0].getMouseAction(baseAction)) ?? baseAction;
				}, {
          var baseAction = (
            initialPosition: position,
            initialCanvasPosition: initialCanvasPosition,
            //mouseMoveAction: { arg ev; selectionRectangle.onDrag(ev)},
            //mouseUpAction: { arg ev;
            //  selectionRectangle.onDragEnd(ev)
            //}
          );
          canvas.setContextMenuActions(
            *this.getContextMenuActions(baseAction, clipboard)
          );
          baseAction;
        });
		};

		canvas.mouseMoveAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			mouseAction.position = position;
    
      mouseAction.mouseDelta = position - mouseAction.initialPosition;
			mouseAction !? { mouseAction.mouseMoveAction }
		};

		canvas.mouseUpAction = { arg view, mouseX, mouseY, modifiers, buttonNumber, clickCount;
			var position = this.translateMousePosition(mouseX, mouseY);
			mouseAction !? { mouseAction.mouseUpAction };
		};
	}

	translateMousePosition { arg mouseX, mouseY;
    var translatedMouse = Point(mouseX, mouseY) - props.origin;
		^translatedMouse;
	}

	addChildViews { arg items;
		grid = SequencerGrid();
		views = items.collect({ arg item;
			var class = this.getItemEmbedView(item);
			class.new(item, props);
		});
	}

	zoomBy { arg x = 1, y = 1;
		var zoomX = props.zoom.x;
		var zoomY = props.zoom.y;
		props.zoom = (zoomX * x)@(zoomY * y);
		canvas.refresh;	
	}

	moveOrigin { arg x, y;
		props.origin = (props.origin.x + x)@(props.origin.y + y);
		canvas.refresh;
	}
  incrementQuantSubdivision { arg increment;
    props.quantSubdivisions = max(1, props.quantSubdivisions + increment);
    canvas.refresh;
  }
  play {
    store.play;
  }
  close {
    canvas.parent.close;
  }
}
