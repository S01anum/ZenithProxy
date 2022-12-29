package com.zenith.module;

import static com.zenith.util.Constants.EVENT_BUS;

/**
 * Module system base class.
 */
public abstract class Module {

    public Module() {
        EVENT_BUS.subscribe(this);
    }

    public void clientTickStarting() {
    }

    public void clientTickStopping() {
    }
}
