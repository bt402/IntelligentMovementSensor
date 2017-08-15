package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BlockAdapter extends ArrayAdapter<Block> {

    Context context;
    int layoutResourceId;
    Block data[] = null;

    public BlockAdapter(Context context, int layoutResourceId, Block[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BlockHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new BlockHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else
        {
            holder = (BlockHolder)row.getTag();
        }

        if (position < data.length){
            Block block = data[position];
            holder.txtTitle.setText(block.text);
        }

        return row;
    }

    @Override
    public int getCount() {
        return 10;
    }

    static class BlockHolder
    {
        TextView txtTitle;
    }
}


