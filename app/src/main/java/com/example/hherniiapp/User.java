package com.example.hherniiapp;

import android.media.Image;

public class User {
    public String username, name, surname, dni, age, peso, obra_social, user_email, photourl, medico_op, medico_de, institucion, direccion;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String name, String surname, String dni, String obra_social, String peso, String age, String photourl, String user_email, String medico_op, String medico_de, String institucion, String direccion) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.dni = dni;
        this.age = age;
        this.peso = peso;
        this.obra_social = obra_social;
        this.photourl = photourl;
        this.user_email = user_email;
        this.medico_op = medico_op;
        this.medico_de = medico_de;
        this.institucion = institucion;
        this.direccion = direccion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getDNI() {
        return dni;
    }

    public void setDNI(String dni) {
        this.dni = dni;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }
    public String getObra_social() {
        return obra_social;
    }

    public void setObra_social(String obra_social) {
        this.obra_social = obra_social;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getMedico_op() { return medico_op; }
    public void getMedico_op(String medico_op){ this.medico_op = medico_op;}
    public String getMedico_de() { return medico_de; }
    public void getMedico_de(String medico_de){ this.medico_de = medico_de;}
    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }
    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}

