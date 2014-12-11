void updatePlot(String uuidStr, dataPoint point) {
    double[] d = point.getDatac();

    if (selected.equals("Accelerometer")){
        for (int i = 0; i < 3; i++) {
            RTSeries[i].addFirst(null, d[i]);
        }
    }

    else if (selected.equals("Gyroscope")){
        for (int i = 0; i < 3; i++) {
            RTSeries[i].addFirst(null, d[i+3]);
        }
    }
