package com.example.imageupload;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>
{

    ArrayList<String> arrayList;
    Context context;



    public ImageListAdapter(ImageUploadActivity context, ArrayList<String> arrayList) {
        this.context =context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.imageData.setImageBitmap(bitmap);
        Glide.with(context).load(arrayList.get(position)).placeholder(R.drawable.ic_launcher_background).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.imageData);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageData = itemView.findViewById(R.id.imageData);
        }
    }

}
