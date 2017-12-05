package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase;

import java.util.ArrayList;


import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.Clue;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.Paperchase;

/**
 * Created by Yannik on 05.12.2017.
 */

public interface PaperchaseDatabaseHandler {

    //--------Paperchase management------
    void insertPaperchase(Paperchase paperchase);

    Paperchase getPaperchase(String paperchaseName);

    ArrayList<Paperchase> getAllPaperchases();

    boolean checkIfPaperchaseExists(String paperchaseName);

    void deletePaperchase(Paperchase paperchase);

    //--------Clue management-------

    void insertClue(Clue clue, String paperchaseName);

    Clue getClue(int clueId);

    ArrayList<Clue> getAllCluesOfPaperchase(String paperchaseName);

    boolean checkIfClueExists(int clueId);

    void deleteCluesByPaperchase(String paperchaseName);


}
