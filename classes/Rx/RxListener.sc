RxListener {
  addObject { arg ... payload;
  }
  removeObject { arg ... payload;
  }

  updateObject { arg ... payload;
  }

  update { arg object, changer ... args;
    var action, payload;
    #action ... payload = args;
    this.perform(action, payload)
  }
}
