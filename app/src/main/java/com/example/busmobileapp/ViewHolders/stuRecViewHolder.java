package com.example.busmobileapp.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.R;

public class stuRecViewHolder extends RecyclerView.ViewHolder {
    public ImageView img;
    public TextView t1;

    public stuRecViewHolder(@NonNull View itemView) {
        super(itemView);
        img = (ImageView) itemView.findViewById(R.id.img1);
        t1=(TextView) itemView.findViewById(R.id.t1);


    }
}
