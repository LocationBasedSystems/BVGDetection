package de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models;


import android.support.annotation.NonNull;

import java.io.Serializable;

import de.htwberlin.f4.ai.ma.indoorroutefinder.node.Node;

/**
 * Created by Yannik on 21.11.2017.
 */

public class Clue implements Comparable<Clue>, Serializable{
    private String clueText;
    private int idx;
    private Node loc;
    private String hintPicturePath = null;



    public Clue(Node loc) {
        this.loc = loc;
    }

    public Clue(String clueText) {
        this.clueText = clueText;

    }

    public Clue(String clueText, Node loc) {
        this.clueText = clueText;
        this.loc = loc;
    }

    public Clue(String clueText, int idx, Node loc) {
        this.clueText = clueText;
        this.idx = idx;
        this.loc = loc;
    }

    public Clue() {
    }


    public String getHintPicturePath() {
        return hintPicturePath;
    }

    public void setHintPicturePath(String hintPicturePath) {
        this.hintPicturePath = hintPicturePath;
    }

    public void setClueText(String clueText) {
        this.clueText = clueText;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setLoc(Node loc) {
        this.loc = loc;
    }

    public String getClueText() {
        return clueText;
    }

    public int getIdx() {
        return idx;
    }

    public Node getLoc() {
        return loc;
    }
    
    @Override
    public int compareTo(@NonNull Clue clue) {
        if (idx > clue.getIdx()) {
            return -1;
        } else if (idx < clue.getIdx()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (loc.getDescription().length() <30 ){
            return idx + "  " + loc.getDescription();
        }
        return idx + "  " + loc.getDescription().substring(0,30);
    }
}
