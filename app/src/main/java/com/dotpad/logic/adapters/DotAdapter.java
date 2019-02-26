package com.dotpad.logic.adapters;

import java.util.List;

import com.dotpad.R;
import com.dotpad.model.Dot;
import com.dotpad.view.DotView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DotAdapter extends ArrayAdapter<Dot> {
	
    private Context mContext;
    private int mLayoutResourceId;
    private List<Dot> mObjects;

    public DotAdapter(Context context, int textViewResourceId, List<Dot> objects) {

        super(context, textViewResourceId, objects);

        this.mContext = context;
        this.mLayoutResourceId = textViewResourceId;
        this.mObjects = objects;
    }

    @Override
    public void add(Dot dot) {
    	this.mObjects.add(dot);
    }
    
    @Override
    public void remove(Dot object) {
    	this.mObjects.remove(object);
    }
    
    @Override
    public int getCount() {
    	return this.mObjects.size();
    }
    
    public List<Dot> getItems() {
    	return this.mObjects;
    }
    
    public void update(Dot dot) {
    
    	for (Dot child : this.mObjects)
    		if (child.id == dot.id) {
    		
    			child.text = dot.text;
    			child.color = dot.color;
    			child.size = dot.size;
    			child.position = dot.position;
    			break;
    		}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ItemHolder holder;

        final Dot item = this.mObjects.get(position);

        if (row == null) {

            LayoutInflater inflater = ((Activity)this.mContext).getLayoutInflater();
            row = inflater.inflate(this.mLayoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.dotView = (DotView)row.findViewById(R.id.dot_item_view);

            row.setTag(holder);
        }
        else {

            holder = (ItemHolder)row.getTag();
        }

        holder.dotView.setDot(item);

        return row;
    }

    private static class ItemHolder {

        public DotView dotView;
    }
}
