package model;

import lombok.Data;

@Data
public class Activity {
    int oEndTime;
    String modeBefore;
    ActivityType oType;
    ActivityType dType;
    int oZone;
    int dZone;

    public void setModeBefore(String code) {
        switch (code) {
            case "0":
            case "7":
                modeBefore = "car";
                break;
            case "2":
                modeBefore = "walk";
                break;
            default:
                modeBefore = "pt";
                break;
        }
    }

    public void setOType(String type) {
        oType = mapActivity(type);
    }

    public void setDType(String type) {
        dType = mapActivity(type);
    }

    private ActivityType mapActivity(String code) {
        switch (code) {
            case "0":
                return ActivityType.SHOP;
            case "2":
            case "6":
            case "13":
            case "18":
            case "12":
            case "15":
            case "16":
            	return ActivityType.HOME;
            case "7":
            case "8":
            case "17":
            	return ActivityType.WORK;
            case "14":
            case "19":
            	return ActivityType.STUDY_CHILD;
            default:
            	return ActivityType.LEISURE;
        }
    }
}
