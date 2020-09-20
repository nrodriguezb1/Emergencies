package dte.masteriot.mdp.emergencies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    private ListView lvCameras;
    private TextView selectionURL;
    private TextView selectionCoordinates, tvEmergencies;
    private TextView modoURL;
    private ImageView ivCamera;
    private ArrayList<String> initialData = new ArrayList<>();
    ArrayList<Integer> allEmergencies = new ArrayList<>();
    //private String[] initialAlertFlag = new String[301];
    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private int numberOfEmergencies = 0;
    private CustomAdapter adapter;
    DownloadFileTask downloadFileTask = new DownloadFileTask();
    DownloadChannelsTask downloadChannelsTask = new DownloadChannelsTask();
    //CameraData
    ArrayList<String> mcameraListName = new ArrayList<>();
    ArrayList<String> mcameraListURLS = new ArrayList<>();
    ArrayList<String> mcameraListCoordinates = new ArrayList<>();
    //ArrayList<String> mcameraListNO2 = new ArrayList<>();
    String[] mcameraListNO2;
    //ArrayList<String> mcameraListChannels = new ArrayList<>();
    //ArrayList<Integer> mCameraListAlarmFlag = new ArrayList<>();
    int [] mCameraListAlarmFlag;

    CameraData cameraData;// = new CameraData(mcameraListName, mcameraListURLS, mcameraListCoordinates, mcameraListNO2, mCameraListAlarmFlag);

    //ChannelData
    ArrayList<String> mchannelListID = new ArrayList<>();
    ArrayList<String> mchannelListName = new ArrayList<>();
    ArrayList<String> mchannelListLat = new ArrayList<>();
    ArrayList<String> mchannelListLon = new ArrayList<>();
    ArrayList<String> mchannelListReadAPIKey = new ArrayList<>();
    //ArrayList<String> mchannelListNO2= new ArrayList<>();
    String[] mchannelListNO2;  // = new String [10]
    ArrayList<String> mchannelListNearestCameras = new ArrayList<>();

    ChannelData channelData;// = new ChannelData(mchannelListID, mchannelListName, mchannelListLat, mchannelListLon, mchannelListReadAPIKey, mchannelListNO2, mchannelListNearestCameras);
    double distance= 500.0;
    int emergencyCounter = 0;
    //API KEYS
    //Keys for accesing ThingSpeak
    private static final String UserAPIKey = "H1ISWCZ2T5PP9X9W";  //H1ISWCZ2T5PP9X9W (Cristina)   ZI31QQJN0F4GVCBQ (Mouhad)
    private static final String MQTTAPIKey = "KQY31YIATFBANL7W";  //KQY31YIATFBANL7W (Cristina)   F2K1ZLTZMI99MD1Q (Mouhad)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TextView URL
        //selectionURL = (TextView) findViewById(R.id.selectionURL);
        selectionCoordinates = (TextView) findViewById(R.id.selectionCoordinates);
        //modoURL = (TextView)findViewById(R.id.modoURL);
        ivCamera = (ImageView) findViewById(R.id.ivCamera);
        ivCamera.setImageResource(R.drawable.logo2);
        lvCameras = (ListView) findViewById(R.id.lvCameras);
        tvEmergencies = (TextView) findViewById(R.id.tvEmergencies);
        //tvEmergencies.setText("Number of emergencies: " + numberEmergencies);

        //Set choice mode
        lvCameras.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //Set ListView item click listener
        lvCameras.setOnItemClickListener(this);

        //Build ArrayAdapter and set layout
        //adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, cameraListName);
        //Build ArrayAdapter and set layout. For step 4
        //adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, initialData );
        //Build CustomAdpter and set layout. For final proyect, emergencies
        //adapter = new CustomAdapter(this,initialData);
        adapter = new CustomAdapter(this,initialData, this);   //, this
        //Set adapter for ListView
        lvCameras.setAdapter(adapter);

        //Initialize some arrayList of the cameraData
        //mcameraListChannels.addAll(null);
        //cameraData.getCameraListNO2().addAll(null);
        //cameraData.getCameraListAlarmFlag().addAll(-1);
        //Initialize some arrayList of the channelData
        //channelData.getChannelListNO2().addAll(null);

        downloadFileTask.execute(URL_CAMERAS);
        downloadChannelsTask.execute();
    }

    /*
    public void onDestroy() {
        super.onDestroy();
        for (int i=0; i<channelData.getChannelSize(); i++){
            String topic_to_unsubscribe = "channels/"+channelData.getChannelID(i)+"/subscribe/fields/field1/"+channelData.getChannelReadAPIKey(i);
            try {
                //Try to subscribe to all topics, that in this case are the channels
                mqttAndroidClient.unsubscribe(topic_to_unsubscribe);

            }catch (MqttException ex){
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
        }

    }
     */

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        DownloadImageTask downloadImageTask = new DownloadImageTask();

        String items_URL = "";
        String items_coord = "";
        SparseBooleanArray checked = lvCameras.getCheckedItemPositions();

        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                int pos = checked.keyAt(i);
                items_URL = items_URL + " " + cameraData.getURLSOnPosition(pos);
                items_coord = items_coord + " " + cameraData.getCoordinatesOnPosition(pos);
            }
        }
        //selectionURL.setText(items_URL);
        selectionCoordinates.setText(items_coord.substring(0,34));
        downloadImageTask.execute(items_URL);
    }

    //Método que ejecuta la pantalla de maps al clickar en la imagen
    //También muestra un marker en el lugar de la imagen
    public void cambiarPantalla(View view) {
        String coordenadass;
        Intent intent = new Intent(this, MapsActivity.class);
        coordenadass = selectionCoordinates.getText().toString();
        intent.putExtra("coordinates", coordenadass);
        startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// DISTANCE BETWEEN CHANNELS AND CAMERAS /////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Calculate the distance between two coordinates using the Harvesine Formula
    //https://www.movable-type.co.uk/scripts/latlong.html
    //LAT N/S, LON W/E
    /*
    public double distanceBtTwoCoordinates(String latChannel, String lonChannel, String coordCamera) {

        double lonCameraDouble = Double.parseDouble(coordCamera.substring(1, 15));  //1
        double latCameraDouble = Double.parseDouble(coordCamera.substring(19, 32)); //1
        double latChannelDouble = Double.parseDouble(latChannel);                  //2
        double lonChannelDouble = Double.parseDouble(lonChannel);                  //2

        double earthRadius = 6371.0; //m
        double latCamera_rad = latCameraDouble * (Math.PI / 180);
        double latChannel_rad = latChannelDouble * (Math.PI / 180);
        double deltaLat = (latChannelDouble - latCameraDouble) * (Math.PI / 180);
        double deltaLon = (lonChannelDouble - lonCameraDouble) * (Math.PI / 180);

        double a = Math.pow((Math.sin(deltaLat / 2)), 2) + Math.cos(latCamera_rad) * Math.cos(latChannel_rad) * Math.pow((Math.sin(deltaLon / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = earthRadius * c;
        return d;

    }

     */
    public double distanceBtTwoCoordinates(String latChannel, String lonChannel, String coordCamera){

        double distance = 0.0;

        double lonCameraDouble = Double.parseDouble(coordCamera.substring(0, 7));  //15
        double latCameraDouble = Double.parseDouble(coordCamera.substring(18, 25)); //32
        double latChannelDouble = Double.parseDouble(latChannel);
        double lonChannelDouble = Double.parseDouble(lonChannel);

        double oneTerm = Math.pow((lonCameraDouble-lonChannelDouble), 2);
        double secondTerm = Math.pow((latCameraDouble-latChannelDouble), 2);
        distance = Math.sqrt(oneTerm + secondTerm);
        return distance;
    }

    //This method calculate the nearest camera of a channel.
    //Go through the array of cameras, calculate the distance
    //between each camera and the given channel and, return the name of the camera nearest to that channel.
    public int positionOfTheNearestCamera(CameraData cameraData, String lat, String lon){

        double distanceMin = 50000000.0;
        int cameraPosition = -1;

        for (int i = 0; i < cameraData.getCameraListName().size(); i++) {
            double distance = distanceBtTwoCoordinates(lat, lon, cameraData.getCoordinatesOnPosition(i));
            if(distance<distanceMin){
                distanceMin = distance;
                cameraPosition = i;
            }
        }
        return cameraPosition;
    }

    //Go through the array of channels and save, for each channel, its nearest camera.
    //For each camera, the channel that is nearest to it is saved
    public void addTheNearestCamera(CameraData cameraData, ChannelData channelData){

        for (int j = 0; j<channelData.getChannelListID().size(); j++){
            int cameraPosition = positionOfTheNearestCamera(cameraData,channelData.getChannelLat(j), channelData.getChannelLon(j));
            Log.d("addTheNearestCamera:cameraPosition", ""+cameraPosition);
            //Save the nearest camera in the channel data
            channelData.addChannelListNearestCamera(cameraPosition);
        }
    }
    //Once we have the nearest camera to a channel, I need the information of the channel about NO2 to paint or not
    //the background of the camera in the ListView

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////// PARSE FILE XML //////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void parseFileXML(InputStream is, ArrayList URL, ArrayList names, ArrayList coordinates) {
        XmlPullParserFactory parserFactory;
        String cameraURL, cameraCoordinates, cameraName;
        String nameAttibute;

        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:

                        if ("description".equals(elementName)) {
                            cameraURL = parser.nextText();
                            cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                            cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4);
                            //cd.addCameraListURLS(cameraURL);
                            URL.add(cameraURL);
                            //cameraListViewArrayListInitial.

                        } else if ("Data".equals(elementName)) {
                            // Get the "name" attribute value by means of: parser.getAttributeValue(...)
                            nameAttibute = parser.getAttributeValue(0);
                            if ("Nombre".equals(nameAttibute)) {
                                //Jump to next TAG
                                parser.nextTag();
                                //Get tag text
                                cameraName = parser.nextText();
                                //cd.addCameraListName(cameraName);
                                names.add(cameraName);
                            }

                        } else if ("coordinates".equals(elementName)) {
                            cameraCoordinates = parser.nextText();
                            //cd.addCameraListCoordinates(cameraCoordinates);
                            coordinates.add(cameraCoordinates);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// CLASS DOWNLOAD FILE TASK //////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String response = "";

            //Abrir URL
            HttpURLConnection urlConnection = null;

            //Parse XML file
            XmlPullParserFactory parserFactory;
            String cameraURL;
            String nameAttibute;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream is = urlConnection.getInputStream();
                parseFileXML(is,mcameraListURLS, mcameraListName, mcameraListCoordinates);

                //Create the object cameraData
                mcameraListNO2 = new String[mcameraListName.size()];
                mCameraListAlarmFlag = new int[mcameraListName.size()];
                cameraData = new CameraData(mcameraListName, mcameraListURLS, mcameraListCoordinates, mcameraListNO2, mCameraListAlarmFlag);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            //Initialize some arrays of cameraData
            for(int i=0; i<cameraData.getCameraListName().size(); i++){
                String valueNull = "-999";
                int valueNullAlarm = 0;
                cameraData.addCameraListNO2OnPosition(valueNull, i);
                cameraData.addCameraListAlarmFlagOnPosition(0, i);
                //channelData.addChannelListNO2OnPosition(valueNull, i);
            }
            //Update ListVIew
            adapter.clear();
            adapter.addAll(cameraData.getCameraListName());
            adapter.notifyDataSetChanged();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// CLASS DOWNLOAD IMAGE TASK /////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private Bitmap loadedImage;
        //AsyncResponseImage delegateImage = null;

        protected Bitmap doInBackground(String... urls) {
            // Worker thread
            //Bitmap loadedImage;
            //Open URL
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                loadedImage = BitmapFactory.decodeStream(bis);

                bis.close();
                is.close();
                urlConnection.disconnect();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return loadedImage;
        }

        protected void onPostExecute(Bitmap bitmaps) {
            //Executed on UI thread
            ivCamera.setImageBitmap(bitmaps);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////// PARSE CHANNELS JSON /////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void parseChannelsJson(InputStream is, ArrayList ID, ArrayList names, ArrayList lat, ArrayList lon, ArrayList apiKey){

        Gson gson = new Gson();
        CHANNELS[]channels = gson.fromJson(new InputStreamReader(is), CHANNELS[].class );
        int nchannels = channels.length;
        String idChannel;
        String nameChannel;
        String latitudeChannel;
        String longitudeChannel;
        String readAPIKeyChannel;

        for (int j=0; j<nchannels; j++){

            //Obtain the ID of the channel
            idChannel = channels[j].id;
            ID.add(idChannel);

            //Obtain the name of the channel
            nameChannel = channels[j].name;
            names.add(nameChannel);

            //Obtain the latitude of the channel
            latitudeChannel = channels[j].latitude;
            lat.add(latitudeChannel);

            //Obtain the longitude of the channel
            longitudeChannel = channels[j].longitude;
            lon.add(longitudeChannel);

            //Obtain the Read API Key
            readAPIKeyChannel = channels[j].api_keys[1].api_key;
            apiKey.add(readAPIKeyChannel);

        }

    }
    public class CHANNELS{
        String id;
        String name;
        String latitude;
        String longitude;
        API_KEYS [] api_keys;
    }

    class API_KEYS{
        String api_key;
        String write_flag;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// CLASS DOWNLOAD CHANNEL TASK ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class DownloadChannelsTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String response = "";

            //Abrir URL
            HttpURLConnection urlConnection = null;
            String URL_CHANNEL = "https://api.thingspeak.com/channels.json?api_key="+UserAPIKey;
            //Log.d("URL","URL_CHANNEL.tostring()");
            try{
                URL url = new URL(URL_CHANNEL);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream is = urlConnection.getInputStream();
                parseChannelsJson(is, mchannelListID, mchannelListName, mchannelListLat, mchannelListLon, mchannelListReadAPIKey);

                //Create the object channelData
                mchannelListNO2 = new String[mchannelListID.size()];
                channelData = new ChannelData(mchannelListID, mchannelListName, mchannelListLat, mchannelListLon, mchannelListReadAPIKey, mchannelListNO2, mchannelListNearestCameras);

                for (int i=0; i<channelData.getChannelListName().size();i++){
                    Log.d("ChannelDebugReadingID", channelData.getChannelID(i));
                    Log.d("ChannelDebugReadingName", channelData.getChannelName(i));
                    Log.d("ChannelDebugReadingLat", channelData.getChannelLat(i));
                    Log.d("ChannelDebugReadingLon", channelData.getChannelLon(i));
                    Log.d("ChannelDebugReadingAPIKey", channelData.getChannelReadAPIKey(i));
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String s) {
            //Executed on UI thread
            //Initialize some arrays of channelData
            for(int a=0; a<channelData.getChannelListName().size(); a++){
                String valueNull = "-999";
                channelData.addChannelListNO2OnPosition(valueNull, a);
                Log.d("ChannelDebugReadingNO2", ""+channelData.getChannelNO2(a));
            }
            //Add to the channel data the information about the nearest camera to each one.
            addTheNearestCamera(cameraData, channelData);
            for(int b = 0; b < channelData.getChannelListNearestCameras().size(); b++) {
                Log.d("addTheNearestCamera", "" + channelData.getChannelNearestCameras(b));
            }
            //Subscribe to the channels
            SubscribeToTopicsTask subscribeToTopicTask = new SubscribeToTopicsTask();
            subscribeToTopicTask.execute();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////// CLASS SUBSCRIBE TO TOPIC TASK /////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class SubscribeToTopicsTask extends AsyncTask<String, Void, String>{

        MqttAndroidClient mqttAndroidClient;

        final String serverUri = "tcp://mqtt.thingspeak.com:1883";
        String clientId = "Cristina";

        @Override
        protected String doInBackground(String... strings){
            String response = "";
            //Create and configure and MQTT Client
            startSubscription();
            return response;
        }

        protected void onPostExecute(String s) {
            //Executed on UI thread
        }

        public void startSubscription(){
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

            MqttCallbackExtended mqttCallbackExtended = new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Toast.makeText(getApplicationContext(), "Reconnected to : " + serverURI, Toast.LENGTH_SHORT).show();
                        //Log.d("Debug", "Reconnected to : " + serverURI);
                        // Because Clean Session is true, we need to re-subscribe
                        subscribeToTopics(channelData);
                    } else {
                        Toast.makeText(getApplicationContext(),"Connected to: " + serverURI, Toast.LENGTH_SHORT).show();
                        //Log.d("Debug", "Connected to: " + serverURI);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(getApplicationContext(), "The Connection was lost.", Toast.LENGTH_SHORT).show();
                    //Log.d("Debug", "The Connection was lost.");
                    //Here the mobile should try to connect again.
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                    byte[] msg = mqttMessage.getPayload();
                    String message = new String(msg);
                    int messageInt = Integer.parseInt(message);

                    Toast.makeText(getApplicationContext(), "Incoming message: " + message+" from "+ topic, Toast.LENGTH_LONG).show();

                    //We have to save this message in the same row or colum that are all the information about the channel
                    //this message has been sent from
                    String channelID = topic.substring(9,15);
                    Log.d("Debug", channelID);

                    for(int i=0; i<channelData.getChannelSize(); i++){
                        Log.d("Go trough channelData", ""+channelData.getChannelID(i));
                        if(channelID.equals(channelData.getChannelID(i))){
                            Log.d("Go trough channelData", "In the if");

                            //ADD THE PAYLOAD TO CHANNEL DATA
                            //This solution doesnt save the previous values
                            channelData.addChannelListNO2OnPosition(message, i);
                            for(int k = 0; k<channelData.getChannelListNO2().length; k++){
                                  Log.d("Index", Integer.toString(k));
                                Log.d("Channel list NO2", channelData.getChannelNO2(k));
                             }

                            //ADD THE PAYLOAD TO CAMERA DATA
                            //Search in channel data the asociated camera
                            int camPosition = channelData.getChannelNearestCameras(i);
                            Log.d("CameraPosition", ""+Integer.toString(camPosition));
                            //Save the information in camera data in the correct position
                            //This solution doesnt save the previous values
                            cameraData.addCameraListNO2OnPosition(message, camPosition);
                            Log.d("messageArrived", ""+cameraData.getNO2OnPosition(camPosition));

                            //ALARM FLAG
                            //This solution doesnt save the previous values
                            if(messageInt>=100){
                                cameraData.addCameraListAlarmFlagOnPosition(1, camPosition);
                                for(int j=0;j<cameraData.getCameraListAlarmFlag().length;j++){
                                    Log.d("Alarm Flag", Integer.toString(cameraData.getAlarmFlagOnPosition(j)));
                                    Log.d("Index of alarm flag", ""+j);
                                }
                            }else{
                                cameraData.addCameraListAlarmFlagOnPosition(0, camPosition);
                            }

                            //NUMBER OF EMERGENCIES.
                            for(int c=0; c<cameraData.getCameraListAlarmFlag().length; c++){
                                int alarm = cameraData.getAlarmFlagOnPosition(c);
                                if(alarm == 1){
                                    //emergencyCounter++;
                                    allEmergencies.add(1);
                                }
                            }
                            tvEmergencies.setText("Number of emergencies: "+Integer.toString(allEmergencies.size()));
                            //Reset allEmergencies ArrayList
                            allEmergencies.clear();

                            //COLOR THE BACKGROUND OF THE CAMERA NAME IN THE LISTVIEW IN RED
                            //OR REMOVE THE COLOR
                            for(int d = 0; d<cameraData.getCameraListAlarmFlag().length; d++){
                                String passToString = Integer.toString(cameraData.getAlarmFlagOnPosition(d));
                                Log.d("AdapterAdd", ""+cameraData.getCameraListAlarmFlag().length);
                                Log.d("AdapterAdd", ""+cameraData.getAlarmFlagOnPosition(d));
                            }
                            //adapter.obtainData(cameraData);
                            //adapter.notifyDataSetChanged();

                        }
                    }
                    //Log.d("Incoming message: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            };
            mqttAndroidClient.setCallback(mqttCallbackExtended);

            //MQTT connect options
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            //mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setCleanSession(true);

            mqttConnectOptions.setUserName("Cristina");  //username (MQTTBox)
            mqttConnectOptions.setPassword(MQTTAPIKey.toCharArray());  //MQTT API Key (ThingSpeak)

            try {
                //addToHistory("Connecting to " + serverUri);
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        //mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopics(channelData);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Toast.makeText(getApplicationContext(), "Failed to connect to: " + serverUri, Toast.LENGTH_SHORT).show();
                        //Log.d("Debug", "Failed to connect to: " + serverUri);
                    }
                });


            } catch (MqttException ex){
                ex.printStackTrace();
            }

        }
        public void subscribeToTopics(ChannelData cd){

            IMqttActionListener iMqttActionListener = new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    //Log.d("Debug", "Subscribed!");
                    Toast.makeText(getApplicationContext(), "Subscribed!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    //Log.d("Debug", "Failed to subscribe");
                    Toast.makeText(getApplicationContext(), "Failed to subscribe", Toast.LENGTH_SHORT).show();
                }
            };

            for (int i=0; i<cd.getChannelListName().size(); i++){
                String topic_to_subscribe = "channels/"+cd.getChannelID(i)+"/subscribe/fields/field1/"+cd.getChannelReadAPIKey(i);

                try {
                    //Try to subscribe to all topics, that in this case are the channels
                    mqttAndroidClient.subscribe(topic_to_subscribe, 0, getApplicationContext(), iMqttActionListener);

                }catch (MqttException ex){
                    System.err.println("Exception whilst subscribing");
                    ex.printStackTrace();
                }
            }
        }
    }
}
