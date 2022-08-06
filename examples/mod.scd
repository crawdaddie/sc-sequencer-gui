'injectData'.importFrom('userDataHooks');
SynthDef("acid", {
  arg out=0, gate=1, freq=50, amp=0.2, sustain = 1;
  var env1, env2, sig;
  var freqlag = Lag.kr(freq * [1, 1.01], 'freqlagtime'.kr(0.1));
  freq = Lag.kr(freq, 0.12 * (1-Trig.kr(gate, 0.001)) * gate);
  env1 = EnvGen.ar(Env.adsr(0.01, 0.1, 0.8, 0.1), gate, 1, doneAction: Done.freeSelf);
  env2 = EnvGen.ar(Env.adsr(0.001, 0.8, 0, 8, 70, -4), gate);

  sig = Mix.new(Array.fill(3, { LFPulse.ar(freqlag * (1 + rand(0.01)) , 0.0, width: LFNoise1.kr(0.2, 0.2, 0.5), mul: 2, add: -1) }));


  sig = RLPF.ar(sig, (1 + env2 * \squelch.kr(1, 0.2)) * freqlag, \res.kr(0.3, 0.1));
  sig = sig * env1;
  sig = (\gain.kr(1, 0.1) * sig).fold(-0.5, 0.5) * 2 + sig;

  Out.ar(out, Pan2.ar(sig) * amp);
}).add; // borrowed from acid otophilia

~bass = NodeProxy.audio(s, 2).quant_(4);

~injectData.('bassParams', {
  NodeSettings(
    (
      squelch: 2,
      gain: 1,
      res: 0.2,
      vol: 0.1 
    ),
    nodeRef: 'bass'
  )
}).apply;

~bass.play;

~pattern = M { arg ... pbindParams;
  ~bass.source = Pchain(
    PmonoArtic('acid'),
    Pbind(*pbindParams)
  )
};

~edit = M {
  var ndefGui = ~bass.edit;
  ndefGui.zone.keyDownAction_(mod.keyDownHandler);
};

~loadPreset = M {

};

