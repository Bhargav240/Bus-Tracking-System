package com.example.busmobileapp;

public class ReadWriteUserDetails {
    public String name,email,phnum,route,stop,roll,pass,status;
    public ReadWriteUserDetails(String fname, String femail, String pnum, String froute, String fstop, String froll, String pass2,String fbuspass) {
        this.name = fname;
        this.email = femail;
        this.phnum = pnum;
        this.route = froute;
        this.stop = fstop;
        this.roll = froll;
        this.pass = pass2;
        this.status = fbuspass;
    }

    public ReadWriteUserDetails() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhnum() {
        return phnum;
    }

    public void setPhnum(String phnum) {
        this.phnum = phnum;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
