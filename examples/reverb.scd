var proxyManager = 'proxyManager'.asModule;
~sends = [];

proxyManager.initProxies({
  var sends = ~sends;
  {
    ~reverbSend = NodeProxy.audio(s, 2);
    ~reverbSend[1] = \vstFilter -> { arg in;
      VSTPlugin.ar(in, 2, id: 'reverb', params: [1, 0, 2, 0.5]);
    };
    ~reverbSend
  }
});

~reverbSend = proxyManager.p['reverbSend'];
~vstCtl = VSTPluginNodeProxyController.new(~reverbSend, 1, id: 'reverb').open("TAL-Reverb-2");

~setSends = M { arg ... sends;
  ~sends = sends;
  proxyManager.p.use {
    ~reverbSend[0] = {
      Mix.ar(
        sends.collect({ |proxynamedefault|
          var proxy, name, default;
          #proxy, name, default = proxynamedefault;
          proxy * (name ++ '_send').asSymbol.kr(default ?? 0.5, lag: 0.1, spec:[0, 1])
        })
      );
    };
  };
};

~setSidechain = M { arg sidechain;
  proxyManager.p.use {
    ~reverbSend[2] = \filter -> { arg in;
      Compander.ar(
        in,
        sidechain,
        \thresh.kr(0.04),
        1,
        \compression.kr(0.08, 0.1),
        \attack.kr(0.01, 0.1),
        \release.kr(0.2, 0.1));
    }
  }
}
