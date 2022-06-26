Seq : RxEvent {
  classvar lastId = 1000;

  *getId {
    lastId = lastId + 1;
    ^format("e%", lastId).asSymbol
  }

  add { arg obj;
    var id = Seq.getId;
    obj.id = id;
    this.put(id, RxEvent(obj));
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
