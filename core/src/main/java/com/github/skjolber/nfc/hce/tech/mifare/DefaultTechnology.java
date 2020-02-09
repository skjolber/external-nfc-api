package com.github.skjolber.nfc.hce.tech.mifare;

import com.github.skjolber.nfc.hce.tech.TagTechnology;

public class DefaultTechnology implements TagTechnology {

    protected final int tagTechnology;
    protected int slotNumber;

    public DefaultTechnology(int tagTechnology, int slotNumber) {
        this.tagTechnology = tagTechnology;
        this.slotNumber = slotNumber;
    }

    @Override
    public int getTagTechnology() {
        return tagTechnology;
    }

    public int getSlotNumber() {
        return slotNumber;
    }
}
