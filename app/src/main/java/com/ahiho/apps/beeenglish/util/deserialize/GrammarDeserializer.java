package com.ahiho.apps.beeenglish.util.deserialize;

import com.ahiho.apps.beeenglish.model.realm_object.GrammarObject;
import com.ahiho.apps.beeenglish.model.realm_object.SubDetailObject;
import com.ahiho.apps.beeenglish.model.realm_object.SubObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import io.realm.RealmList;

/**
 * Created by theptokim on 12/14/16.
 */

public class GrammarDeserializer implements JsonDeserializer<GrammarObject> {

    @Override
    public GrammarObject deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final int id = jsonObject.get("id").getAsInt();

        final String name = jsonObject.get("name").getAsString();


        final JsonArray data = jsonObject.get("data").getAsJsonArray();
        RealmList<SubObject> subObjects =new RealmList<>();
        for(int i=0;i<data.size();i++){
            final JsonObject subData =data.get(i).getAsJsonObject();
            final JsonArray jsonDetailData = subData.get("data").getAsJsonArray();
            RealmList<SubDetailObject> subDetailObjects = new RealmList<>();
            for(int j=0;j<jsonDetailData.size();j++){
                final JsonObject jsonDetail = jsonDetailData.get(j).getAsJsonObject();
                subDetailObjects.add(new SubDetailObject(jsonDetail.get("name").getAsString(),jsonDetail.get("detail").getAsString(),jsonDetail.get("link").getAsString()));
            }

            subObjects.add(new SubObject(subData.get("name").getAsString(), subDetailObjects));
        }
        //The deserialisation code is missing

        final GrammarObject grammarObject = new GrammarObject();
        grammarObject.setId(id);
        grammarObject.setName(name);
        grammarObject.setData(subObjects);
        return grammarObject;
    }
}

