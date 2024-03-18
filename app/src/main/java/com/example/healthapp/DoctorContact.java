package com.example.healthapp;

import com.google.type.DateTime;

public class DoctorContact extends Contact{
    private DateTime open;
    private DateTime close;

    public DoctorContact(String name, String phoneNumber, String email, DateTime open, DateTime close)
    {
        super(name, phoneNumber, email, "Doctor Contact");
        this.open = open;
        this.close = close;
    }

    public DateTime getOpen() {
        return open;
    }
    public void setOpen(DateTime open) {
        this.open = open;
    }

    public DateTime getClose() {
        return close;
    }
    public void setClose(DateTime close) {
        this.close = close;
    }
}
