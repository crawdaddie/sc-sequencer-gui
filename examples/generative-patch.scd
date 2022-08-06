(
SynthDef(\kick, {
	var snd;
	snd = SinOsc.ar(50 * (1 + (8 * Env.perc(0.001, 0.05).ar) + (2 * Env.perc(0.001, 0.3).ar)));
	snd = snd + (SinOsc.ar(200 * (1 + (3 * Env.perc(0.001, 0.05).ar))) * Env.perc(0.001, 0.05).ar);
	snd = snd + (Hasher.ar(Sweep.ar) * Env.perc(0.001, 0.01).ar);
	snd = snd.tanh;
	snd = snd * Env.perc(0.001, 1.0).ar(Done.freeSelf);
	snd = snd ! 2;
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\snare, {
	var snd, freq;
	freq = 210 * (1 + (Env.perc(0.001, 0.03).ar * 2));
	snd = SinOsc.ar(freq + (200 * SinOsc.ar(freq * 1.6))) * Env.perc(0.001, 0.2).ar;
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 2130, 0.3) * Env.perc(0.1, 0.3).ar);
	snd = snd.tanh;
	snd = snd * Env.perc(0.001, 0.3).ar(Done.freeSelf);
	snd = snd ! 2;
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\clap, {
	var snd, freq;
	snd = BPF.ar(Hasher.ar(Sweep.ar), [1320, 1100, 1420] * 0.9, 0.1) * 20.dbamp;
	snd = snd * Env.perc(0.001, 0.3).delay([0, 0.01, 0.02]).ar;
	snd = Splay.ar(snd, 0.1);
	snd = snd * Env.perc(0, \decay.kr(0.3)).ar(Done.freeSelf);
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\hihat, {
	var snd, freq;
	snd = BPF.ar(Hasher.ar(Sweep.ar), 8130, 0.3);
	snd = snd ! 2;
	snd = snd * Env.perc(0.001, 0.05).ar(Done.freeSelf);
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\glitch, {
	var snd, freq, n, dry;
	n = (1..16);
	snd = (SinOsc.ar(LFNoise2.kr(3).linexp(-1, 1, 100, 8000) * n * (1 + (n * TRand.ar(-1, 1, Dust.ar(16)) * 1))) / n).sum;
	snd = snd * Env.linen(0.001, 0.06, 0.001).ar;
	dry = snd;
	snd = snd + LocalIn.ar(1);
	snd = HPF.ar(snd, 300);
	snd = PitchShift.ar(snd, 0.5, 2);
	snd = LPF.ar(snd, 3000);
	LocalOut.ar(snd);
	snd = snd + dry;
	snd = snd * Env.linen(0.001, \dur.kr(1), 0.001).ar(Done.freeSelf);
	snd = Pan2.ar(snd, LFNoise2.kr(0.3));
	snd = snd * \amp.kr(0.3);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\bass, {
	var snd;
  var freq = \freq.kr(440);
  // freq = freq * Lag.ar(
  //     2 ** TIRand.ar(-1, 2, Dust.ar(4)),
  //     0.2
  //   );
  freq = freq * ({ LFNoise2.kr(1) } ! 3 * 0.2).midiratio ;

	snd = Saw.ar(freq);
  // snd = LFPulse.ar(freq);

	// https://nathan.ho.name/posts/moisture-bass/
	snd = BHiShelf.ar(snd, 3000, 1.0, 10.0);
	snd = BPF.ar(snd, (0..31).linexp(0, 31, 100, 8000), LFNoise1.kr(1, mul: 0.3, add: 0.3 + 0.1)).sum;
	snd = (snd * 4).tanh;
	snd = MoogFF.ar(snd, LFNoise2.kr(3).linexp(-1, 1, 100, 8000), 0);
	snd = Splay.ar(snd);
	snd = snd * Env.asr(0.01, 1, 0.01).ar(Done.freeSelf, \gate.kr(1));
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\pluck, {
	var snd, freq;
	freq = \freq.kr(440);
	snd = SinOsc.ar(freq * IRand(1, 4)) * Env.perc(0.01, 0.3).ar * 100;
	snd = SinOsc.ar(freq + snd);
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = snd * Env.perc(0.01, Rand(0.1, 1)).ar(Done.freeSelf);
	snd = snd * \amp.kr(1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\pad, {
	var snd, freq;
	freq = \freq.kr(440) * (SinOsc.ar(6) * Line.ar(0, 1, 3) * 0.1).midiratio;
	freq = freq * ({ LFNoise2.kr(3) } ! 4 * 0.1).midiratio * [1, 2, 2, 1];
	snd = SinOsc.ar(freq * IRand(1, 4)) * Env.perc(0.01, 0.3).ar * Rand(0, 3000);
	snd = SinOsc.ar(freq + snd);
	snd = Splay.ar(snd);
	snd = snd * Env.perc(Rand(3, 8), Rand(3, 8)).ar(Done.freeSelf);
	snd = snd * \amp.kr(0.1);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\send, {
	var snd;
	snd = In.ar(\in.kr(0), 2);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\fx, {
	var snd;
	snd = In.ar(\out.kr(0), 2);
	snd = LeakDC.ar(snd);
	snd = Limiter.ar(snd);
	ReplaceOut.ar(\out.kr(0), snd);
}).add;

SynthDef(\reverb, {
	var snd;
	snd = In.ar(\out.kr(0), 2);
	snd = snd + (NHHall.ar(snd) * -5.dbamp);
	ReplaceOut.ar(\out.kr(0), snd);
}).add;

SynthDef(\drumFx, {
	var snd;
	snd = In.ar(\out.kr(0), 2);
	snd = SelectX.ar(ToggleFF.ar(Dust.ar(4)).lag(0.1), [snd, snd + PitchShift.ar(snd, 0.1, 1.5)]);
	snd = SelectX.ar(ToggleFF.ar(Dust.ar(4)).lag(0.1), [snd, snd + PitchShift.ar(snd, 0.15, 1.3)]);
	snd = SelectX.ar(Trig.ar(Dust.ar(0.3), 0.3).lag(0.1), [snd, Latch.ar(snd, Impulse.ar(TRand.ar(-1, 1, Dust.ar(8)).linexp(-1, 1, 8000, 16e3)))]);
	snd = (snd * 1).tanh;
	snd = BPF.ar([snd], (0..31).linexp(0, 31, 100, 8000), 0.1).sum;
	snd = snd + NHHall.ar(snd * Trig.ar(Dust.ar(0.3), 0.1).lag(0.03), 3);
	ReplaceOut.ar(\out.kr(0), snd);
}).add;
)

(
Routine {
	var s;
	var bpm, beat;
	var bassNote, pluckChord;
	var reverbGroup, reverbBus;
	var drumGroup, drumBus;
	s = Server.default;
	bpm = 210;
	beat = 60 / bpm;

	reverbGroup = Group();
	reverbBus = Bus.audio(s, 2);

	Synth.after(reverbGroup, \send, [in: reverbBus, out: 0]);
	Synth.tail(nil, \fx);
	Synth.tail(reverbGroup, \reverb, [out: reverbBus]);

	drumGroup = Group();

	Synth.after(drumGroup, \send, [in: drumBus, out: 0]);
	Synth.tail(drumGroup, \drumFx, [out: drumBus]);

	Routine {
		loop {
			bassNote = 37;
			pluckChord = 61 + [-8, -5, 0, 2, 4, 7, 9, 12, 14, 19];
			(beat * 30).wait;

			bassNote = 34;
			pluckChord = 58 + [0, 3, 7, 12, 14, 15, 22, 24];
			(beat * 30).wait;

			bassNote = 30;
			pluckChord = 54 + [0, 4, 7, 11, 12, 16, 23, 24];
			(beat * 30).wait;

			bassNote = 32;
			pluckChord = 54 + [0, 4, 7, 11, 12, 16, 23, 24];
			(beat * 15).wait;

			bassNote = 39;
			pluckChord = 54 + [0, 4, 7, 11, 12, 16, 23, 24];
			(beat * 5).wait;

			bassNote = 27;
			pluckChord = 54 + [0, 4, 7, 11, 12, 16, 23, 24];
			(beat * 10).wait;
		};
	} .play;

	Routine {
		var bassSynth;
		s.makeBundle(s.latency, {
			bassSynth = Synth(\bass, [freq: bassNote.midicps, out: reverbBus], reverbGroup);
		});
		loop {
			bassSynth.set(\freq, bassNote.midicps);
			beat.wait;
		};
	} .play;
	
	// Routine {
	// 	var last = nil;
	// 	loop {
	// 		var note;
	// 		note = pluckChord.choose;
	// 		while { note == last } { note = pluckChord.choose; };
	// 		last = note;
	// 		s.makeBundle(s.latency, {
	// 			Synth(\pluck, [freq: note.midicps, pan: rrand(-1.0, 1.0), out: reverbBus], reverbGroup);
	// 		});
	// 		beat.wait;
	// 	};
	// } .play;

	Routine {
		var last = nil;
		loop {
			var note;
			note = pluckChord.choose;
			while { note == last } { note = pluckChord.choose; };
			last = note;
			s.makeBundle(s.latency, {
				Synth(\pad, [freq: note.midicps, pan: rrand(-1.0, 1.0), out: reverbBus], reverbGroup);
			});
			beat.wait;
		};
	} .play;
	//
	// Routine {
	// 	loop {
	// 		[0, 4, 6, 20, 35, 52, 59, 77, 78, 86, 89, 99, 122, 127][1..].differentiate.do { |duration|
	// 			s.makeBundle(s.latency, {
	// 				Synth(\kick, [out: drumBus], drumGroup);
	// 			});
	// 			(beat * 0.5 * duration).wait;
	// 		};
	// 	};
	// }.play;

	// Routine {
	// 	loop {
	// 		[0, 1, 11, 16, 19, 23, 25][1..].differentiate.do { |duration|
	// 			s.makeBundle(s.latency, {
	// 				Synth(\snare, [out: drumBus], drumGroup);
	// 			});
	// 			(beat * 0.5 * duration).wait;
	// 		};
	// 	};
	// }.play;

	// Routine {
	// 	loop {
	// 		[0, 1, 4, 9, 15, 22, 32, 34][1..].differentiate.do { |duration|
	// 			if (0.2.coin) {
	// 				10.do {
	// 					s.makeBundle(s.latency, {
	// 						Synth(\hihat, [out: drumBus], drumGroup);
	// 					});
	// 					(beat * 0.5 / 10).wait;
	// 				};
	// 				(beat * 0.5 * (duration - 1)).wait;
	// 			} {
	// 				s.makeBundle(s.latency, {
	// 					Synth(\hihat, [out: drumBus], drumGroup);
	// 				});
	// 				(beat * 0.5 * duration).wait;
	// 			};
	// 		};
	// 	};
	// }.play;
	// Routine {
	// 	loop {
	// 		[0, 1, 5, 12, 25, 27, 35, 41, 44][1..].differentiate.do { |duration|
	// 			if (0.2.coin) {
	// 				10.do {
	// 					s.makeBundle(s.latency, {
	// 						Synth(\clap, [out: drumBus, decay: 0.01], drumGroup);
	// 					});
	// 					(beat * 0.5 / 10).wait;
	// 				};
	// 				(beat * 0.5 * (duration - 1)).wait;
	// 			} {
	// 				s.makeBundle(s.latency, {
	// 					Synth(\clap, [out: drumBus], drumGroup);
	// 				});
	// 				(beat * 0.5 * duration).wait;
	// 			};
	// 		};
	// 	};
	// } .play;
	// Routine {
	// 	loop {
	// 		[0, 2, 6, 24, 29, 40, 43, 55, 68, 75, 76, 85][1..].differentiate.do { |duration|
	// 			s.makeBundle(s.latency, {
	// 				Synth(\glitch, [out: drumBus, dur: beat * 0.5 * duration], drumGroup);
	// 			});
	// 			(beat * 0.5 * duration).wait;
	// 		};
	// 	};
	// }.play;
} .play;
)


s.makeGui;
