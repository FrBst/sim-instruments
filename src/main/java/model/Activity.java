package model;

import lombok.Data;

@Data
public class Activity {
    int oEndTime;
    String modeBefore;
    String oType;
    String dType;
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

    private String mapActivity(String code) {
        switch (code) {
            case "0":
                return "shop";
            case "6":
            case "13":
            case "18":
            case "2":
            case "12":
            case "15":
            case "16":
                return "home";
            case "7":
            case "17":
            case "8":
            case "14":
            case "19":
                return "work";
            default:
                return "leisure";
        }
    }
}
