package by.klnvch.link5dots.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import by.klnvch.link5dots.R;

class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    public DeviceListAdapter(Context context) {
        super(context, R.layout.device_name);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_name, parent, false);
        }

        TextView textView = (TextView) convertView;

        textView.setText(device.getName());

        return convertView;
    }
}
