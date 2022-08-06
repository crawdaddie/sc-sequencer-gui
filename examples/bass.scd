var proxyManager = 'proxyManager'.asModule;
var scale = [0,2,3,5,7,8,10];

'injectData'.importFrom('userDataHooks');

SynthDef(\square_bass, {
	var snd;
  var freq = \freq.kr(440);
  // freq = freq * Lag.ar(
  //     2 ** TIRand.ar(-1, 2, Dust.ar(4)),
  //     0.2
  //   );
  freq = freq * ({ LFNoise2.kr(1) } ! 4 * 0.3).midiratio ;

	// snd = Saw.ar(freq);
  snd = LFPulse.ar(freq);

	// https://nathan.ho.name/posts/moisture-bass/
	snd = BHiShelf.ar(snd, 3000, 1.0, 10.0);
	snd = BPF.ar(snd, (0..31).linexp(0, 31, 100, 8000), 0.3).sum;
	snd = (snd * \dist.kr(4)).tanh;
	snd = MoogFF.ar(snd, LFNoise2.kr(1).linexp(-1, 1, 100, 8000), 0);
	// snd = MoogFF.ar(snd, SinOsc.kr(1).linexp(-1, 1, 100, 8000), 0);

	snd = Splay.ar(snd);
	snd = snd * Env.asr(0.01, \sustain.kr(1), 0.01).ar(Done.freeSelf, \gate.kr(1));
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

proxyManager.initProxies({
  {
    ~bass = NodeProxy(rate: \audio, numChannels: 2);
    ~bass.quant = 0;
    ~bass;
  }; 
});

~bass = proxyManager.p['bass'];

~playEvents = M { arg events;
  var evs = events.asArray;
  var notes = evs.collect({|e| 48 - e.row });
  var sus = evs.collect(_.sustain);

  ~bass.source = (
    instrument: 'square_bass',
    midinote: notes,
    sustain: sus,
  );
};


~injectData.('seq', {
  Seq.from(
  { |i|
    (
    time: i,
    sustain: rrand(0.5, 1.0),
    row: 12 - scale.choose,
    )
  }!4
)});

~editSeq = M {
  ~seqGui = SequencerCanvas(~seq, pianoRoll: true);
};

~routine = M {
  var r = Routine {
    inf.do {
      var time = 0;
      mod.seq.order.do { arg item;
        mod.playEvents(item.events);
        time = item.time;
        item.delta !? _.wait;
      };
      (12 - time).wait;
    }
  };
  r;
}
