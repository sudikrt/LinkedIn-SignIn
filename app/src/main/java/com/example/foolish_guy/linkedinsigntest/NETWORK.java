package com.example.foolish_guy.linkedinsigntest;

/**
 * Created by Foolish_Guy on 3/8/2017.
 */

public enum NETWORK {
    Linkedin ("linkedin"),
    Facebook ("facebook"),
    Twitter ("twitter");

    private final String text;

    NETWORK(final String text) {
        this.text = text;
    }


    @Override
    public String toString() {
        return text;
    }
}
