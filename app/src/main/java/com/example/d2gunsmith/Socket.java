package com.example.d2gunsmith;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Socket implements Parcelable {

    JsonObject socketTypeDefinition;
    ArrayList<WeaponPerk> curatedPerkList;
    ArrayList<WeaponPerk> randomPerkList;

    String socketName;

    public Socket (JsonObject socketTypeDefinition, ArrayList<WeaponPerk> curatedPerkList, ArrayList<WeaponPerk> randomPerkDefinitionList) {
        this.socketTypeDefinition = socketTypeDefinition;
        this.curatedPerkList = curatedPerkList;
        this.randomPerkList = randomPerkDefinitionList;

        socketName = socketTypeDefinition.getAsJsonArray("plugWhitelist").get(0).getAsJsonObject().get("categoryIdentifier").getAsString();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Socket(Parcel in) {
        socketTypeDefinition = JsonParser.parseString(in.readString()).getAsJsonObject();
        curatedPerkList = new ArrayList<>();
        in.readParcelableList(curatedPerkList, WeaponPerk.class.getClassLoader());
        randomPerkList = new ArrayList<>();
        in.readParcelableList(randomPerkList, WeaponPerk.class.getClassLoader());
        socketName = in.readString();
    }

    public static final Creator<Socket> CREATOR = new Creator<Socket>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Socket createFromParcel(Parcel in) {
            return new Socket(in);
        }

        @Override
        public Socket[] newArray(int size) {
            return new Socket[size];
        }
    };

    public String toString () {
        return this.socketName;
    }

    public ArrayList<WeaponPerk> getCuratedPerkList() {
        return this.curatedPerkList;
    }

    public ArrayList<WeaponPerk> getRandomPerkList () {
        return this.randomPerkList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(socketTypeDefinition.toString());
        dest.writeParcelableList(curatedPerkList, flags);
        dest.writeParcelableList(randomPerkList, flags);
        dest.writeString(socketName);
    }
}
