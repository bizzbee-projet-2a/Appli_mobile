package com.desbois.mathis.bizzbee;

import org.json.JSONArray;

public interface Connectable {
    void makeRequest();
    void computeData(JSONArray j);
}
