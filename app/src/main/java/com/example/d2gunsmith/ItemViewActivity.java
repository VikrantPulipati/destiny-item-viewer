package com.example.d2gunsmith;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ItemViewActivity extends AppCompatActivity {

    Context context = this;

    ProgressBar loadingCircle;

    ScrollView scrollView;

    ImageView icon;
    ImageView screenshot;
    TextView name;
    TextView element;
    TextView weaponType;
    TextView flavorText;
    Button readLore;
    TextView ammoType;
    TextView breakerType;
    RecyclerView statRecycler;

    GameItem gameItem;

    JsonObject itemDefinition;

    StatAdapter statAdapter;

    ImageView intrinsicIcon;
    TextView intrinsicName;
    TextView intrinsicDescription;

    LinearLayout socketLayout;
    TextView curatedHeader;
    LinearLayout curatedPerkGridLayout;
    TextView randomHeader;
    LinearLayout randomPerkGridLayout;

    LinearLayout intrinsicDisplay;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        scrollView = findViewById(R.id.id_scrollView);
        scrollView.setVisibility(View.GONE);

        loadingCircle = findViewById(R.id.id_loadingCircle);
        loadingCircle.setVisibility(View.GONE);

        icon = findViewById(R.id.id_itemView_icon);
        screenshot = findViewById(R.id.id_itemView_screenshot);
        name = findViewById(R.id.id_itemView_name);
        element = findViewById(R.id.id_element);
        weaponType = findViewById(R.id.id_weaponType);
        flavorText = findViewById(R.id.id_flavorText);
        readLore = findViewById(R.id.id_readLore);
        ammoType = findViewById(R.id.id_ammoType);
        breakerType = findViewById(R.id.id_breakerType);

        gameItem = getIntent().getParcelableExtra("gameItem");
        itemDefinition = gameItem.getItemDefinition();

        new LoadInspectTask().execute(itemDefinition);

        icon.setImageBitmap(gameItem.getIcon());
        name.setText(gameItem.name());
        weaponType.setText(gameItem.weaponType());
        flavorText.setText(gameItem.flavorText());

        if (gameItem.getItemDefinition().has("loreHash")) {
            readLore.setVisibility(View.VISIBLE);
            readLore.setOnClickListener(v -> {
                String loreHash = gameItem.getItemDefinition().get("loreHash").getAsString();
                Intent intent = new Intent(context, LoreViewActivity.class);
                intent.putExtra("loreHash", loreHash);
                intent.putExtra("gameItem", gameItem);
                context.startActivity(intent);
            });
        }

        statRecycler = findViewById(R.id.id_statRecycler);

        intrinsicIcon = findViewById(R.id.id_intrinsicPerk_icon);
        intrinsicName = findViewById(R.id.id_intrinsicPerk_name);
        intrinsicDescription = findViewById(R.id.id_intrinsicPerk_description);

        socketLayout = findViewById(R.id.id_socketLayout);
        curatedHeader = findViewById(R.id.id_curatedPerk_Header);
        curatedPerkGridLayout = findViewById(R.id.id_curatedPerkGrid_layout);
        randomHeader = findViewById(R.id.id_randomPerk_Header);
        randomPerkGridLayout = findViewById(R.id.id_randomPerkGrid_Layout);

        intrinsicDisplay = findViewById(R.id.id_intrinsic_display);
    }

    public class LoadInspectTask extends AsyncTask<JsonObject, Void, ArrayList<Object>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingCircle.setVisibility(View.VISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected ArrayList<Object> doInBackground(JsonObject... params) {


            ArrayList<Object> output = new ArrayList<>();
            JsonObject itemDef = params[0];

            if (itemDef.has("screenshot")) {
                String screenshotURL = "https://www.bungie.net" + itemDef.get("screenshot").getAsString();
                Bitmap scrnshot = null;
                try {
                    InputStream in = new URL(screenshotURL).openStream();
                    scrnshot = BitmapFactory.decodeStream(in);
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (itemDef.has("secondaryIcon")) {
                    String foundryURL = "https://www.bungie.net" + itemDef.get("secondaryIcon").getAsString();
                    Bitmap foundry = null;
                    try {
                        InputStream in = new URL(foundryURL).openStream();
                        foundry = BitmapFactory.decodeStream(in);
                        in.close();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (scrnshot != null) {
                        scrnshot = scrnshot.copy(scrnshot.getConfig(), true);
                        Canvas canvas = new Canvas(scrnshot);
                        if (foundry != null) {
                            canvas.drawBitmap(foundry, 0, 0, null);
                        }
                    }
                }

                scrnshot = Bitmap.createScaledBitmap(scrnshot, 1280, 720, false);

                output.add(scrnshot);

                ArrayList<WeaponStat> statList = new ArrayList<>();
                JsonObject stats = itemDef.getAsJsonObject("stats").getAsJsonObject("stats");
                Set<String> keySet = stats.keySet();
                for (String key : keySet) {
                    if (!(key.equals("hasDisplayableStats") || key.equals("primaryBaseStatHash") || key.equals("1935470627") ||
                            key.equals("1885944937") || key.equals("3784226438") || key.equals("1480404414") ||
                            key.equals("3597844532") || key.equals("3988418950") || key.equals("3907551967") ||
                            key.equals("3291498656") || key.equals("3291498659") || key.equals("3291498658") ||
                            key.equals("3291498661") || key.equals("3409715177") || key.equals("953546184") ||
                            key.equals("2299076437") || key.equals("3123546339"))) {
                        JsonObject stat = stats.getAsJsonObject(key);
                        String hash = stat.get("statHash").getAsString();
                        int value = stat.get("value").getAsInt();
                        if (value > 0) {
                            JsonObject statDef = new JsonObject();

                            try {
                                InputStream in = getResources().openRawResource(R.raw.stat);
                                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String id = reader.nextName();
                                    if (id.equals(hash)) {
                                        statDef = JsonParser.parseReader(reader).getAsJsonObject();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            statList.add(new WeaponStat(hash, statDef, value));
                        }

                    }
                }

                Collections.sort(statList);
                output.add(statList);
            }

            ArrayList<Socket> socketList = new ArrayList<>();
            JsonArray socketEntries = itemDef.getAsJsonObject("sockets").getAsJsonArray("socketEntries");
            for (int i = 0; i < socketEntries.size(); i++) {
                String socketTypeHash = socketEntries.get(i).getAsJsonObject().get("socketTypeHash").getAsString();
                JsonObject socketTypeDef = null;
                ArrayList<WeaponPerk> curatedPerkList = new ArrayList<>();
                ArrayList<WeaponPerk> randomPerkList = null;
                if (!socketTypeHash.equals("0")) {
                    socketTypeDef = new JsonObject();
                    try {
                        InputStream in = getResources().openRawResource(R.raw.sockettype);
                        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String id = reader.nextName();
                            if (id.equals(socketTypeHash)) {
                                socketTypeDef = JsonParser.parseReader(reader).getAsJsonObject();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (socketTypeDef.get("socketCategoryHash").getAsString().equals("4241085061") ||
                            socketTypeDef.get("socketCategoryHash").getAsString().equals("3956125808")) {
                        if (i < 5) {
                            if (socketEntries.get(i).getAsJsonObject().has("reusablePlugSetHash")) {
                                String reusablePlugSetHash = socketEntries.get(i).getAsJsonObject().get("reusablePlugSetHash").getAsString();
                                JsonObject reusablePlugSetDefinition = new JsonObject();
                                try {
                                    InputStream in = getResources().openRawResource(R.raw.plugset);
                                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String id = reader.nextName();
                                        if (id.equals(reusablePlugSetHash)) {
                                            reusablePlugSetDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                                        } else {
                                            reader.skipValue();
                                        }
                                    }
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                JsonArray curatedPerkArray = reusablePlugSetDefinition.getAsJsonArray("reusablePlugItems");
                                for (int j = 0; j < curatedPerkArray.size(); j++) {
                                    if (curatedPerkArray.get(j).getAsJsonObject().get("currentlyCanRoll").getAsBoolean()) {
                                        JsonObject curatedPerkDefinition = new JsonObject();
                                        try {
                                            InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                                            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                            reader.beginObject();
                                            while (reader.hasNext()) {
                                                String id = reader.nextName();
                                                if (id.equals(curatedPerkArray.get(j).getAsJsonObject().get("plugItemHash").getAsString())) {
                                                    curatedPerkDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                                                } else {
                                                    reader.skipValue();
                                                }
                                            }
                                            reader.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        Bitmap curatedPerkIcon = null;
                                        String curatedPerkIconURL = "https://www.bungie.net" + curatedPerkDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();
                                        try {
                                            InputStream in = new URL(curatedPerkIconURL).openStream();
                                            curatedPerkIcon = BitmapFactory.decodeStream(in);
                                            in.close();
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            curatedPerkIcon = null;
                                            e.printStackTrace();
                                        }
                                        //Log.d("TAG1", String.valueOf(curatedPerkIcon.getRowBytes()*curatedPerkIcon.getHeight()));
                                        curatedPerkList.add(new WeaponPerk(curatedPerkDefinition, curatedPerkIcon));
                                    }
                                }

                            }
                            else if (socketEntries.get(i).getAsJsonObject().getAsJsonArray("reusablePlugItems").size() > 0) {
                                JsonArray reusablePlugItems = socketEntries.get(i).getAsJsonObject().getAsJsonArray("reusablePlugItems");
                                for (JsonElement obj : reusablePlugItems) {
                                    JsonObject reusablePlugItem = obj.getAsJsonObject();
                                    String plugItemHash = reusablePlugItem.get("plugItemHash").getAsString();
                                    JsonObject curatedPerkDefinition = new JsonObject();
                                    try {
                                        InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                                        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            String id = reader.nextName();
                                            if (id.equals(plugItemHash)) {
                                                curatedPerkDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                                            } else {
                                                reader.skipValue();
                                            }
                                        }
                                        reader.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Bitmap curatedPerkIcon = null;
                                    String curatedPerkIconURL = "https://www.bungie.net" + curatedPerkDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();
                                    try {
                                        InputStream in = new URL(curatedPerkIconURL).openStream();
                                        curatedPerkIcon = BitmapFactory.decodeStream(in);
                                        in.close();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        curatedPerkIcon = null;
                                        e.printStackTrace();
                                    }
                                    //Log.d("TAG1", String.valueOf(curatedPerkIcon.getRowBytes()*curatedPerkIcon.getHeight()));
                                    curatedPerkList.add(new WeaponPerk(curatedPerkDefinition, curatedPerkIcon));
                                }
                            } else {
                                JsonObject curatedPerkDefinition = new JsonObject();
                                if (socketEntries.get(i).getAsJsonObject().get("singleInitialItemHash").getAsString().equals("0")) {
                                    break;
                                }
                                try {
                                    InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String id = reader.nextName();
                                        if (id.equals(socketEntries.get(i).getAsJsonObject().get("singleInitialItemHash").getAsString())) {
                                            curatedPerkDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                                        } else {
                                            reader.skipValue();
                                        }
                                    }
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Bitmap curatedPerkIcon = null;
                                String curatedPerkIconURL = "https://bungie.net" + curatedPerkDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();
                                try {
                                    InputStream in = new URL(curatedPerkIconURL).openStream();
                                    curatedPerkIcon = BitmapFactory.decodeStream(in);
                                    in.close();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    curatedPerkIcon = null;
                                    e.printStackTrace();
                                }

                                curatedPerkList.add(new WeaponPerk(curatedPerkDefinition, curatedPerkIcon));
                            }


                            if (socketEntries.get(i).getAsJsonObject().has("randomizedPlugSetHash")) {
                                gameItem.setHasRandomRolls(true);

                                randomPerkList = new ArrayList<>();

                                JsonObject plugSetDefinition = new JsonObject();
                                try {
                                    InputStream in = getResources().openRawResource(R.raw.plugset);
                                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String id = reader.nextName();
                                        if (id.equals(socketEntries.get(i).getAsJsonObject().get("randomizedPlugSetHash").getAsString())) {
                                            plugSetDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                                        } else {
                                            reader.skipValue();
                                        }
                                    }
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                JsonArray randomPerkArray = plugSetDefinition.getAsJsonArray("reusablePlugItems");
                                ArrayList<String> randomPerkHashArray = new ArrayList<>();
                                for (JsonElement element : randomPerkArray) {
                                    JsonObject randomPerkDefinition = element.getAsJsonObject();
                                    if (randomPerkDefinition.get("currentlyCanRoll").getAsBoolean()) {
                                        randomPerkHashArray.add(randomPerkDefinition.get("plugItemHash").getAsString());
                                    }
                                }
                                ArrayList<JsonObject> randomPerkDefinitionArray = new ArrayList<>();
                                try {
                                    InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        String id = reader.nextName();
                                        if (randomPerkHashArray.contains(id)) {
                                            randomPerkDefinitionArray.add(JsonParser.parseReader(reader).getAsJsonObject());
                                            if (randomPerkDefinitionArray.size() == randomPerkHashArray.size()) {
                                                break;
                                            }
                                        } else {
                                            reader.skipValue();
                                        }
                                    }
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                for (JsonObject perk : randomPerkDefinitionArray) {
                                    Bitmap randomPerkIcon = null;
                                    String randomPerkIconURL = "https://www.bungie.net" + perk.getAsJsonObject("displayProperties").get("icon").getAsString();
                                    try {
                                        InputStream in = new URL(randomPerkIconURL).openStream();
                                        randomPerkIcon = BitmapFactory.decodeStream(in);
                                        in.close();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        randomPerkIcon = null;
                                        e.printStackTrace();
                                    }

                                    WeaponPerk randomPerk = new WeaponPerk(perk, randomPerkIcon);
                                    boolean contains = false;
                                    for (WeaponPerk wepPerk : randomPerkList) {
                                        if (wepPerk.getHash().equals(randomPerk.getHash())) {
                                            contains = true;
                                            break;
                                        }
                                    }
                                    if (!contains) {
                                        randomPerkList.add(randomPerk);
                                    }
                                }
                            }

                            Socket socket = new Socket(socketTypeDef, curatedPerkList, randomPerkList);
                            Log.d("TAG1", socket.toString());
                            socketList.add(socket);
                        }
                    }
                }
            }
            output.add(socketList);
            return output;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            super.onPostExecute(objects);
            loadInspect(objects);
        }
    }

    public void loadInspect (ArrayList<Object> list) {
        gameItem.setScreenshot((Bitmap)list.get(0));
        screenshot.setImageBitmap(gameItem.getScreenshot());

        int damageType = itemDefinition.get("defaultDamageType").getAsInt();
        if (damageType == 1) {
            Drawable d = getResources().getDrawable(R.drawable.kinetic);
            d.setBounds(0, 0, 85, 85);
            element.setCompoundDrawables(d, null, null, null);
            String str = "  - Deals KINETIC damage";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.text_gray));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            element.setText(ss);
        }
        else if (damageType == 2) {
            Drawable d = getResources().getDrawable(R.drawable.arc);
            d.setBounds(0, 0, 85, 85);
            element.setCompoundDrawables(d, null, null, null);
            String str = "  - Deals ARC damage";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.arc_blue));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            element.setText(ss);
        }
        else if (damageType == 3) {
            Drawable d = getResources().getDrawable(R.drawable.solar);
            d.setBounds(0, 0, 85, 85);
            element.setCompoundDrawables(d, null, null, null);
            String str = "  - Deals SOLAR damage";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.solar_orange));
            ss.setSpan(f, 10, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 10, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            element.setText(ss);
        }
        else if (damageType == 4) {
            Drawable d = getResources().getDrawable(R.drawable.void_);
            d.setBounds(0, 0, 85, 85);
            element.setCompoundDrawables(d, null, null, null);
            String str = "  - Deals VOID damage";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.void_purple));
            ss.setSpan(f, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            element.setText(ss);
        }
        else if (damageType == 6) {
            Drawable d = getResources().getDrawable(R.drawable.stasis);
            d.setBounds(0, 0, 85, 85);
            element.setCompoundDrawables(d, null, null, null);
            String str = "  - Deals STASIS damage";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.stasis_blue));
            ss.setSpan(f, 10, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 10, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            element.setText(ss);
        }

        int ammo = itemDefinition.getAsJsonObject("equippingBlock").get("ammoType").getAsInt();
        if (ammo == 1) {
            Drawable d = getResources().getDrawable(R.drawable.primary);
            d.setBounds(0, 0, 90, 60);
            ammoType.setCompoundDrawables(d, null, null, null);
            String str = "  - Uses PRIMARY ammo";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.white));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ammoType.setText(ss);
        }
        else if (ammo == 2) {
            Drawable d = getResources().getDrawable(R.drawable.special);
            d.setBounds(0, 0, 90, 60);
            ammoType.setCompoundDrawables(d, null, null, null);
            String str = "  - Uses SPECIAL ammo";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.special_green));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 9, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ammoType.setText(ss);
        }
        else if (ammo == 3) {
            Drawable d = getResources().getDrawable(R.drawable.heavy);
            d.setBounds(0, 0, 90, 60);
            ammoType.setCompoundDrawables(d, null, null, null);
            String str = "  - Uses HEAVY ammo";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.heavy_purple));
            ss.setSpan(f, 9, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 9, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ammoType.setText(ss);
        }

        int breaker = itemDefinition.get("breakerType").getAsInt();
        if (breaker == 1) {
            breakerType.setVisibility(View.VISIBLE);
            Drawable d = getResources().getDrawable(R.drawable.shieldpiercing);
            d.setBounds(0, 0, 85, 85);
            breakerType.setCompoundDrawables(d, null, null, null);
            String str = "  - Pierces BARRIER Shields";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.text_gray));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 12, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 12, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            breakerType.setText(ss);
        }
        else if (breaker == 2) {
            breakerType.setVisibility(View.VISIBLE);
            Drawable d = getResources().getDrawable(R.drawable.disruption);
            d.setBounds(0, 0, 85, 85);
            breakerType.setCompoundDrawables(d, null, null, null);
            String str = "  - Disrupts OVERLOAD Champions";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.text_gray));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 13, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 13, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            breakerType.setText(ss);
        }
        else if (breaker == 3) {
            breakerType.setVisibility(View.VISIBLE);
            Drawable d = getResources().getDrawable(R.drawable.stagger);
            d.setBounds(0, 0, 85, 85);
            breakerType.setCompoundDrawables(d, null, null, null);
            String str = "  - Staggers UNSTOPPABLE Champions";
            SpannableString ss = new SpannableString(str);
            ForegroundColorSpan f = new ForegroundColorSpan(getResources().getColor(R.color.text_gray));
            StyleSpan sp = new StyleSpan(Typeface.BOLD);
            ss.setSpan(sp, 13, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(f, 13, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            breakerType.setText(ss);
        }
        gameItem.setWeaponStats((ArrayList<WeaponStat>)list.get(1));
        statAdapter = new StatAdapter(this, gameItem.getWeaponStats());
        statRecycler.setAdapter(statAdapter);
        statRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        statAdapter.notifyDataSetChanged();

        gameItem.setSocketList((ArrayList<Socket>)list.get(2));

        for (WeaponPerk archetype : gameItem.getSocketList().get(0).getCuratedPerkList()) {
            intrinsicIcon.setImageBitmap(archetype.getIcon());
            intrinsicName.setText(archetype.name());
            intrinsicDescription.setText(archetype.description());
            intrinsicDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PerkViewActivity.class);
                    intent.putExtra("weaponPerk", archetype);
                    context.startActivity(intent);
                }
            });
        }

        if (gameItem.getSocketList().size() > 1) {
            socketLayout.setVisibility(View.VISIBLE);
            for (int i = 1; i < gameItem.getSocketList().size(); i++) {
                curatedHeader.setVisibility(View.VISIBLE);
                curatedPerkGridLayout.setVisibility(View.VISIBLE);
                Socket perkCol = gameItem.getSocketList().get(i);
                ArrayList<WeaponPerk> curatedPerkList = perkCol.getCuratedPerkList();
                LinearLayout curatedPerkColLayout = new LinearLayout(this);
                curatedPerkColLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams curatedParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                curatedParams.setMargins(pixelFromDP(5), pixelFromDP(5), pixelFromDP(5), pixelFromDP(5));
                curatedPerkColLayout.setLayoutParams(curatedParams);
                for (WeaponPerk curatedPerk : curatedPerkList) {
                    ImageView curatedPerkIcon = new ImageView(this);
                    curatedPerkIcon.setLayoutParams(new LinearLayout.LayoutParams(pixelFromDP(70), pixelFromDP(70)));
                    if (curatedPerk.getIcon() == null) {
                        String url = "https://bungie.net" + curatedPerk.getPerkDefinition().getAsJsonObject("displayProperties").get("icon").getAsString();
                        Bitmap img = null;
                        try {
                            img = new DownloadImageTask().execute(url).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        curatedPerk.setIcon(img);
                    } else {
                        curatedPerkIcon.setImageBitmap(curatedPerk.getIcon());
                    }
                    curatedPerkIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PerkViewActivity.class);
                            intent.putExtra("weaponPerk", curatedPerk);
                            context.startActivity(intent);
                        }
                    });
                    curatedPerkColLayout.addView(curatedPerkIcon);
                }
                curatedPerkGridLayout.addView(curatedPerkColLayout);

                if (perkCol.getRandomPerkList() != null) {
                    randomHeader.setVisibility(View.VISIBLE);
                    randomPerkGridLayout.setVisibility(View.VISIBLE);
                    ArrayList<WeaponPerk> randomPerkList = perkCol.getRandomPerkList();
                    LinearLayout randomPerkColLayout  = new LinearLayout(this);
                    randomPerkColLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams randomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    randomParams.setMargins(pixelFromDP(5), pixelFromDP(5), pixelFromDP(5), pixelFromDP(5));
                    randomPerkColLayout.setLayoutParams(randomParams);
                    for (WeaponPerk randomPerk : randomPerkList) {
                        ImageView randomPerkIcon = new ImageView(this);
                        randomPerkIcon.setLayoutParams(new LinearLayout.LayoutParams(pixelFromDP(70), pixelFromDP(70)));
                        if (randomPerk.getIcon() == null) {
                            String url = "https://bungie.net" + randomPerk.getPerkDefinition().getAsJsonObject("displayProperties").get("icon").getAsString();
                            Bitmap img = null;
                            try {
                                img = new DownloadImageTask().execute(url).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            randomPerk.setIcon(img);
                        } else {
                            randomPerkIcon.setImageBitmap(randomPerk.getIcon());
                        }
                        randomPerkIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, PerkViewActivity.class);
                                intent.putExtra("weaponPerk", randomPerk);
                                context.startActivity(intent);
                            }
                        });
                        randomPerkColLayout.addView(randomPerkIcon);
                    }
                    randomPerkGridLayout.addView(randomPerkColLayout);
                }
            }
        }

        loadingCircle.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    public int pixelFromDP (int dp) {
        float ratio = this.getResources().getDisplayMetrics().density;
        return (int)(dp * ratio);
    }
}