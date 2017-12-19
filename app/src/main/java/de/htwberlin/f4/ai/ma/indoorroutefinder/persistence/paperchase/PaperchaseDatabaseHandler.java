package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.paperchase;

import java.util.ArrayList;


import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Clue;
import de.htwberlin.f4.ai.ma.indoorroutefinder.paperchase.models.Paperchase;

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

    void updatePaperchase(Paperchase paperchase,String oldPaperchaseName);




}
