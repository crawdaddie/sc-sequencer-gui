Seq : SortedList {
  classvar lastId = 1000;
  var <timeSeq;
  var <timeIndices;

  var <>time;
  var <>sustain;

  *new {
    // "new seq".postln;
    ^super.new(8, { |a, b| a.time < b.time }).init()
  }
  
  init {
    // "new seq init".postln;
    // timeSeq = SortedList(8, { arg a, b;
    //   a.time < b.time
    // });
    // timeIndices = Dictionary();
    // timeSeq = SortedList(8, { |a, b| a[0] < b[0]});
    // Dictionary();

    Dispatcher.addListener(
      'updateObject',
      this,
      { |p|
        if (p.originalValues[\time].notNil && this.keys.includes(p.id)) {
          this.updateTimeSeq;
        };
      }
    );
  }


  *getId {
    lastId = lastId + 1;
    ^format("e%", lastId).asSymbol
  }

  *isId { arg id;
    ^"e[0-9].".matchRegexp(id.asString);
  }

  *from { arg events;
    var seq = super.new.init();
    events.do { arg event; seq.add(event, updateTimeSeq: false) };
    this.updateTimeSeq;
    ^seq;
  }

  add { arg obj, updateTimeSeq = true;
    var id, seqObj, timelineBucket;
    id = Seq.getId;
    obj.id = id;

    seqObj = RxEvent(obj);
    this.put(id, seqObj);
    if (updateTimeSeq) {
      this.updateTimeSeq
    };
  }

  updateTimeSeq {
    var timeline = SortedList(this.size, { |a, b| a.time < b.time });
    var eventsByTime = Dictionary();
    this.keysValuesDo { |key, val|
      if (Seq.isId(key)) {
        var time = val.time;
        var evs = eventsByTime[time] ?? Set();
        eventsByTime[time] = evs.add(key);
      }
    };

    eventsByTime.keysValuesDo { | time, events |
      timeline.add((time: time, events: events))
    };
    timeSeq = timeline;
  }

  order { arg end;
    var times, deltas;

    times = timeSeq.asArray;
    times = times.collect({ arg t, i;
      var eventKeys = t.events;
      var events = eventKeys.collect({ |k| this[k] }); 
      var time = t.time;
      var delta = if (times[i + 1].notNil, {times[i + 1].time - time}, nil);
      (time: time, delta: delta, events: events)
    });

    if (times[0].time != 0) {
      var d = times[0].time;
      times = [(time: 0, delta: d)] ++ times;
    };
    ^times;
  }

  push {
    this.use {
      ~items = {
        this.values;
      }
    };
    super.push;
  }
}
