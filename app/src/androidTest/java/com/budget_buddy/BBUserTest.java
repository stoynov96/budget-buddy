package com.budget_buddy;

import android.support.test.runner.AndroidJUnit4;

import com.budget_buddy.exception.InvalidDataLabelException;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BBUserTest {
    @Test
    public void testWrite() throws InvalidDataLabelException {
        BBUser user = BBUser.GetInstance().SetUsername("uTest Write User");
        user.WriteUserInfo();
    }
    @Test
    public void testRead() throws InvalidDataLabelException, InterruptedException {
        BBUser user = BBUser.GetInstance().SetUsername("uTest Read User");
        user.LatchToDatabase();

        TimeUnit.SECONDS.sleep(3);
        assertEquals(23, user.getBudgetLevel());
    }

    @AfterClass
    public static void ConcludeTesting() throws InterruptedException {
        // todo: we should change this to wait for Firebase to finish writing
        TimeUnit.SECONDS.sleep(3);
    }
}
