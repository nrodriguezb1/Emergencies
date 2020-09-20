package dte.masteriot.mdp.emergencies;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String>{

    private Context context;
    private ArrayList<String> nameCameras;
    private MainActivity mainActivity;
    //private int[] alertFlag;
    //private CameraData cameraData;

    //Constructor

    public CustomAdapter(Context _context, ArrayList _nameCameras, MainActivity ma) {   //, int[] _alertFlag    //, MainActivity ma
        super(_context, 0, _nameCameras);
        this.context = _context;
        this.nameCameras = _nameCameras;
        //this.alertFlag = _alertFlag;
        this.mainActivity = ma;
    }

   /*
    public CustomAdapter(Context _context, ArrayList _cameraListViewArrayList, MainActivity ma){
        super(_context, 0, _cameraListViewArrayList);
        this.cameraListViewArrayList = _cameraListViewArrayList;
    }

     */

    //This view is called when a listItem needs to be created and populated with the data
    //In this method first the View is inflated using the LayoutInflator.inflate() method
    //In this view, you should set the data into the views
    //@Override
    public View getView(final int position, View convertView, ViewGroup parent){    //int[] _alarm

        //With this if, we reuse the views and I want a new view per each name of camera
        /*
        if(convertView == null){
            LayoutInflater layoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.custom_list_view, parent, false);
        }
         */
        //The view is inflated
        LayoutInflater layoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.custom_list_view, parent, false);

        //Pass the information to each view (a RadioButton and a text)
        //RadioButton cameraName = (RadioButton) convertView.findViewById(R.id.rbCameraName);
        String currentName = nameCameras.get(position);
        int alert = mainActivity.cameraData.getAlarmFlagOnPosition(position);
        //Log.d("CustomAdapterAlert", ""+alert);
        CheckedTextView tvCameraName = (CheckedTextView) convertView.findViewById(R.id.tvCameraName);
        tvCameraName.setText(currentName);

        //Change the color
        if(alert == 1){
            tvCameraName.setBackgroundColor(Color.RED);
        }
        else{
            tvCameraName.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }
}
