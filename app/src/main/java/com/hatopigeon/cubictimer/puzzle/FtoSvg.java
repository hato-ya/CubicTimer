package com.hatopigeon.cubictimer.puzzle;

import org.worldcubeassociation.tnoodle.svglite.Dimension;
import org.worldcubeassociation.tnoodle.svglite.Svg;

public class FtoSvg extends Svg {
    public String strSvg;
    public FtoSvg(Dimension size) {
        super(size);
    }

    public FtoSvg(Dimension size, String str) {
        super(size);
        strSvg = str;
    }

    @Override
    public String toString() {
        return strSvg;
    }
}
