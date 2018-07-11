package com.example.q.Tab1andTab2_01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ContentModel> contentModelArrayList;

    public ListViewAdapter(Context context, ArrayList<ContentModel> contentModelArrayList) {
        this.context = context;
        this.contentModelArrayList = contentModelArrayList;
    }

    @Override
    public int getCount() {
        return contentModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return contentModelArrayList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.item_listview, null);
            holder.tvtime = (TextView) convertView.findViewById(R.id.time_TEXTVIEW);
            holder.tvdesc = (TextView) convertView.findViewById(R.id.description_TEXTVIEW);
            holder.tvsum = (TextView) convertView.findViewById(R.id.sum_TEXTVIEW);
            convertView.setTag(holder);
            String author = contentModelArrayList.get(position).getAuthor();
            String title = contentModelArrayList.get(position).getTitle();
            holder.tvtime.setText(author);
            holder.tvdesc.setText(title);
            holder.tvsum.setText(" ");
        return convertView;
    }

    private class ViewHolder {
        protected TextView tvtime, tvdesc, tvsum;
    }
}