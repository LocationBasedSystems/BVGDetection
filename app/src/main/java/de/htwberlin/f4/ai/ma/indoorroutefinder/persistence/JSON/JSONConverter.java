package de.htwberlin.f4.ai.ma.indoorroutefinder.persistence.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.SignalSample;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformation;
import de.htwberlin.f4.ai.ma.indoorroutefinder.fingerprint.accesspoint_information.AccessPointInformationFactory;

/**
 * Created by Johann Winter
 */

public class JSONConverter {

    /**
     * Convert a List<SignalSample> to JSON-String (for database storing).
     * @param signalSampleList a list of SignalSamples
     * @return JSON-String containing the signal data for the database
     */
    public String convertSignalSampleListToJSON(List<SignalSample> signalSampleList) {

        JSONObject jsonObject = new JSONObject();
        JSONArray signalJsonArray = new JSONArray();

        if (signalSampleList != null) {
            try {
                for (int i = 0; i < signalSampleList.size(); i++) {

                    JSONObject signalJsonObject = new JSONObject();
                    JSONArray accessPointInfoArray = new JSONArray();

                    for (int j = 0; j < signalSampleList.get(i).getAccessPointInformationList().size(); j++) {

                        JSONObject accessPointInformation = new JSONObject();
                        accessPointInformation.put("macAddress", signalSampleList.get(i).getAccessPointInformationList().get(j).getMacAddress());
                        accessPointInformation.put("strength", signalSampleList.get(i).getAccessPointInformationList().get(j).getRssi());
                        accessPointInfoArray.put(accessPointInformation);
                    }
                    signalJsonObject.put("timestamp", signalSampleList.get(i).getTimestamp());
                    signalJsonObject.put("accessPointInfoList", accessPointInfoArray);
                    signalJsonArray.put(signalJsonObject);
                }
                jsonObject.put("signalSample", signalJsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }


    /**
     * Convert JSON-String to a List<SignalSample>
     * @param jsonString the JSON-String from the database
     * @return the list of SignalSamples
     */
    public List<SignalSample> convertJsonToSignalSampleList(String jsonString) {

        List<SignalSample> signalSampleList = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            if (jsonObj.has("signalSample")) {
                JSONArray signalSampleArray = jsonObj.getJSONArray("signalSample");

                for (int j = 0; j < signalSampleArray.length(); j++) {

                    JSONObject jsonSignalSample = signalSampleArray.getJSONObject(j);
                    String timestamp = jsonSignalSample.getString("timestamp");

                    JSONArray accessPointInfoArray = jsonSignalSample.getJSONArray("accessPointInfoList");
                    List<AccessPointInformation> accessPointInformations = new ArrayList<>();

                    for (int k = 0; k < accessPointInfoArray.length(); k++) {
                        JSONObject accessPointInfo = accessPointInfoArray.getJSONObject(k);
                        String macAddress = accessPointInfo.getString("macAddress");
                        int signalStrength = accessPointInfo.getInt("strength");
                        AccessPointInformation accessPointInformation = AccessPointInformationFactory.createInstance(macAddress, signalStrength);
                        accessPointInformations.add(accessPointInformation);
                    }
                    SignalSample signalSample = new SignalSample(timestamp, accessPointInformations);
                    signalSampleList.add(signalSample);
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return signalSampleList;
    }
}
