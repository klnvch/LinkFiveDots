package by.klnvch.link5dots.nsd;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import by.klnvch.link5dots.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ServiceListAdapter extends ArrayAdapter<NsdServiceInfo>{

    public ServiceListAdapter(Context context){
        super(context, R.layout.device_name);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NsdServiceInfo service = getItem(position);

        LayoutInflater mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_name, parent, false);
        }

        TextView textView = (TextView)convertView;

        textView.setText(service.getServiceName());

        return convertView;
    }

}
