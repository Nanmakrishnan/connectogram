package com.example.connectogram.models;

public class ModelAnnounce {
    String aId,uId,uName,uEmail,aTitle,aDesc,aTime,uDp,aFile;

    public ModelAnnounce() {
    }

    public ModelAnnounce(String aId, String uId, String uName, String uEmail, String aTitle, String aDesc, String aTime, String uDp, String aFile) {
        this.aId = aId;
        this.uId = uId;
        this.uName = uName;
        this.uEmail = uEmail;
        this.aTitle = aTitle;
        this.aDesc = aDesc;
        this.aTime = aTime;
        this.uDp = uDp;
        this.aFile = aFile;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getaTitle() {
        return aTitle;
    }

    public void setaTitle(String aTitle) {
        this.aTitle = aTitle;
    }

    public String getaDesc() {
        return aDesc;
    }

    public void setaDesc(String aDesc) {
        this.aDesc = aDesc;
    }

    public String getaTime() {
        return aTime;
    }

    public void setaTime(String aTime) {
        this.aTime = aTime;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getaFile() {
        return aFile;
    }

    public void setaFile(String aFile) {
        this.aFile = aFile;
    }
}
