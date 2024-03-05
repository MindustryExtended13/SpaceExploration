package se.util;

import org.jetbrains.annotations.Contract;

public enum EventState {
    PRE, POST;

    @Contract(pure = true)
    public boolean pre() {
        return this == PRE;
    }

    @Contract(pure = true)
    public boolean post() {
        return this == POST;
    }
}