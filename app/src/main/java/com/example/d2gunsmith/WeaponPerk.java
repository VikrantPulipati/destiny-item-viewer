package com.example.d2gunsmith;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

public class WeaponPerk implements Parcelable {

    JsonObject perkDefinition;
    Bitmap perkIcon;

    public WeaponPerk (JsonObject def, Bitmap icon) {
        this.perkDefinition = def;
        this.perkIcon = icon;
    }

    protected WeaponPerk(Parcel in) {
        perkDefinition = JsonParser.parseString(in.readString()).getAsJsonObject();
        perkIcon = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<WeaponPerk> CREATOR = new Creator<WeaponPerk>() {
        @Override
        public WeaponPerk createFromParcel(Parcel in) {
            return new WeaponPerk(in);
        }

        @Override
        public WeaponPerk[] newArray(int size) {
            return new WeaponPerk[size];
        }
    };

    public Bitmap getIcon () {
        return this.perkIcon;
    }

    public JsonObject getPerkDefinition () {
        return this.perkDefinition;
    }

    public String name () {
        return this.perkDefinition.getAsJsonObject("displayProperties").get("name").getAsString();
    }

    public String description () {
        return this.perkDefinition.getAsJsonObject("displayProperties").get("description").getAsString();
    }

    public String getHash () {
        return this.perkDefinition.get("hash").getAsString();
    }

    public void setIcon (Bitmap img) {
        this.perkIcon = img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.perkDefinition.toString());
        dest.writeParcelable(perkIcon, flags);
    }
}
