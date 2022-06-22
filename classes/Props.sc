Props : Event {
	var <>onUpdate;
	*new { arg anEvent = ();
		^super.new.init(anEvent);
	}

	init { arg anEvent;
		super.putAll(anEvent);
    know = true;
	}

	put { arg key, value;
		super.put(key, value);
		this.changed(key, value);
	}
  update { arg ... args;
    onUpdate.value(*args);
  }
}
