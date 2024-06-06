package com.example.busmobileapp.ViewHolders;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.R;

public class InStudentsViewHolder extends RecyclerView.ViewHolder {
    public TextView isname,isroll,isstop,ispass,isph;
    public Button approve,disapprove;

    public InStudentsViewHolder(@NonNull View itemView) {
        super(itemView);
        isname=(TextView) itemView.findViewById(R.id.stunamer);
        isroll=(TextView) itemView.findViewById(R.id.stunrollr);
        isstop = (TextView) itemView.findViewById(R.id.stustopr);
        ispass = (TextView) itemView.findViewById(R.id.statusr);
        isph = (TextView) itemView.findViewById(R.id.stuphr);
        approve = (Button) itemView.findViewById(R.id.approvepass);
        disapprove = (Button) itemView.findViewById(R.id.disapprovepass);


    }
}
