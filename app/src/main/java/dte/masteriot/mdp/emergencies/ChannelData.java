package dte.masteriot.mdp.emergencies;

import java.util.ArrayList;

/**
 * ChannelData class.
 * In this class, we connect to the web to obtain the channel list in json format from ThingSpeak.
 * Then, we save the information of each channel in different ArrayLists.
 */

public class ChannelData {

    private ArrayList<String> channelListID;
    private ArrayList<String> channelListName;
    private ArrayList<String> channelListLat;
    private ArrayList<String> channelListLon;
    private ArrayList<String> channelListReadAPIKey;
    //private ArrayList<String> channelListNO2;
    private String[] channelListNO2;
    private ArrayList<Integer> channelListNearestCameras;
    //private ArrayList<String> channelListNearestCameras;

    public ChannelData (ArrayList _channelListID, ArrayList _channelListName, ArrayList _channelListLat, ArrayList _channelListLon, ArrayList _channelListReadAPIKey, String[] _channelListNO2, ArrayList _channelListNearestCameras){
        this.channelListID = _channelListID;
        this.channelListName = _channelListName;
        this.channelListLat = _channelListLat;
        this.channelListLon = _channelListLon;
        this.channelListReadAPIKey = _channelListReadAPIKey;
        this.channelListNO2 = _channelListNO2;
        this.channelListNearestCameras = _channelListNearestCameras;
    }

    //GETTERS

    public int getChannelSize(){
        return this.channelListName.size();
    }

    public ArrayList<String> getChannelListID() {
        return this.channelListID;
    }

    public ArrayList<String> getChannelListName() {
        return this.channelListName;
    }

    public ArrayList<String> getChannelListLat() {
        return this.channelListLat;
    }

    public ArrayList<String> getChannelListLon() {
        return this.channelListLon;
    }

    /*
    public ArrayList<String> getChannelListNO2() {
        return this.channelListNO2;
    }

     */
    public String[] getChannelListNO2(){
        return this.channelListNO2;
    }

    public ArrayList<String> getChannelListReadAPIKey() {
        return this.channelListReadAPIKey;
    }

    public ArrayList<Integer> getChannelListNearestCameras(){
        return this.channelListNearestCameras;
    }

    public String getChannelID(int position){
        return this.channelListID.get(position);
    }

    public String getChannelName(int position){
        return this.channelListName.get(position);
    }

    public String getChannelLat(int position){
        return this.channelListLat.get(position);
    }

    public String getChannelLon(int position){
        return this.channelListLon.get(position);
    }

    public String getChannelNO2(int position){
        //return this.channelListNO2.get(position);
        return this.channelListNO2[position];
    }


    public String getChannelReadAPIKey(int position){
        return this.channelListReadAPIKey.get(position);
    }

    public int getChannelNearestCameras(int position){
        return this.channelListNearestCameras.get(position);
    }

    //SETTERS

    public void addChannelListID(String s) {
        this.channelListID.add(s);
    }

    public void addChannelListName(String s) {
        this.channelListName.add(s);
    }

    public void addChannelListLat(String s) {
        this.channelListLat.add(s);
    }

    public void addChannelListLon(String s) {
        this.channelListLon.add(s);
    }

    public void addChannelListReadAPIKey(String s) {
        this.channelListReadAPIKey.add(s);
    }

    /*
    public void addChannelListNO2(String s) {
        this.channelListNO2.add(s);
    }

     */

    public void addChannelListNearestCamera(int value){
        this.channelListNearestCameras.add(value);
    }

    public void addChannelListNO2OnPosition(String value, int position){
        //Aqui puede ocurrir que al borrar el dato de una posicion, esa
        //posicion desaparezca. Entonces las numeraciones se adelantan una posicion
        //y puede ser que este machacando la informacion.
        //this.channelListNO2.remove(position);
        //this.channelListNO2.add(position, s);
        //////////////////////////////////////////////////////////////////
        //this.channelListNO2.set(position, s);
        //this.channelListNO2.set(position-1, s);
        ///////////////////////////////////////////////////////////////
        this.channelListNO2[position] = value;
    }

    public void addChannelListNearestCameraOnPosition(int value, int position){
        this.channelListNearestCameras.set(position, value);
        //this.channelListNearestCameras.set(position-1, s);
    }
}
