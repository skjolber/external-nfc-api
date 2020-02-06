package com.skjolberg.hce.resolve;

import java.util.List;

import android.nfc.Tag;

import com.skjolberg.hce.tech.TagTechnology;

public class TagProxy {

    private int handle;
    private int slotNumber;

    private List<TagTechnology> technologies;

    private TagTechnology current;

    private boolean present = true;

    public TagProxy(int handle, int slotNumber, List<TagTechnology> technologies) {
        this.handle = handle;
        this.slotNumber = slotNumber;
        this.technologies = technologies;
    }

    public TagTechnology getCurrent() {
        return current;
    }

    public void setCurrent(TagTechnology current) {
        this.current = current;
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int id) {
        this.handle = id;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public List<TagTechnology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TagTechnology> technologies) {
        this.technologies = technologies;
    }

    public boolean add(TagTechnology object) {
        return technologies.add(object);
    }

    public TagTechnology getTechnology(int technology) {
        for (TagTechnology tech : technologies) {
            if (tech.getTagTechnology() == technology) {
                return tech;
            }
        }
        return null;
    }

    public boolean selectTechnology(int technology) {
        for (TagTechnology tech : technologies) {
            if (tech.getTagTechnology() == technology) {
                this.current = tech;

                return true;
            }
        }

        return false;
    }

    public void closeTechnology() {
        this.current = null;
    }

    public Tag rediscover(Object callback) {
        throw new RuntimeException();
    }

    public boolean isPresent() {
        return present;
    }
}
