//! ---- HOWTO: piano roll
//! very straightforward "procedural" style.

(
// number of notes and steps
var notes, steps;
// scale array (as ratios), fundamental in hz
var scale, fundamental;

// sequence state: note flag array, step duration
var seq_note_state, seq_step_period;
// sequencer state: is-playing flag, current position
var seq_play_flag, seq_pos;
// sequencer control functions
var seq_update, seq_advance, seq_set_pos;

// sequencer main loop (a Thread of some kind)
var seq_run_loop;
// GUI elements
var win, pos_but, val_but;
// GUI parameters
var bpx; // size of buttons

notes = 16;
steps = 16; 

// 8-tone scale of natural harmonics
scale = 16.collect({ arg x;
	var y;
	var xm8 = x%8;
	if(x==0, {
		y = 1.0;
	}, {
		y = xm8.nthPrime;
		while( { y > 2.0 }, { y = y / 2.0; });
		y = y * (2 ** ((x/8).floor));
	});
	y
}).sort;

/*
ok ok, you want 12tet instead...
scale = 16.collect({ arg x; x.midiratio });
*/

fundamental = 220;

seq_step_period = 0.2;

// initial position (advance before update)
seq_pos = -1;

// the "score": 2d  array of Booleans for note states
// outer array could be a List for extensibility,
// inner element could be an Event for more complex state, &c
seq_note_state = Array.fill(steps, { Array.fill(notes, { false  }); });

// update step function
seq_update = {
	// play the notes for the current step
	seq_note_state[seq_pos].postln;
	seq_note_state[seq_pos].do({ arg v, i;
		if(v, {
			// test synthdef compiled on the fly from a function...
			// could use e.g.:
			// Synth.new(\mydef, [\freq:fundamental*scale[i]... ], s);
			{
				SinOsc.ar(
					fundamental * scale[i],
					mul:EnvGen.ar(Env.perc, levelScale:0.125, doneAction:2)
				).dup
			}.play(Server.default);
		});
	});
	// update the GUI
	AppClock.sched(0, {
		steps.do({ arg i;
			pos_but[i].value = (i==seq_pos);
		});
		nil
	});
};


// increment the position for next update
seq_advance = {
	seq_pos = (seq_pos +1)%steps;
};

// function to immediately set position
seq_set_pos = { arg i;
	seq_pos  = i;
	seq_update.value;
};

// main update thread
seq_run_loop = Routine { inf.do {
	seq_advance.value;
	seq_update.value;
	seq_step_period.wait;
} };

//--------------------------
//--- ok, do stuff

// boot the server
s = Server.default;
s.waitForBoot({

	///... FIXME: for now, just run the thing forever
	seq_run_loop.play;
	seq_play_flag= true;

}); // waitForBoot


//---------------------------------
//-- GUI (very basic)

// size of buttons
bpx = 32;

// make window
win = Window.new("piano", Rect(0, 0, bpx * (steps+1), bpx * notes));
win.front;

// list of buttons to change state
val_but = Array.fill(steps, { arg i; Array.fill(notes,{ arg j;
	Button(win, Rect(j*bpx, i*bpx, bpx, bpx))
	.states_([
		["", Color.black, Color.black],
		["", Color.white, Color.white] ])
	.action_({arg but; seq_note_state[i][j] = (but.value > 0);
	})
}) });

// list of buttons to show/select position
pos_but = Array.fill(steps,{ arg i;
	Button(win, Rect(notes*bpx, i*bpx, bpx, bpx))
	.states_([
		["", Color.grey, Color.grey],
		["", Color.red, Color.red]
	])
	.action_({arg but; seq_set_pos.value(i); })
});


// toggle play with spacebar... select speed with numbers... &c.
win.view.keyDownAction = { arg view, char, mod, unicode, keycode, key;
    [view, char, mod, unicode, keycode, key].postln;
	///... left as exercise for reader :)
	// if(seq_play_flag, { ... }, { ... });
};

)
