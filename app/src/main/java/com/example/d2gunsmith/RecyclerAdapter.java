package com.example.d2gunsmith;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    Context parentContext;
    ArrayList<GameItem> list;

    public RecyclerAdapter (Context context, ArrayList<GameItem> list) {
        parentContext = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parentContext).inflate(R.layout.adapter_layout, parent, false);
        RecyclerViewHolder holder = new RecyclerViewHolder(view);
        return holder;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        holder.itemName.setText(list.get(position).name());

        holder.itemImage.setImageBitmap(list.get(position).getIcon());
        holder.holderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentContext, ItemViewActivity.class);
                intent.putExtra("gameItem", list.get(position));
                parentContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        //declare widgets here and do the findviewbyid stuffs
        TextView itemName;
        ImageView itemImage;
        LinearLayout holderLayout;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.id_holder_name);
            itemImage = itemView.findViewById(R.id.id_holder_itemImage);
            holderLayout = itemView.findViewById(R.id.id_holder_layout);
        }
    }

}
