package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Yannik on 21.11.2017.
 */

public class Paperchase implements Serializable {
    private String name;
    private String description;
    private ArrayList<Clue> clueList = new ArrayList<>();

    public Paperchase() {
    }

    public Paperchase(String name) {
        this.name = name;
    }

    public Paperchase(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addClue(Clue clue){
        clueList.add(clue);
    }
    public Paperchase(String name, String description, ArrayList<Clue> clueList) {
        this.name = name;
        this.description = description;
        this.clueList = clueList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Clue> getClueList() {
        return clueList;
    }

    public void setClueList(ArrayList<Clue> clueList) {
        this.clueList = clueList;
    }
}
