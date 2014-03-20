package icom5047.aerobal.adapters;

import icom5047.aerobal.activities.R;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerAdapter extends ArrayAdapter<String> {

	public DrawerAdapter(Context context, List<String> options) {
		super(context, R.layout.row_drawer_lv, options);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String rowValue = this.getItem(position);
		
		LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
		View rowLayout = inflater.inflate(R.layout.row_drawer_lv, parent, false);
		
		TextView tv = (TextView) rowLayout.findViewById(R.id.row_drawer_tv);
		Typeface face = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/Roboto-Thin.ttf");
		tv.setTypeface(face);
		tv.setText(rowValue);
		
		return rowLayout;
	}

}
