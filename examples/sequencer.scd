(
Dispatcher.debug_(true);
~seq = Seq();
~clapsf = Import("./Clap.wav", loader: 'soundfile');
~seq.add((time: 1, a: 1, row: 0, sustain: 1));
~seq.add((time: 3, a: 1, row: 0, sustain: 1, soundfileRef: ~clapsf.ref, startPos: 0));
~seqGui = SequencerCanvas(~seq);
/* -> SequencerCanvas accepts as a parameter any RxEvent / subclass
  - it must broadcast updates in a similar way using this.changed:
  broadcastUpdate { arg key, originalValue, value;
    if (originalValue.isNil && value.notNil) {
      ^this.changed(nil, \addObject, value);
    };

    if (originalValue.notNil && value.isNil) {
      ^this.changed(nil, \removeObject, key);
    };

    ^this.changed(nil, \updateObject, this);
  }
*/
)

~seq.push


currentEnvironment

~items.().select(_.time.notNil)

~items.time.notNil

currentEnvironment.keys
