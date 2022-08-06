(
var scale = [0,2,3,5,7,8,10];
'./plucks'.import;
'./bass'.import;
'injectData'.importFrom('userDataHooks');
'proxyManager'.import;

'./reverb'.import.setSends(
  [~plucks.out,  'plucks', 0.1],
  [~bass.out,  'plucks', 0.1],
);


~injectData.('seqData', {
  { |i|
    (
    time: i,
    sustain: rrand(0.5, 1.0),
    row: 11 - scale.choose,
    )
  }!12
});
~seqData.postln;
~seq = Seq.from(~seqData); 

~edit = M {
  ~seqGui = SequencerCanvas(~seq, pianoRoll: true);
};
// ~edit.(currentEnvironment);

~seqRoutine = { arg cb;
  Routine {
    inf.do {
      var time = 0;
      ~seq.order.do { arg item;
        cb.value(item.events);
        time = item.time;
        item.delta !? _.wait;
      };
      (12 - time).wait;
    }
  };
};
)


~seqData



(
var clock = TempoClock(180/60);
~proxyManager.p.do(_.play);
~seqRoutine.({|events|
  ~plucks.playEvents(events);
}).play(clock, quant: 1);
~edit.(currentEnvironment);
~bass.routine.play(clock, quant: 1);
)



~seq



~seq.values.collect({ |item| item['id'] = nil; item }).asCompileString

~edit.(currentEnvironment)
~bass.editSeq
~bass.out.edit


prUnarchive()

"e[0-9].".matchRegexp('e100012312')

~seq.select({ |k, v| "e[0-9].".matchRegexp(k.asString) })

~seq.time = 1 
~seq.time
h = "/Users/adam/projects/sc/sc-sequencer-gui/examples/.test.data.scd".load;
h


(
{
  Seq.from([(a: 1)]);
}.asMinifiedTextArchive;
)

o=[]
