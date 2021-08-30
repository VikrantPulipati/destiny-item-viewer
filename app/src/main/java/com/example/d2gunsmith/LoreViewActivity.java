package com.example.d2gunsmith;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoreViewActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ScrollView scrollView;

    GameItem gameItem;
    JsonObject itemDefinition;
    String loreHash;

    ImageView iconDisplay;
    TextView name;
    TextView flavorText;
    TextView loreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lore_view);

        progressBar = findViewById(R.id.id_loreView_progressBar);
        scrollView = findViewById(R.id.id_scrollView);

        gameItem = getIntent().getParcelableExtra("gameItem");
        loreHash = getIntent().getStringExtra("loreHash");

        iconDisplay = findViewById(R.id.id_loreView_icon);
        name = findViewById(R.id.id_loreView_name);
        flavorText = findViewById(R.id.id_loreView_flavorText);
        loreText = findViewById(R.id.id_loreView_loreText);

        new LoadLoreTask().execute(loreHash);
    }

    public class LoadLoreTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String loreHash = strings[0];
            JsonObject loreDefinition = new JsonObject();
            try {
                InputStream in = getResources().openRawResource(R.raw.lore);
                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    String id = reader.nextName();
                    if (id.equals(loreHash)) {
                        loreDefinition = JsonParser.parseReader(reader).getAsJsonObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String lore = loreDefinition.getAsJsonObject("displayProperties").get("description").getAsString();
            Log.d("TAG1", lore);
            return lore;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadLore(s);
        }
    }

    public void loadLore (String s) {

        itemDefinition = gameItem.getItemDefinition();

        name.setText(gameItem.name());
        flavorText.setText(gameItem.flavorText());

        iconDisplay.setImageBitmap(gameItem.getIcon());
        loreText.setText(s);

        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }
}