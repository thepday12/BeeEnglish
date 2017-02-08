package com.ahiho.apps.beeenglish.model.realm_object;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by theptokim on 12/12/16.
 */

public class SubObject extends RealmObject {
    private String name;
    private RealmList<SubDetailObject> data;

    public SubObject() {
    }

    public SubObject(int id, String name) {
        this.name = name;
    }

    public SubObject(String name, RealmList<SubDetailObject> data) {
        this.name = name;
        this.data = data;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<SubDetailObject> getData() {
        return data;
    }

    public void setData(RealmList<SubDetailObject> data) {
        this.data = data;
    }
}
