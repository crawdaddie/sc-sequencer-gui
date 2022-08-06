var proxyManager = 'proxyManager'.asModule;

SynthDef(\pluck, {
	var snd, freq;
	freq = \freq.kr(440);
	snd = SinOsc.ar(freq * IRand(1, 4)) * Env.perc(0.01, \fmsus.kr(0.2)).ar * \fmamt.kr(100);
	snd = SinOsc.ar(freq + snd);
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = snd * Env.perc(0.01, \sustain.kr(1)).ar(Done.freeSelf);
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;



proxyManager.initProxies({
  {
    ~pluck = NodeProxy(rate: \audio, numChannels: 2);
    ~pluck.quant = 0;
    ~pluck;
  }; 
});

~pluck = proxyManager.p['pluck'];
~playEvents = M { arg events;
  var evs = events.asArray;
  var notes = evs.collect({|e|
    60 - e.row + (12 * rand(3));  
  });
  var sus = evs.collect(_.sustain);
  ~pluck.source = (
    instrument: 'pluck',
    midinote: notes,
    sustain: sus,
    fmsus: rrand(0.2, 5),
    fmamt: rrand(100, 200)
  );
};


