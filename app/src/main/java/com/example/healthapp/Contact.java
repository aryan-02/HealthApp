package com.example.healthapp;

public class Contact {

    private String name;
    private String phoneNumber;
    private String email;

    private String databaseID;

    private String type;

    public Contact(String name, String phoneNumber, String email, String type)
    {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }
    public void setDatabaseID(String databaseID)
    {
        this.databaseID = databaseID;
    }

    public static boolean ValidatePhone(String phone)
    {
        return phone.matches("[0-9]{10}");
    }
    public static boolean ValidateEmail(String email)
    {
        //regex from: https://www.baeldung.com/java-email-validation-regex
        return email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    }

    public String getFormattedPhoneNumber()
    {
        return "(" + phoneNumber.substring(0, 3) + ") " + phoneNumber.substring(3, 6) + "-" + phoneNumber.substring(6);
    }

}
