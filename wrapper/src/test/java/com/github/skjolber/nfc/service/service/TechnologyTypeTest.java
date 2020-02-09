package com.github.skjolber.nfc.service.service;

import com.github.skjolber.nfc.service.TechnologyType;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TechnologyTypeTest {

    @Test
    public void iso14443Test() {
        byte[] atr = new byte[]{0x3b, (byte)0x8f, (byte)0x80, 0x01, (byte)0x80, 0x4f, 0x0c, (byte)0xa0, 0x00, 0x00, 0x03, 0x06, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x68};

        Assert.assertTrue(TechnologyType.isISO14443Part3Or4Header(atr));

        Assert.assertTrue(TechnologyType.isNFCA(atr));
    }
}