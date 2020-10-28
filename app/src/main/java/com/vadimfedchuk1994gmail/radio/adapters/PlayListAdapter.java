/* email of the author of the code soloviev_alexey@bk.ru */

package com.vadimfedchuk1994gmail.radio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vadimfedchuk1994gmail.radio.R;
import com.vadimfedchuk1994gmail.radio.models.PlayListPOJO;

import java.util.ArrayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private ArrayList<PlayListPOJO> obj;
    private Context mContext;

    public PlayListAdapter(ArrayList<PlayListPOJO> obj, Context mContext){
        this.obj = obj;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListAdapter.ViewHolder holder, int position) {
        String html = null;
        if(position == 0){
            holder.root.setBackgroundColor(Color.parseColor("#33cc66"));
            html = "<font color='#000000'>"+obj.get(position).getDate()+"</font> "+obj.get(position).getName();
        } else {
            holder.root.setBackgroundColor(Color.parseColor("#ffffff"));
            html = "<font color='#9da3ad'>"+obj.get(position).getDate()+"</font> "+obj.get(position).getName();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           holder.name.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.name.setText(Html.fromHtml(html));
        }
    }

    @Override
    public int getItemCount() {
        return obj.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout root;
        TextView name;
        View separator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.itemPlayList_root);
            name = itemView.findViewById(R.id.itemPlayList_name);
            separator = itemView.findViewById(R.id.itemPlayList_separator);
            Typeface geometriaFace = Typeface.createFromAsset(mContext.getAssets(), "geometria.ttf");
            name.setTypeface(geometriaFace);
            root.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
