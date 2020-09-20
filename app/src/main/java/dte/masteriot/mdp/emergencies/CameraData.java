package dte.masteriot.mdp.emergencies;

import java.util.ArrayList;

public class CameraData {

    private ArrayList<String> cameraListName;
    private ArrayList<String> cameraListURLS;
    private ArrayList<String> cameraListCoordinates;
    //private ArrayList<String> cameraListNO2;
    private String [] cameraListNO2;
    //private ArrayList<String> cameraListChannels;
    //private ArrayList<Integer> cameraListAlarmFlag;
    private int[] cameraListAlarmFlag;

    //Constructor with parameters
    public CameraData(ArrayList _cameraListName, ArrayList _cameraListURLS, ArrayList _cameraListCoordinates, String[] _cameraListNO2, int[] _cameraListAlarmFlag){
        this.cameraListName = _cameraListName;
        this.cameraListURLS = _cameraListURLS;
        this.cameraListCoordinates = _cameraListCoordinates;
        this.cameraListNO2 = _cameraListNO2;
        //this.cameraListChannels = _cameraListChannels;
        this.cameraListAlarmFlag = _cameraListAlarmFlag;
    }

    //Constructor without parameters
    public CameraData(){
        this.cameraListCoordinates = null;
        this.cameraListURLS = null;
        this.cameraListName = null;
        this.cameraListNO2 = null;
        //this.cameraListChannels = null;
        this.cameraListAlarmFlag = null;
    }

    //Constructor with an object
    public CameraData(CameraData cd){
        this.cameraListCoordinates = cd.cameraListCoordinates;
        this.cameraListURLS = cd.cameraListURLS;
        this.cameraListName = cd.cameraListName;
        this.cameraListNO2 = cd.cameraListNO2;
        //this.cameraListChannels = cd.cameraListChannels;
        this.cameraListAlarmFlag = cd.cameraListAlarmFlag;
    }

    //GETTERS
    public ArrayList<String> getCameraListCoordinates() {
        return this.cameraListCoordinates;
    }

    public ArrayList<String> getCameraListName() {
        return this.cameraListName;
    }

    public ArrayList<String> getCameraListURLS() {
        return this.cameraListURLS;
    }

    /*
    public ArrayList<String> getCameraListNO2() {
        return this.cameraListNO2;
    }

     */

    public String[] getCameraListNO2() {
        return this.cameraListNO2;
    }
    /*
    public ArrayList<String> getCameraListChannels() {
        return this.cameraListChannels;
    }
    public ArrayList<Integer> getCameraListAlarmFlag(){
        return this.cameraListAlarmFlag;
    }

     */

    public int[] getCameraListAlarmFlag() {
        return this.cameraListAlarmFlag;
    }

    public String getNameOnPosition(int position){
        return this.cameraListName.get(position);
    }

    public String getCoordinatesOnPosition(int position){
        return this.cameraListCoordinates.get(position);
    }

    public String getURLSOnPosition(int position){
        return this.cameraListURLS.get(position);
    }

    public String getNO2OnPosition(int position){
        //return this.cameraListNO2.get(position);
        return this.cameraListNO2[position];
    }

    /*
    public String getChannelOnPosition(int position){
        return this.cameraListChannels.get(position);
    }
    public int getAlarmFlagOnPosition(int position){
        return this.cameraListAlarmFlag.get(position);
    }

     */
    public int getAlarmFlagOnPosition(int position){
        return this.cameraListAlarmFlag[position];
    }

    //SETTERS
    public void addCameraListName(String n){
        this.cameraListName.add(n);
    }

    public void addCameraListURLS(String URL){
        this.cameraListURLS.add(URL);
    }
    public void addCameraListCoordinates(String coord){
        this.cameraListCoordinates.add(coord);
    }

    public void addCameraListNO2 (String no2){
        //this.cameraListNO2.add(no2);

    }

    /*
    public void addCameraListChannel(String ch){
        this.cameraListChannels.add(ch);
    }
    public void addCameraListAlarmFlag(int value){
        this.cameraListAlarmFlag.add(value);
    }
    public void addCameraListChannelOnPosition(String ch, int pos){
        this.cameraListChannels.set(pos, ch);
    }
     */

    public void addCameraListNO2OnPosition(String NO2,int pos){
        //this.cameraListNO2.set(pos, NO2);
        this.cameraListNO2[pos] = NO2;
    }

    public void addCameraListAlarmFlagOnPosition(int value, int pos){
        //this.cameraListAlarmFlag.set(pos,value);
        this.cameraListAlarmFlag[pos] = value;
    }

    public int obtainThePositionOfAName(String name){

        int result = -1;

        for(int i = 0; i<this.getCameraListName().size();i++){
            if(name.equals(this.getNameOnPosition(i))){
                result = i;
            }
        }
        return result;
    }

}
