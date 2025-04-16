package ru.itmo.practicemanager.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Component;
import ru.itmo.practicemanager.entity.CheckStatus;
import ru.itmo.practicemanager.entity.PracticeType;

@Component
public class CompanyChecker {

    /*private final ActivityRepository activityRepository;
    private final List<String> activityCodes = activityRepository.findAll().stream()
            .map(ActivityCode::getCode).toList();*/

    private final List<String> activityCodes = List.of(new String[]{"62.01", "62.02", "62.03", "62.09", "63.11", "63.12"});

    public CheckStatus checkCompany(String inn, PracticeType practiceType, String name) {
        try {
            String urlString = "https://egrul.itsoft.ru/" + inn + ".json";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();

            if (responseCode == 404) {
                return CheckStatus.COMPANY_NOT_FOUND;
            }
            if (responseCode != 200) {
                return CheckStatus.API_ERROR;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                conn.disconnect();

                JSONObject json;
                try {
                    json = new JSONObject(content.toString());
                } catch (JSONException e) {
                    return CheckStatus.JSON_PARSING_ERROR;
                }

                if (json.has("СвЮЛ")) {
                    JSONObject svYul = json.getJSONObject("СвЮЛ");

                    // Проверка имени компании
                    if (!hasValidName(svYul, name)) {
                        return CheckStatus.INVALID_COMPANY_NAME;
                    }

                    if (!hasSuitableActivity(svYul)) {
                        return CheckStatus.ACTIVITY_NOT_SUITABLE;
                    }

                    if (practiceType == PracticeType.OFFLINE && !isFromSPB(svYul)) {
                        return CheckStatus.LOCATION_NOT_SUITABLE;
                    }

                    return CheckStatus.OK;
                }
                else if (json.has("address") && json.has("okved") && json.has("name")) {
                    // Проверка имени компании
                    String companyName = json.getString("name");
                    if (name == null || !name.trim().equalsIgnoreCase(companyName.trim())) {
                        return CheckStatus.INVALID_COMPANY_NAME;
                    }

                    JSONObject address = json.getJSONObject("address");
                    JSONArray okvedArray = json.getJSONArray("okved");

                    boolean suitableActivity = false;
                    for (int i = 0; i < okvedArray.length(); i++) {
                        JSONObject okved = okvedArray.getJSONObject(i);
                        if (okved.has("code") && !okved.isNull("code") &&
                                activityCodes.contains(okved.getString("code"))) {
                            suitableActivity = true;
                            break;
                        }
                    }
                    if (!suitableActivity) {
                        return CheckStatus.ACTIVITY_NOT_SUITABLE;
                    }

                    if (practiceType == PracticeType.OFFLINE &&
                            (!address.has("regionCode") || !"78".equals(address.getString("regionCode")))) {
                        return CheckStatus.LOCATION_NOT_SUITABLE;
                    }

                    return CheckStatus.OK;
                }
                else {
                    return CheckStatus.COMPANY_NOT_FOUND;
                }
            }
        } catch (Exception e) {
            return CheckStatus.API_ERROR;
        }
    }

    private boolean hasValidName(JSONObject svYul, String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Проверка ЕГРЮЛ-формата
        if (svYul.has("СвНаимЮЛ") && !svYul.isNull("СвНаимЮЛ")) {
            JSONObject svNaimYul = svYul.getJSONObject("СвНаимЮЛ");
            // Проверка полного названия
            if (svNaimYul.has("@attributes") && !svNaimYul.isNull("@attributes")) {
                JSONObject attrs = svNaimYul.getJSONObject("@attributes");
                if (attrs.has("НаимЮЛПолн") && !attrs.isNull("НаимЮЛПолн")) {
                    String fullName = attrs.getString("НаимЮЛПолн");
                    if (name.trim().equals(fullName.replaceAll("[\\s]{2,}", " ").trim())) {
                        return true;
                    }
                }
            }
            // Проверка сокращённого названия
            if (svNaimYul.has("СвНаимЮЛСокр") && !svNaimYul.isNull("СвНаимЮЛСокр")) {
                JSONObject svNaimYulSokr = svNaimYul.getJSONObject("СвНаимЮЛСокр");
                if (svNaimYulSokr.has("@attributes") && !svNaimYulSokr.isNull("@attributes")) {
                    JSONObject attrs = svNaimYulSokr.getJSONObject("@attributes");
                    if (attrs.has("НаимСокр") && !attrs.isNull("НаимСокр")) {
                        String shortName = attrs.getString("НаимСокр");
                        if (name.trim().equals(shortName.replaceAll("[\\s]{2,}", " ").trim())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSuitableActivity(JSONObject svYul) {
        if (svYul.has("СвОКВЭД") && !svYul.isNull("СвОКВЭД")) {
            JSONObject svOKVED = svYul.getJSONObject("СвОКВЭД");

            if (svOKVED.has("СвОКВЭДОсн") && !svOKVED.isNull("СвОКВЭДОсн")) {
                JSONObject svOKVEDOsn = svOKVED.getJSONObject("СвОКВЭДОсн");
                if (svOKVEDOsn.has("@attributes") && !svOKVEDOsn.isNull("@attributes")) {
                    JSONObject attrs = svOKVEDOsn.getJSONObject("@attributes");
                    if (attrs.has("КодОКВЭД") && !attrs.isNull("КодОКВЭД") &&
                            activityCodes.contains(attrs.getString("КодОКВЭД"))) {
                        return true;
                    }
                }
            }

            if (svOKVED.has("СвОКВЭДДоп") && !svOKVED.isNull("СвОКВЭДДоп")) {
                Object svOKVEDDopObj = svOKVED.get("СвОКВЭДДоп");
                JSONArray svOKVEDDop = svOKVEDDopObj instanceof JSONArray
                        ? (JSONArray) svOKVEDDopObj
                        : new JSONArray().put(svOKVEDDopObj);
                for (int i = 0; i < svOKVEDDop.length(); i++) {
                    JSONObject dop = svOKVEDDop.getJSONObject(i);
                    if (dop.has("@attributes") && !dop.isNull("@attributes")) {
                        JSONObject attrs = dop.getJSONObject("@attributes");
                        if (attrs.has("КодОКВЭД") && !attrs.isNull("КодОКВЭД") &&
                                activityCodes.contains(attrs.getString("КодОКВЭД"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isFromSPB(JSONObject svYul) {
        if (svYul.has("СвАдресЮЛ") && !svYul.isNull("СвАдресЮЛ")) {
            JSONObject svAdresYul = svYul.getJSONObject("СвАдресЮЛ");

            // Проверка через АдресРФ
            if (svAdresYul.has("АдресРФ") && !svAdresYul.isNull("АдресРФ")) {
                JSONObject adresRF = svAdresYul.getJSONObject("АдресРФ");
                if (adresRF.has("@attributes") && !adresRF.isNull("@attributes")) {
                    JSONObject attrs = adresRF.getJSONObject("@attributes");
                    if (attrs.has("КодРегион") && !attrs.isNull("КодРегион") &&
                            "78".equals(attrs.getString("КодРегион"))) {
                        return true;
                    }
                }
            }

            // Проверка через СвМНЮЛ
            if (svAdresYul.has("СвМНЮЛ") && !svAdresYul.isNull("СвМНЮЛ")) {
                JSONObject svMNYUL = svAdresYul.getJSONObject("СвМНЮЛ");
                if (svMNYUL.has("Регион") && !svMNYUL.isNull("Регион") &&
                        "78".equals(svMNYUL.getString("Регион"))) {
                    return true;
                }
            }
        }
        return false;
    }
}