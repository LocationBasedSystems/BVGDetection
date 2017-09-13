package de.htwberlin.f4.ai.ma.fingerprint.show_fingerprint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import de.htwberlin.f4.ai.ma.fingerprint.Fingerprint;

/**
 * Created by Johann Winter
 */


public class ShowFingerprintAdapter extends BaseExpandableListAdapter{

    private Fingerprint fingerprint;
    private Context context;
    //private List<String> groups;
    //private List<SignalInformation> groups;


    public ShowFingerprintAdapter(Context context, Fingerprint fingerprint) {
        this.context = context;
        this.fingerprint = fingerprint;

        //this.groups = fingerprint.getSignalInformationList();
    }


    @Override
    public int getGroupCount() {
        return fingerprint.getSignalInformationList().size();
    }

    @Override
    public int getChildrenCount(int i) {
        return fingerprint.getSignalInformationList().get(i).getAccessPointSampleList().size();
    }

    @Override
    public Object getGroup(int i) {
        return fingerprint.getSignalInformationList().get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return fingerprint.getSignalInformationList().get(i).getAccessPointSampleList().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        TextView textView = new TextView(context);
        textView.setText("Second no. " + i);
        textView.setPadding(100, 0, 0, 0);
        textView.setTextSize(20);
        return textView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        textView.setText(fingerprint.getSignalInformationList().get(i).getAccessPointSampleList().get(i1).getMacAddress() +
                "   " + fingerprint.getSignalInformationList().get(i).getAccessPointSampleList().get(i1).getRSSI() + " dBm");
        textView.setPadding(140, 0, 0, 0);
        return textView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}