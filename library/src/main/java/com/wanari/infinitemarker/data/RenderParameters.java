package com.wanari.infinitemarker.data;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Size;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class RenderParameters {
    private LatLngBounds drawingBounds;
    private List<PointF> vertexList;
    private Point drawingSize;
    private int markerCount;

    public void setDrawingBounds(LatLngBounds drawingBounds) {
        this.drawingBounds = drawingBounds;
    }

    public void setVertexList(List<PointF> vertexList) {
        this.vertexList = vertexList;
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
}
