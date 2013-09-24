package com.moallemi.math;

public class BipartiteMatcherFactory {
    public static final int LAWLER = 0;
    public static final int LP = 1;

    private int type = LAWLER;

    public void setType(int type) {
        this.type = type;
    }

    public void setType(String name) {
        if (name.equals("lawler"))
            type = LAWLER;
        else if (name.equals("lp"))
            type = LP;
        else
            throw new IllegalArgumentException("unknown type: " + name);
    }

    public BipartiteMatcher newMatcher(int n) {
        switch (type) {
        case LAWLER:
            return new LawlerBipartiteMatcher(n);
        case LP:
            return new LPBipartiteMatcher(n);
        }
        throw new IllegalArgumentException("unknown type: " + type);
    }
}