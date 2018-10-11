package com.budget_buddy.utils.Data;

import java.util.Map;

public interface DataNode {
    /**
     * Converts this object to a map that will be written to the database
     * @return Map to be written to the database
     */
    Map<String, Object> ToMap();

    /**
     * Assigns values to this DataNode from a map
     */
    void GetFromMap(Map<String, Object> map);
}
