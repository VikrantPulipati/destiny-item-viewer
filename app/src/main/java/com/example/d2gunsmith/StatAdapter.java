package com.example.d2gunsmith;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

public class StatAdapter extends RecyclerView.Adapter<StatAdapter.StatViewHolder> {

    Context parentContext;
    ArrayList<WeaponStat> list;

    public StatAdapter (Context context, ArrayList<WeaponStat> list) {
        this.parentContext = context;
        this.list = list;
    }

    @NonNull
    @Override
    public StatAdapter.StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parentContext).inflate(R.layout.stat_adapter_layout, parent, false);
        StatAdapter.StatViewHolder holder = new StatAdapter.StatViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StatAdapter.StatViewHolder holder, int position) {
        holder.statName.setText(list.get(position).toString());
        if (list.get(position).getHasBar()) {
            holder.statBar.setVisibility(View.VISIBLE);
            holder.statBar.setProgress(list.get(position).getValue());
            holder.statBar.getProgressDrawable().setColorFilter(parentContext.getResources().getColor(R.color.text_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            holder.statBar.setVisibility(View.GONE);
            holder.statName.setText(list.get(position).toString() + "                                                   ");
            try {
                if (list.get(position).name().equals("Recoil Direction")) {
                    int val = list.get(position).getValue();
                    double func = Math.sin((val+5)*2*Math.PI/20)*(100-val);
                    if (Math.abs(func) < 1) {
                        func = 0;
                    }
                    if (func > 0) { //tends right
                        holder.statName.setText(list.get(position).toString() + " - Tends Right                         ");
                    }
                    else if (func < 0) { //tends left
                        holder.statName.setText(list.get(position).toString() + " - Tends Left                            ");
                    } else { //tends vertical
                        holder.statName.setText(list.get(position).toString() + " - Tends Vertical                     ");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class StatViewHolder extends RecyclerView.ViewHolder {
        //declare widgets here and do the findviewbyid stuffs
        TextView statName;
        ProgressBar statBar;
        LinearLayout statLayout;

        public StatViewHolder (@NonNull View itemView) {
            super(itemView);
            statName = itemView.findViewById(R.id.id_stat_name);
            statBar = itemView.findViewById(R.id.id_stat_bar);
            statLayout = itemView.findViewById(R.id.id_stat_layout);
        }
    }
}
