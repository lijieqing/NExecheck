package com.kstech.nexecheck;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.kstech.nexecheck.utils.MD5Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.kstech.nexecheck", appContext.getPackageName());
    }
    @Test
    public void generateCheckTxt() throws Exception{
        String mac = MD5Utils.getMac();
        String md5 = MD5Utils.md5(mac);
        MD5Utils.generateMD5(md5);
    }

    @Test
    public void resourceCopy() throws Exception{
        Context appContext = InstrumentationRegistry.getTargetContext();
    }
}
