package com.grundfos.pump.replace.model;

public class MetadataInput {
    private String userName;
    private String installDate;
    private String timezone;

    public MetadataInput(){}

    public MetadataInput(String userName, String installDate, String timezone) {
        this.userName = userName;
        this.installDate = installDate;
        this.timezone = timezone;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "MetadataInput{" +
                "userName='" + userName + '\'' +
                ", installDate='" + installDate + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
