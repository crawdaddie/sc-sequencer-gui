Waveform {
  classvar <all;
  var array;
  var data;
  *initClass {
    all = Dictionary();
  }
  *new { arg 
      data, /* -> pointer to raw soundfile data */
      soundfile, /* -> Soundfile */
      zoom = 1,
      cb 
    ;
    var id = format("%:%", soundfile.path, zoom);
    var wf = all.at(id);
    if (wf.notNil, {
      ^wf;
    }, {
      ^super.new.init(data, soundfile, zoom, cb)
    });
  }

  init { arg rawdata, soundfile, zoom, cb;
    var id = format("%:%", soundfile.path, zoom);
    var horizontalUnit = Theme.horizontalUnit;
    var bufRateScale = soundfile.sampleRate / Server.default.sampleRate;
    var index = bufRateScale * zoom;
    var duration = soundfile.duration;
    var chunks = (duration * horizontalUnit * index).asInteger;
    var wfTask;
    data = rawdata;
    array = Array.fill(chunks, [0,0]);
    wfTask = this.getWaveformComputation(array, rawdata, cb);
    wfTask.start(AppClock);
    all.put(id, this);
  }

  getWaveformComputation { arg array, rawArray, cb;
    var chunks = array.size;
    var chunkSize = (rawArray.size / chunks).asInteger;
    ^Task({
      chunks.do { | index |
        var maxVal, minVal;
        var rowData;
        var startFrame = index * chunkSize;
        rowData = rawArray[startFrame .. (startFrame + chunkSize - 1)];
        minVal = maxVal = rowData[0];
        rowData.do { |data, index|
          maxVal = max(maxVal, data);
          minVal = min(minVal, data);
        };
			  array[index] = [maxVal, minVal];
      };
      cb.value();
    })
  }

  array { arg start, end;
    ^array;
  }
}
