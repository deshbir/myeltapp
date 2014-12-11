package com.myeltapp;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleListAdapter extends BaseAdapter {

	private Context _context;
    private List<String> _linksDataHeader; // header titles
	public SimpleListAdapter(Context context, List<String> linksDataHeader){
		this._context = context;
		this._linksDataHeader = linksDataHeader;
	}
    
    @Override
	public int getCount() {
    	return this._linksDataHeader.size();
	}

	@Override
	public Object getItem(int position) {
		return this._linksDataHeader.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String linksListHeaderTitle = (String) getItem(position);
		if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.links_list_item, null);
        }
 
        TextView linksListHeaderView = (TextView) convertView
                .findViewById(R.id.links_list_header);
        ImageView linksListIcons=(ImageView) convertView.findViewById(R.id.links_list_icons);
        if(position == 0){
        	linksListIcons.setImageResource(R.drawable.home128);
        }else if(position == 1){
        	linksListIcons.setImageResource(R.drawable.profile128);
		}else if(position == 2){
			linksListIcons.setImageResource(R.drawable.messages128);
		}else {
			linksListIcons.setImageResource(R.drawable.help128);
		}
        linksListHeaderView.setTypeface(null, Typeface.BOLD);
        linksListHeaderView.setText(linksListHeaderTitle);
		return convertView;
	}

	
	

}
