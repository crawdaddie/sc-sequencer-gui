RxEvent : Event {
  *new { arg ev;
    if (ev.isKindOf(RxEvent)) {
      ^ev;
    };
    ^super.new.init(ev);
  }

  init { arg ev = ();
    ev.keysValuesDo { |k, v|
      super.put(k, v);
    }
  }

  dispatch { arg type, payload;
    Dispatcher(type, payload, this);
  }

  broadcastUpdate { arg key, originalValue, value;
  // TODO: refactor this to broadcast messages to a central exchange
  // to stop UIs needing to subscribe to this instance and then resubscribe when an instance changes
  // instead they can subscribe just to updates for this instance's _ID_ , which will remain stable throughout
  // rather than itself
  // will simplify subscribers needing to remove their listeners for GC
    [key, originalValue, value].postln;

    if (originalValue.isNil && value.notNil) {
      ^this.dispatch(\addObject, (id: this.id, object: this));
    };

    if (originalValue.notNil && value.isNil) {
      ^this.dispatch(\removeObject, (id: this.id) ++ (removedKey: key) );
    };

    ^this.dispatch(\updateObject, (id: this.id, object: this));
  }

	put { arg key, value, dispatch = true;
    var originalValue = this.at(key);
    super.put(key, value);
    this.postln;
    if (dispatch && (originalValue != value)) { this.broadcastUpdate(key, originalValue, value) };
		^this;
	}

	putAll { arg dictionary, dispatch = true;
		var updates = ();

		dictionary.keysValuesDo { arg key, value;
			if (this.at(key) != value) {
				updates.put(key, value);
			};
			
			super.put(key, value);
		};

    if (dispatch) {
      this.dispatch(\updateObject, (id: this.id, object: this));
    };
	}

	id {
		^this['id']
	}
  
  copyAsEvent {
    var newEvent = ().putAll(this);
    newEvent.id = nil; 
    newEvent.parent_(this.parent);
    ^newEvent;
  }

  // play { arg storeCtx = (), clock;
  //   var playEvent = this.copy;
  //   var mod = storeCtx.modulePath !? { arg p; Mod(p) };
  //   playEvent.use {
  //     ~modCtx = mod;
  //     ~clock = clock; 
  //     currentEnvironment[\play].value();
  //   }
  // }
}


