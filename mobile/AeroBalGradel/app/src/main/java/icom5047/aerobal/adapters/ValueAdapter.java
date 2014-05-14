package icom5047.aerobal.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.NumberFormat;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.ValueContainer;
import icom5047.aerobal.resources.GlobalConstants;

/**
 * Created by enrique on 5/14/14.
 */
public class ValueAdapter extends ArrayAdapter<ValueContainer> {

    public ValueAdapter(Context context, ValueContainer[] values){
        super(context, R.layout.row_average_2_unit_lv, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ValueContainer container = this.getItem(position);

        LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
        //Inflate 3 Value Layout
        View view = inflater.inflate(R.layout.row_average_3_unit_lv, parent, false);

        if(!container.hasUnit){
            view = inflater.inflate(R.layout.row_average_2_unit_lv, parent, false);
        }

        ((TextView) view.findViewById(R.id.rowAveUnitTitle)).setText(container.title);

        TextView tvValue = (TextView) view.findViewById(R.id.rowAveUnitValue);

        if(container.isString){

            tvValue.setText(  container.strValue );

        }
        else{
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);
            nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
            tvValue.setText(nf.format(container.value));

        }


        if(container.hasUnit){
            ((TextView) view.findViewById(R.id.rowAve3UnitUnit)).setText(container.unit);
        }



        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}