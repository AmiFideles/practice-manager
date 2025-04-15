package ru.itmo.practicemanager.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Component;
import ru.itmo.practicemanager.entity.CheckResult;
import ru.itmo.practicemanager.entity.PracticeType;

@Component
public class CompanyChecker {
    public CheckResult checkCompany(String inn, PracticeType practiceType) {
        try {
            String urlString = "https://egrul.itsoft.ru/" + inn + ".json";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int responseCode = conn.getResponseCode();

            if (responseCode == 404) {
                return CheckResult.COMPANY_NOT_FOUND;
            }
            if (responseCode != 200) {
                return CheckResult.API_ERROR;
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
                    return CheckResult.JSON_PARSING_ERROR;
                }

                if (json.has("СвЮЛ")) {
                    JSONObject svYul = json.getJSONObject("СвЮЛ");

                    if (!hasActivity(svYul, "62.01")) {
                        return CheckResult.ACTIVITY_NOT_SUITABLE;
                    }

                    if (practiceType == PracticeType.OFFLINE && !isFromSPB(svYul)) {
                        return CheckResult.LOCATION_NOT_SUITABLE;
                    }

                    return CheckResult.OK;
                }
                else if (json.has("address") && json.has("okved")) {
                    JSONObject address = json.getJSONObject("address");
                    JSONArray okvedArray = json.getJSONArray("okved");

                    boolean hasSoftwareDevelopment = false;
                    for (int i = 0; i < okvedArray.length(); i++) {
                        JSONObject okved = okvedArray.getJSONObject(i);
                        if (okved.has("code") && !okved.isNull("code") &&
                                "62.01".equals(okved.getString("code"))) {
                            hasSoftwareDevelopment = true;
                            break;
                        }
                    }
                    if (!hasSoftwareDevelopment) {
                        return CheckResult.ACTIVITY_NOT_SUITABLE;
                    }

                    if (practiceType == PracticeType.OFFLINE &&
                            (!address.has("regionCode") || !"78".equals(address.getString("regionCode")))) {
                        return CheckResult.LOCATION_NOT_SUITABLE;
                    }

                    return CheckResult.OK;
                }
                else {
                    return CheckResult.COMPANY_NOT_FOUND;
                }
            }
        } catch (Exception e) {
            return CheckResult.API_ERROR;
        }
    }

    private boolean hasActivity(JSONObject svYul, String code) {
        if (svYul.has("СвОКВЭД") && !svYul.isNull("СвОКВЭД")) {
            JSONObject svOKVED = svYul.getJSONObject("СвОКВЭД");

            if (svOKVED.has("СвОКВЭДОсн") && !svOKVED.isNull("СвОКВЭДОсн")) {
                JSONObject svOKVEDOsn = svOKVED.getJSONObject("СвОКВЭДОсн");
                if (svOKVEDOsn.has("@attributes") && !svOKVEDOsn.isNull("@attributes")) {
                    JSONObject attrs = svOKVEDOsn.getJSONObject("@attributes");
                    if (attrs.has("КодОКВЭД") && !attrs.isNull("КодОКВЭД") &&
                            code.equals(attrs.getString("КодОКВЭД"))) {
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
                                code.equals(attrs.getString("КодОКВЭД"))) {
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