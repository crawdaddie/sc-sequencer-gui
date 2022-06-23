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

~clapsf = Import("./Clap.wav", loader: 'soundfile');
~seq.addObject((beats: 1, a: 1, row: 0, sustain: 1));
~seq.addObject((beats: 3, a: 1, row: 0, sustain: 1, soundfile: ~clapsf, startPos: 0));
)

~clapsf.soundfile.readData()
~clapsf.getData

h = Dictionary()

h.put("./Clap.wav:1,1", ())

h.at("./Clap.wav:1.1")

h.at("./Clap.wav:1,1")

h
(
h = [1,2,3];

j = { arg ref;
  ref[0] = 5;
};
)


h

j.(h)
h


~clapsf


Soundfile
~seqGui.zoomBy(2, 0.3)

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

Quarks.install("~/projects/sc/sc-lsp")

Quarks.install("https://github.com/scztt/log.quark");
s.boot;

4.postln;
'soundfile'.import;
~soundfile
sb
Quarks.gui;

Log

{ SinOsc.ar(100)!2}.play;


~clapsf = Import("./Clap.wav", loader: 'soundfile')
~clap



Quarks.install("https://github.com/scztt/Deferred.quark")
