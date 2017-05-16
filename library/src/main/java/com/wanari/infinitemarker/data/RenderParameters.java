package com.wanari.infinitemarker.data;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Size;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class RenderParameters {
    private LatLngBounds drawingBounds;
    private List<PointF> vertexList = new ArrayList<>();
    private Point drawingSize;
    private PointF dimensionInMeter;
    private int markerCount;

    public void setDrawingBounds(LatLngBounds drawingBounds) {
        this.drawingBounds = drawingBounds;
    }

    public void setDrawingSize(Point drawingSize) {
        this.drawingSize = drawingSize;
    }

    public LatLngBounds getDrawingBounds() {
        return drawingBounds;
    }

    public List<PointF> getVertexList() {
        return vertexList;
    }

    public Point getDrawingSize() {
        return drawingSize;
    }

    public int getMarkerCount() {
        return markerCount;
    }

    public void setMarkerCount(int markerCount) {
        this.markerCount = markerCount;
    }

    public PointF getDimensionInMeter() {
        return dimensionInMeter;
    }

    public void setDimensionInMeter(PointF dimensionInMeter) {
        this.dimensionInMeter = dimensionInMeter;
    }

    public void setVertexList(List<PointF> vertexList) {
        this.vertexList = vertexList;
    }
}
