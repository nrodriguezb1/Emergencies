package dte.masteriot.mdp.emergencies;

public class CameraListView {
    private String cameraName;
    private int alarmFlag;

    public CameraListView(String cn, int value){
        this.cameraName = cn;
        this.alarmFlag = value;
    }

    //GETTERS

    public int getAlarmFlag() {
        return this.alarmFlag;
    }

    public String getCameraName() {
        return this.cameraName;
    }

    //SETTERS

    public void setAlarmFlag(int alarmFlag) {
        this.alarmFlag = alarmFlag;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }
}
