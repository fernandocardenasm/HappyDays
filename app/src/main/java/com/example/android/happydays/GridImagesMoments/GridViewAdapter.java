package com.example.android.happydays.GridImagesMoments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.happydays.ParseConstants;
import com.example.android.happydays.R;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by usuario on 19/06/2015.
 */
public class GridViewAdapter extends ArrayAdapter<ParseObject> {

    private Context context;
    private int layoutResourceId;
    private List<ParseObject> mMoments;

    public GridViewAdapter(Context context, int layoutResourceId, List<ParseObject> moments) {
        super(context, layoutResourceId, moments);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.mMoments = moments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ParseImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        ParseObject moment = mMoments.get(position);
        if (moment.getString(ParseConstants.KEY_MOMENT_TEXT).equals(ParseConstants.EMPTY_FIELD)){
            holder.imageTitle.setText("Happy moment!");
        }
        else{
            holder.imageTitle.setText(moment.getString(ParseConstants.KEY_MOMENT_TEXT));
        }
        ParseFile file = moment.getParseFile(ParseConstants.KEY_FILE);
        //Uri fileUri = Uri.parse(file.getUrl());
        holder.image.setParseFile(file);
        holder.image.loadInBackground();


        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ParseImageView image;
    }
    public void refill (List<ParseObject> moments){
        mMoments.clear();
        mMoments.addAll(moments);
        notifyDataSetChanged();
    }
}