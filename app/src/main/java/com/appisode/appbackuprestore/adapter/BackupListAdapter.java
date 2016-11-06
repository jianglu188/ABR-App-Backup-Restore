package com.appisode.appbackuprestore.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appisode.appbackuprestore.R;
import com.appisode.appbackuprestore.model.BackupModel;

import java.util.ArrayList;
import java.util.List;

public class BackupListAdapter extends BaseAdapter {

    private List<BackupModel> original_items = new ArrayList<>();
    private List<BackupModel> filtered_items = new ArrayList<>();
    private Context contex;
    private ItemFilter mFilter = new ItemFilter();

    private LayoutInflater l_Inflater;

    public BackupListAdapter(Context context, List<BackupModel> items) {
        this.contex = context;
        original_items = items;
        filtered_items = items;
        l_Inflater = LayoutInflater.from(context);
    }

    public void setSelected(int position, boolean value) {
        filtered_items.get(position).setChecked(value);
    }

    public List<BackupModel> getSelected() {
        List<BackupModel> selected_item = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            if (filtered_items.get(i).isChecked()) {
                selected_item.add(filtered_items.get(i));
            }
        }
        return selected_item;
    }

    public void resetSelected() {
        for (int i = 0; i < getCount(); i++) {
            filtered_items.get(i).setChecked(false);
        }
    }

    public void selectAll() {
        for (int i = 0; i < getCount(); i++) {
            filtered_items.get(i).setChecked(true);
        }
        notifyDataSetChanged();
    }

    public List<BackupModel> getAllItes() {
        return filtered_items;
    }

    public int getCount() {
        return filtered_items.size();
    }

    public BackupModel getItem(int position) {
        return filtered_items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        BackupModel item = filtered_items.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.row_backup, null);
            holder = new ViewHolder();
            holder.txt_title = (TextView) convertView.findViewById(R.id.name);
            holder.txt_version = (TextView) convertView.findViewById(R.id.version);
            holder.txt_size = (TextView) convertView.findViewById(R.id.size);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.lyt_flag = (LinearLayout) convertView.findViewById(R.id.lyt_flag);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt_title.setText(item.getApp_name());
        holder.txt_version.setText("version : " + item.getVersion_name());
        holder.txt_size.setText(Formatter.formatFileSize(contex, item.getApp_memory()));
        holder.image.setImageDrawable(item.getApp_icon());
        if (item.isExist()) {
            holder.lyt_flag.setVisibility(View.VISIBLE);
        } else {
            holder.lyt_flag.setVisibility(View.GONE);
        }

        return convertView;
    }

    public Filter getFilter() {
        return mFilter;
    }

    static class ViewHolder {
        ImageView image;
        TextView txt_title;
        TextView txt_version;
        TextView txt_size;
        LinearLayout lyt_flag;
    }


    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String query = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();
            final List<BackupModel> list = original_items;
            final List<BackupModel> result_list = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                String str_title = list.get(i).getApp_name();
                if (str_title.toLowerCase().contains(query)) {
                    result_list.add(list.get(i));
                }
            }

            results.values = result_list;
            results.count = result_list.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered_items = (List<BackupModel>) results.values;
            notifyDataSetChanged();
        }
    }

}
