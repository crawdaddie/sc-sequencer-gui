(
var scale = [0,2,3,5,7,8,10];
'./plucks'.import;
// './bass'.import;
'injectData'.importFrom('userDataHooks');
'proxyManager'.import;

'./reverb'.import.setSends(
  [~plucks.out,  'plucks', 0.1],
  // [~bass.out,  'plucks', 0.1],
);


~seqData = ~injectData.('seqData', {

  { |i|
    (
    time: i,
    sustain: rrand(0.5, 1.0),
    row: 11 - scale.choose,
    )
  }!12
});

~seq = Seq.from(~seqData); 

~seqGui = SequencerCanvas(~seq, pianoRoll: true);
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

