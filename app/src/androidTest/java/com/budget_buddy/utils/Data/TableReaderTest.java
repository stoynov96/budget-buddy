package com.budget_buddy.utils.Data;

import android.support.test.runner.AndroidJUnit4;

import com.budget_buddy.config.DataConfig;
import com.budget_buddy.exception.InvalidDataLabelException;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TableReaderTest {
    public static TableReader tableReader;

    @Test
    public void testLatch() throws InvalidDataLabelException, InterruptedException {
        tableReader = new TableReader();
        DummyUser user = new DummyUser("noname", 0.24);
        tableReader.Latch(DataConfig.DataLabels.TEST, user);

        TimeUnit.SECONDS.sleep(2); // give it some time to update

        Map<String, Object> expected = new HashMap<>();
        expected.put("mapLabelun", "testUName");
        expected.put("mapLabelB", 235.0);
        assertEquals(expected, user.ToMap());
    }

    @Test
    public void testLatchNested() throws InvalidDataLabelException, InterruptedException {
        tableReader = new TableReader();
        DummyUser user = new DummyUser("noname", 0.24);
        List<String> labels = new ArrayList<>();
        labels.add(DataConfig.DataLabels.TEST);
        labels.add(DataConfig.DataLabels.USERS);
        tableReader.Latch(labels, user);

        TimeUnit.SECONDS.sleep(2); // give it some time to update

        Map<String, Object> expected = new HashMap<>();
        expected.put("mapLabelun", "testUName");
        expected.put("mapLabelB", 235.0);
        assertEquals(expected, user.ToMap());
    }

    @AfterClass
    public static void ConcludeTesting() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
