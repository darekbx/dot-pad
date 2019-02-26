package com.dotpad.logic.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.dotpad.R;
import com.dotpad.model.Dot;
import com.dotpad.view.DotPreview;

public class ArchiveAdapter extends ArrayAdapter<Dot> {

    private class DotFilter extends Filter {

        private Object mLock = new Object();

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            addFilteredItems((List<Dot>) results.values);
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {

            if (prefix == null || prefix.length() == 0) {

                ArrayList<Dot> list;

                synchronized (mLock) {
                    list = new ArrayList<>(mObjects);
                }

                for (Dot dot : list)
                    dot.clearSelection();

                FilterResults results = new FilterResults();
                results.values = list;
                return results;
            }
            else {

                String prefixString = toLower(prefix.toString());

                final ArrayList<Dot> newValues = new ArrayList<>();
                ArrayList<Dot> values;

                synchronized (mLock) {
                    values = new ArrayList<>(mObjects);
                }

                final int count = values.size();

                for (int i = 0; i < count; i++) {

                    final Dot filteredDot = values.get(i);
                    filteredDot.clearSelection();

                    if (filterObject(filteredDot, prefixString)) {
                        filteredDot.setSelection(prefixString);
                        newValues.add(filteredDot);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = newValues;
                return results;
            }
        }

        private boolean filterObject(Dot dot, String constraint) {

            final String text = toLower(dot.text);

            if (text.contains(constraint))
                return true;

            return false;
        }

        private String toLower(String value) {
            return value.toLowerCase(Locale.ENGLISH);
        }
    }

    private DotFilter mFilter;
    private Context mContext;
    private int mLayoutResourceId;
    private List<Dot> mObjects;
    private List<Dot> mFilteredObjects;

    public ArchiveAdapter(Context context, int textViewResourceId, List<Dot> objects) {

        super(context, textViewResourceId, objects);

        this.mContext = context;
        this.mLayoutResourceId = textViewResourceId;
        this.mObjects = new ArrayList<Dot>(objects);
        this.mFilteredObjects = objects;
    }

    @Override
    public void addAll(Collection<? extends Dot> collection) {
        this.mObjects.addAll(collection);
        this.mFilteredObjects.addAll(collection);
    }

    public void addFilteredItems(List<Dot> items) {
        this.mFilteredObjects.clear();
        this.mFilteredObjects.addAll(items);
        this.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (this.mFilter == null)
            this.mFilter = new DotFilter();
        return this.mFilter;
    }

    @Override
    public void clear() {
        super.clear();
        this.mObjects.clear();
        this.mFilteredObjects.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ItemHolder holder;

        final Dot item = this.mFilteredObjects.get(position);

        if (row == null) {

            LayoutInflater inflater = ((Activity)this.mContext).getLayoutInflater();
            row = inflater.inflate(this.mLayoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.previewView = (DotPreview)row.findViewById(R.id.archive_row_preview);
            holder.indexView = (TextView)row.findViewById(R.id.archive_row_index);
            holder.dateView = (TextView)row.findViewById(R.id.archive_row_date);
            holder.noteView = (TextView)row.findViewById(R.id.archive_row_note);

            row.setTag(holder);
        }
        else {

            holder = (ItemHolder)row.getTag();
        }

        holder.previewView.setDot(item);
        
        holder.indexView.setText(this.formatIndex(item.index));
        holder.indexView.setTextSize(8);
        
        holder.dateView.setText(this.formatDate(item.date));
        holder.noteView.setText(item.text);

        if (item.hasSelection()) {

            Pair<String, Integer> selection = item.getSelection();
            SpannableString spannablecontent = new SpannableString(item.text);
            spannablecontent.setSpan(new BackgroundColorSpan(Color.argb(100, 255, 120, 0)),
                    selection.second, selection.second + selection.first.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.noteView.setText(spannablecontent);
        }

        return row;
    }
    
    private String formatDate(long date) {
    	return new SimpleDateFormat(this.mContext.getString(R.string.format_date), Locale.US).format(new Date(date));
    }
    
    private String formatIndex(int index) {
    	return this.mContext.getString(R.string.format_index, index);
    }

    private static class ItemHolder {

    	public DotPreview previewView;
        public TextView indexView;
        public TextView dateView;
        public TextView noteView;
    }
}