package com.riromain.oak.colorpickeroak2;

import android.os.Parcel;

import com.riromain.oak.colorpickeroak2.object.ColorInfo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by brenda on 23/06/16.
 */
public class ColorInfoTest {
    private ColorInfo mSubject;

    @Before
    public void setup() throws Exception {
        mSubject = new ColorInfo();
    }

    @Test
    public void testDescribeContents() throws Exception {
        assertEquals(0, mSubject.describeContents());
    }

    @Test
    public void testWriteToParcel() throws Exception {

    }

    @Test
    public void testRedValue() throws Exception {
        Integer red = 100;
        mSubject.setRedValue(red);
        assertEquals(red, mSubject.getRedValue());
//        colorInfo.setGreenValue(device.getIntVariable("green"));
//        colorInfo.setBlueValue(device.getIntVariable("blue"));
//        colorInfo.setWhiteValue(device.getIntVariable("white"));
//        colorInfo.setIntensity(device.getIntVariable("inten"));
    }

    @Test
    public void testGreenValue() throws Exception {
        Integer green = 100;
        mSubject.setGreenValue(green);
        assertEquals(green, mSubject.getGreenValue());
    }

    @Test
    public void testBlueValue() throws Exception {
        Integer blue = 100;
        mSubject.setBlueValue(blue);
        assertEquals(blue, mSubject.getBlueValue());
    }

    @Test
    public void testWhiteValue() throws Exception {
        Integer white = 100;
        mSubject.setWhiteValue(white);
        assertEquals(white, mSubject.getWhiteValue());
    }

    @Test
    public void testIntensity() throws Exception {
        Integer intensity = 100;
        mSubject.setIntensity(intensity);
        assertEquals(intensity, mSubject.getIntensity());
    }
}