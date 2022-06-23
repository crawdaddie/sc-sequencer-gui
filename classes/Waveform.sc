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
      zoom = 1 
    ;
    var id = format("%:%", soundfile.path, zoom);
    var wf = all.at(id);
    if (wf.notNil, {
      ^wf;
    }, {
      ^super.new.init(data, soundfile, zoom)
    });
  }

  init { arg rawdata, soundfile, zoom;
    var id = format("%:%", soundfile.path, zoom);
    var horizontalUnit = Theme.horizontalUnit;
    var bufRateScale = soundfile.sampleRate / Server.default.sampleRate;
    var index = bufRateScale * zoom;
    var duration = soundfile.duration;
    var chunks = (duration * horizontalUnit * index).asInteger;
    var wfTask;
    data = rawdata;
    array = Array.fill(chunks, [0,0]);
    wfTask = this.computeWaveform(array, rawdata);
    wfTask.start(AppClock);

    all.put(id, this);
  }

  computeWaveform { arg array, rawArray;
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
      }
    })
  }

  array { arg start, end;
    ^array;
  }
}
