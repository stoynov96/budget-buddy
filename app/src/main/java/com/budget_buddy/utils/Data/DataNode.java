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

    /**
     * Function to trigger when data this node is latched onto has changed.
     * This can be overridden for each specific object to insert custom logic
     */
    void OnDataChange();
}
