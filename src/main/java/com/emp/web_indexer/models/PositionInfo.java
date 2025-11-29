package com.emp.web_indexer.models;

public class PositionInfo {
    private final int position;
    private final String tag;
    private final double tagWeight;

    public PositionInfo(int position, String tag, double tagWeight) {
        this.position = position;
        this.tag = tag;
        this.tagWeight = tagWeight;
    }

    public int getPosition()
    {
        return this.position;
    }
    public String getTag()
    {
        return this.tag;
    }

    public double getTagWeight()
    {
        return this.tagWeight;
    }
}
