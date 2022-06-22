(

~getStore = {
  var id = 1000;
  var objProto = ().make({
    ~update = { arg obj, objectUpdate;
      obj.putAll(objectUpdate);
      obj.changed(nil, \updateObject, obj)
    };
  });

  var proto = ().make({
    ~addObject = { arg store, obj;
      store[id] = obj
        .putAll((id: id))
        .proto_(objProto);
      store.changed(nil, \addObject, obj);
      id = id + 1;
      obj;
    };

    ~removeObject = { arg store, id;
      var obj = store[id] = nil;
      obj.removeDependants;
      store.changed(nil, \removeObject, id);
    };
  });
  var store = ().proto_(proto);
  store;
};



~seq = ~getStore.();
~seqGui = SequencerCanvas(~seq);
~seq.addObject((beats: 1, a: 1, row: 0, sustain: 1))
)

~seqGui.zoomBy(1.1, 1)

~seq

~seq[1000].update((beats: 0, sustain: 1));

~seq[1000].changed;

~seq.updateObject(1001, (a: 2))
~seq

~addObject.(~seq, (beats: 1));

~seq = (1001: ());
~seq.parent_((timingContext: (bpm: 60)))
~seq.values
~seq
~seq.dependants

~seq.hasMethod('addObject')

~seq.respondsTo(1000)

~seq.respondsTo('addObject')

~seq.addObject = { arg seq, obj;
  obj.postln;
};

~seq.get('addObject')
~seq.addObject((a: 1))
