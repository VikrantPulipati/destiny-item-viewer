package com.example.d2gunsmith;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PerkViewActivity extends AppCompatActivity {

    WeaponPerk weaponPerk;

    ImageView perkIconView;
    TextView nameView;
    TextView typeView;
    TextView descriptionView;
    LinearLayout statDisplay;

    TextView statHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perk_view);

        perkIconView = findViewById(R.id.id_perkView_icon);
        nameView = findViewById(R.id.id_perkView_name);
        typeView = findViewById(R.id.id_perkView_perkType);
        descriptionView = findViewById(R.id.id_perkView_description);
        statDisplay = findViewById(R.id.id_perkView_statDisplay);
        statHeading = findViewById(R.id.id_stat_heading);

        weaponPerk = getIntent().getParcelableExtra("weaponPerk");

        perkIconView.setImageBitmap(weaponPerk.getIcon());
        nameView.setText(weaponPerk.name());
        typeView.setText(weaponPerk.getPerkDefinition().get("itemTypeAndTierDisplayName").getAsString());
        descriptionView.setText(weaponPerk.getPerkDefinition().getAsJsonObject("displayProperties").get("description").getAsString());

        JsonArray perkStats = weaponPerk.getPerkDefinition().getAsJsonArray("investmentStats");
        if (perkStats.size() > 0) {
            statHeading.setVisibility(View.VISIBLE);
            for (JsonElement element : perkStats) {
                JsonObject obj = element.getAsJsonObject();
                JsonObject statDef = new JsonObject();
                try {
                    InputStream in = getResources().openRawResource(R.raw.stat);
                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String id = reader.nextName();
                        if (id.equals(obj.get("statTypeHash").toString())) {
                            statDef = JsonParser.parseReader(reader).getAsJsonObject();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String statName = statDef.getAsJsonObject("displayProperties").get("name").getAsString();
                int val = obj.get("value").getAsInt();
                String value = "";
                if (val > 0) {
                    value = "+" + val;
                } else {
                    value = "" + val;
                }

                TextView textView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, pixelFromDP(5), 0, 0);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                textView.setText("\t" + statName + ": " + value);
                textView.setTextSize(20);
                textView.setTextColor(getResources().getColor(R.color.text_gray));
                statDisplay.addView(textView);
            }
        }
    }

    public int pixelFromDP (int dp) {
        float ratio = this.getResources().getDisplayMetrics().density;
        return (int)(dp * ratio);
    }
}