package com.example.d2gunsmith;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

public class WeaponStat implements Comparable, Parcelable {

    String hash;
    JsonObject statDefinition;
    int value;
    int index;
    boolean hasBar;

    public WeaponStat (String hash, JsonObject statDefinition, int value) {
        this.hash = hash;
        this.statDefinition = statDefinition;
        this.value = value;
        try {
            this.index = this.statDefinition.get("index").getAsInt();
            this.hasBar = setHasBar();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected WeaponStat(Parcel in) {
        hash = in.readString();
        statDefinition = JsonParser.parseString(in.readString()).getAsJsonObject();
        value = in.readInt();
        index = in.readInt();
        hasBar = in.readByte() != 0;
    }

    public static final Creator<WeaponStat> CREATOR = new Creator<WeaponStat>() {
        @Override
        public WeaponStat createFromParcel(Parcel in) {
            return new WeaponStat(in);
        }

        @Override
        public WeaponStat[] newArray(int size) {
            return new WeaponStat[size];
        }
    };

    public boolean setHasBar () throws JSONException {
        if (this.name().equals("Magazine") || this.name().equals("Rounds Per Minute") ||
                this.name().equals("Inventory Size") || this.name().equals("Recoil Direction") ||
                this.name().equals("Ammo Capacity") || this.name().equals("Charge Time")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean getHasBar () {
        return this.hasBar;
    }

    public String name () throws JSONException {
        return this.statDefinition.getAsJsonObject("displayProperties").get("name").getAsString();
    }

    public String toString () {
        try {
            return this.name() + ": " + this.getValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHash () {
        return this.hash;
    }

    public int getValue () {
        return this.value;
    }

    public int getIndex () {
        return this.index;
    }

    @Override
    public int compareTo(Object o) {
        WeaponStat other = (WeaponStat)o;
        if (this.getHasBar() && !other.getHasBar()) {
            return -1;
        }
        else if (!this.getHasBar() && other.getHasBar()) {
            return 1;
        } else {
            if (this.getIndex() < other.getIndex()) {
                return -1;
            }
            else if (this.getIndex() > other.getIndex()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hash);
        dest.writeString(statDefinition.toString());
        dest.writeInt(value);
        dest.writeInt(index);
        dest.writeByte((byte) (hasBar ? 1 : 0));
    }
}
