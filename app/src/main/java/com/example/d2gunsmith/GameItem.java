package com.example.d2gunsmith;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GameItem implements Parcelable {

    JsonObject itemDefinition;

    Bitmap icon;
    Bitmap screenshot;

    ArrayList<WeaponStat> weaponStats;

    ArrayList<Socket> socketList;

    boolean hasRandomRolls = false;

    public GameItem (JsonObject def, Bitmap ic) {
        itemDefinition = def;
        icon = ic;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected GameItem (Parcel in) {
        itemDefinition = JsonParser.parseString(in.readString()).getAsJsonObject();
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        weaponStats = new ArrayList<>();
        in.readParcelableList(weaponStats, WeaponStat.class.getClassLoader());
        socketList = new ArrayList<>();
        in.readParcelableList(socketList, Socket.class.getClassLoader());
    }

    public String name () {
        return itemDefinition.getAsJsonObject("displayProperties").get("name").getAsString();
    }

    public String weaponType() {
        return itemDefinition.get("itemTypeAndTierDisplayName").getAsString();
    }

    public String flavorText () {
        return itemDefinition.get("flavorText").getAsString();
    }

    public void setHasRandomRolls (boolean b) {
        this.hasRandomRolls = b;
    }

    public boolean hasRandomRolls() {
        return this.hasRandomRolls;
    }

    public Bitmap getIcon () {
        return this.icon;
    }

    public Bitmap getScreenshot () {
        return this.screenshot;
    }

    public void setScreenshot (Bitmap img) {
        this.screenshot = img;
    }

    public void setWeaponStats (ArrayList<WeaponStat> list) {
        this.weaponStats = list;
    }

    public void setSocketList (ArrayList<Socket> list) {
        this.socketList = list;
    }

    public ArrayList<Socket> getSocketList () {
        return this.socketList;
    }

    public ArrayList<WeaponStat> getWeaponStats () {
        return this.weaponStats;
    }

    public JsonObject getItemDefinition () {
        return this.itemDefinition;
    }

    public static final Creator<GameItem> CREATOR = new Creator<GameItem>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public GameItem createFromParcel(Parcel source) {
            return new GameItem(source);
        }

        @Override
        public GameItem[] newArray(int size) {
            return new GameItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.itemDefinition.toString());
        dest.writeParcelable(this.icon, flags);
        if (weaponStats != null) {
            dest.writeParcelableList(weaponStats, flags);
        }
        if (socketList != null) {
            dest.writeParcelableList(socketList, flags);
        }
    }
}
