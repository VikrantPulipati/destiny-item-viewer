package com.example.d2gunsmith;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Context context = this;

    RecyclerView recyclerView;
    EditText searchBar;
    Button searchButton;

    ProgressBar progressBar;
    ArrayList<GameItem> list;
    RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.id_recycler);

        searchBar = findViewById(R.id.id_searchBar);
        searchButton = findViewById(R.id.id_searchButton);


        progressBar = findViewById(R.id.id_progressBar);
        progressBar.setVisibility(View.GONE);

        list = new ArrayList<>();
        adapter = new RecyclerAdapter(this, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchButton.setOnClickListener(v -> {
            hideKeyboard();

            if (searchBar.getText().length() > 0) {
                list = new ArrayList<>();
                adapter.notifyDataSetChanged();
                new GetListTask().execute(searchBar.getText().toString());
            }

        });
    }

    public class GetListTask extends AsyncTask<String, Void, ArrayList<GameItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<GameItem> doInBackground(String... params){
            ArrayList<GameItem> list = new ArrayList<>();
            String apiKey = "b73d00bacaf74b4e9ed0c9aa6c3f7baa";
            String result = "";
            String inputLine;

            String stringURL = "https://www.bungie.net/Platform/Destiny2/Armory/Search/DestinyInventoryItemDefinition/" + params[0] + "/";

            try {
                URL url = new URL(stringURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("X-API-KEY", apiKey);
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);

                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

                BufferedReader reader = new BufferedReader(streamReader);

                while ((inputLine = reader.readLine()) != null) {
                    result = result.concat(inputLine);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JsonObject searchResults = JsonParser.parseString(result).getAsJsonObject();
            JsonArray array = searchResults.getAsJsonObject("Response").getAsJsonObject("results").getAsJsonArray("results");
            if (array.size() > 0) {
                ArrayList<String> itemHashArray = new ArrayList<>();
                for (JsonElement element : array) {
                    itemHashArray.add(element.getAsJsonObject().get("hash").getAsString());
                }
                ArrayList<JsonObject> searchResultArray = new ArrayList<>();
                try {
                    InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String id = reader.nextName();
                        if (itemHashArray.contains(id)) {
                            searchResultArray.add(JsonParser.parseReader(reader).getAsJsonObject());
                            if (searchResultArray.size() == itemHashArray.size()) {
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

                for (JsonObject searchResultDefintion : searchResultArray) {
                    boolean isWeapon = false;
                    if (searchResultDefintion.has("traitIds")) {
                        JsonArray arr = searchResultDefintion.getAsJsonArray("traitIds");
                        for (int j = 0; j < arr.size(); j++) {
                            if (arr.get(j).getAsString().equals("item_type.weapon")) {
                                isWeapon = true;
                                break;
                            }
                        }
                    }

                    if (isWeapon) {
                        String iconURL = "";
                        String watermarkURL = "";
                        Bitmap icon = null;
                        Bitmap watermark = null;

                        if (searchResultDefintion.getAsJsonObject("displayProperties").get("hasIcon").getAsBoolean() == true) {
                            iconURL = searchResultDefintion.getAsJsonObject("displayProperties").get("icon").getAsString();
                            iconURL = "https://www.bungie.net" + iconURL;
                            try {
                                InputStream in = new URL(iconURL).openStream();
                                icon = BitmapFactory.decodeStream(in);
                                in.close();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (searchResultDefintion.has("quality")) {
                            watermarkURL = searchResultDefintion.getAsJsonObject("quality").getAsJsonArray("displayVersionWatermarkIcons").get(searchResultDefintion.getAsJsonObject("quality").getAsJsonArray("displayVersionWatermarkIcons").size() - 1).getAsString();
                            watermarkURL = "https://www.bungie.net" + watermarkURL;
                            try {
                                InputStream in = new java.net.URL(watermarkURL).openStream();
                                watermark = BitmapFactory.decodeStream(in);
                                in.close();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (searchResultDefintion.has("iconWatermark")) {
                            DownloadImageTask task = new DownloadImageTask();
                            watermarkURL = searchResultDefintion.get("iconWatermark").getAsString();
                            watermarkURL = "https://www.bungie.net" + watermarkURL;
                            try {
                                InputStream in = new java.net.URL(watermarkURL).openStream();
                                watermark = BitmapFactory.decodeStream(in);
                                in.close();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            watermark = null;
                        }
                        if (icon != null) {
                            icon = icon.copy(icon.getConfig(), true);
                            Canvas canvas = new Canvas(icon);
                            if (watermark != null) {
                                canvas.drawBitmap(watermark, 0, 0, null);
                            }
                        }
                        list.add(new GameItem(searchResultDefintion, icon));
                    }
                }
            }

            /*if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    String hash = array.getJSONObject(i).getString("hash");
                    JsonObject itemDefinition = new JsonObject();
                    try {
                        InputStream in = getResources().openRawResource(R.raw.inventoryitem);
                        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String id = reader.nextName();
                            if (id.equals(hash)) {
                                itemDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    boolean isWeapon = false;
                    if (itemDefinition.has("traitIds")) {
                        JsonArray arr = itemDefinition.getAsJsonArray("traitIds");
                        for (int j = 0; j < arr.size(); j++) {
                            if (arr.get(j).getAsString().equals("item_type.weapon")) {
                                isWeapon = true;
                                break;
                            }
                        }
                    }
                    if (isWeapon) {
                        String iconURL = "";
                        String watermarkURL = "";
                        Bitmap icon = null;
                        Bitmap watermark = null;

                        if (itemDefinition.getAsJsonObject("displayProperties").get("hasIcon").getAsBoolean() == true) {
                            iconURL = itemDefinition.getAsJsonObject("displayProperties").get("icon").getAsString();
                            iconURL = "https://www.bungie.net" + iconURL;
                            try {
                                InputStream in = new URL(iconURL).openStream();
                                icon = BitmapFactory.decodeStream(in);
                                in.close();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            if (itemDefinition.has("quality")) {
                                watermarkURL = itemDefinition.getAsJsonObject("quality").getAsJsonArray("displayVersionWatermarkIcons").get(itemDefinition.getAsJsonObject("quality").getAsJsonArray("displayVersionWatermarkIcons").size() - 1).getAsString();
                                watermarkURL = "https://www.bungie.net" + watermarkURL;
                                try {
                                    InputStream in = new java.net.URL(watermarkURL).openStream();
                                    watermark = BitmapFactory.decodeStream(in);
                                    in.close();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (itemDefinition.has("iconWatermark")) {
                                DownloadImageTask task = new DownloadImageTask();
                                watermarkURL = itemDefinition.get("iconWatermark").getAsString();
                                watermarkURL = "https://www.bungie.net" + watermarkURL;
                                try {
                                    InputStream in = new java.net.URL(watermarkURL).openStream();
                                    watermark = BitmapFactory.decodeStream(in);
                                    in.close();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                watermark = null;
                            }
                        } catch (NullPointerException e) {}

                        if (icon != null) {
                            icon = icon.copy(icon.getConfig(), true);
                            Canvas canvas = new Canvas(icon);
                            if (watermark != null) {
                                canvas.drawBitmap(watermark, 0, 0, null);
                            }
                        }
                        list.add(new GameItem(itemDefinition, icon));
                    }
                }
            }*/

            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<GameItem> gameItems) {
            super.onPostExecute(gameItems);
            doStuff(gameItems);
        }
    }

    public void doStuff (ArrayList<GameItem> l) {
        list = l;
        adapter = new RecyclerAdapter(context, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        if (list.size() == 0) {
            Toast.makeText(context, "No results found!", Toast.LENGTH_LONG).show();
        }
    }

    public void hideKeyboard () {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}