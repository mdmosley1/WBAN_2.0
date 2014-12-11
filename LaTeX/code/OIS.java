public void onItemSelected(AdapterView<?> parent, View view, 
                           int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selected = parent.getItemAtPosition(pos).toString();

    if (selected.equals("Accelerometer")){
        RTPlot.setTitle("Accelerometer");
        RTPlot.setRangeLabel("Acceleration (G)");
    }
    else if (selected.equals("Gyroscope")){
        RTPlot.setTitle("Gyroscope");
        RTPlot.setRangeLabel("Deg/s");
    }
}
