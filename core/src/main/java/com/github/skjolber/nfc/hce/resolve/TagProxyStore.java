package com.github.skjolber.nfc.hce.resolve;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.github.skjolber.nfc.hce.tech.TagTechnology;

public class TagProxyStore {

    protected static final String TAG = TagProxyStore.class.getName();

    private static int counter = 1;

    public static int nextServiceHandle() {
        synchronized (TagProxyStore.class) {
            counter++;

            return counter;
        }
    }

    private List<TagProxy> items = new ArrayList<TagProxy>();

    public List<TagProxy> getItems() {
        return items;
    }

    public void setItems(List<TagProxy> items) {
        this.items = items;
    }

    public int add(int slotNumber, List<TagTechnology> technologies) {
        int next = nextServiceHandle();

        add(new TagProxy(next, slotNumber, technologies));

        return next;
    }

    public boolean add(TagProxy object) {
        return items.add(object);
    }

    public boolean remove(Object object) {
        return items.remove(object);
    }

    public void removeItem(int slotNumber) {
        for (TagProxy tagItem : items) {
            if (tagItem.getSlotNumber() == slotNumber) {
                items.remove(tagItem);

                return;
            }
        }

    }

    public TagProxy get(int serviceHandle) {
        Log.d(TAG, "Get service handle " + serviceHandle);
        for (TagProxy tagItem : items) {
            if (tagItem.getHandle() == serviceHandle) {
                return tagItem;
            }
        }
        return null;
    }

}
