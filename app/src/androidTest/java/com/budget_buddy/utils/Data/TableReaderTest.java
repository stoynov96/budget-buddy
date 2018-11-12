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
        final Map<String, Object> expected = new HashMap<String, Object>() {{
            put("mapLabelun", "testUName");
            put("mapLabelB", 235.0);
        }};
        DummyUser user = new DummyUser("noname", 0.24) {
            // Check if the data is as expected whenever it changes
            @Override
            public void OnDataChange() {
                assertEquals(expected, ToMap());
            }
        };

        List<String> labels = new ArrayList<String>() {{
            add(DataConfig.DataLabels.TEST);
            add(DataConfig.DataLabels.TEST_USER);
        }};
        tableReader.Latch(labels, user);
    }

    @Test
    public void testLatchNested() throws InvalidDataLabelException, InterruptedException {
        tableReader = new TableReader();
        final Map<String, Object> expected = new HashMap<String, Object>() {{
            put("mapLabelun", "testUName");
            put("mapLabelB", 235.21);
        }};
        DummyUser user = new DummyUser("noname", 0.24) {
            // Check if the data is as expected whenever it changes
            @Override
            public void OnDataChange() {
                assertEquals(expected, ToMap());
            }
        };

        List<String> labels = new ArrayList<String>() {{
            add(DataConfig.DataLabels.TEST);
            add(DataConfig.DataLabels.USERS);
            add(DataConfig.DataLabels.TEST_USER);
        }};
        tableReader.Latch(labels, user);
    }

    @Test(expected = InvalidDataLabelException.class)
    public void testInvalidLabel() throws InvalidDataLabelException {
        tableReader = new TableReader();
        DummyUser user = new DummyUser("noname", 0.24);
        List<String> labels = new ArrayList<String>() {{
            add(DataConfig.DataLabels.TEST);
            add(DataConfig.DataLabels.USERS);
            add("InvalidLabel");
            add(DataConfig.DataLabels.TEST_USER);
        }};
        tableReader.Latch(labels, user);

        user = new DummyUser("noname", 0.24) {
            @Override
            public void OnDataChange() {
                assertEquals("asd", "ewt");
            }
        };

        user.OnDataChange();
    }

    @AfterClass
    public static void ConcludeTesting() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
