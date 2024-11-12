package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpStatus;
import org.example.pojo.Plurk;
import org.example.pojo.Response;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MyCrawler {

    private static final String PLURK_ARTICLE_URL = "https://www.plurk.com/Stats/getAnonymousPlurks?lang=zh";
    private static final String PLURK_RESPONSE_URL = "https://www.plurk.com/Responses/get";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        List<Plurk> plurks = null;
        try {
            //建立httpClient
            HttpClient httpClient = HttpClient.newHttpClient();

            //創建request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PLURK_ARTICLE_URL))
                    .version(HttpClient.Version.HTTP_2)
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            //獲取response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpStatus.SC_OK) {

                String json = response.body();
                plurks = getPlurks(json, httpClient);

            } else {
                throw new Exception("錯誤狀態碼: " + response.statusCode());
            }

            //將plurks轉換成html檔並按日期存成檔案
            HtmlConverter.saveHtmlToFile(plurks);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static List<Plurk> getPlurks(String json, HttpClient httpClient) throws Exception {
        JsonNode rootNode = mapper.readTree(json);
        JsonNode pids = rootNode.path("pids");

        List<Plurk> plurks = new ArrayList<>();
        for (JsonNode pid : pids) {
            String pidStr = pid.asText();
            JsonNode node = rootNode.path(pidStr);
            Plurk plurk = mapper.readValue(node.toString(), Plurk.class);

            //get response
            if (plurk.getResponse_count() > 0) {
                List<Response> responses = getResponses(pidStr, httpClient);
                plurk.setResponses(responses);
            }

            plurks.add(plurk);
        }

        return plurks;
    }

    private static List<Response> getResponses(String pid, HttpClient httpClient) {
        String plurkId = URLEncoder.encode(pid, StandardCharsets.UTF_8);
        String fromResponseid = URLEncoder.encode("0", StandardCharsets.UTF_8);
        String requestParam = "plurk_id=" + plurkId + "&from_response_id=" + fromResponseid;

        List<Response> responses = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PLURK_RESPONSE_URL))
                    .version(HttpClient.Version.HTTP_2)
                    .POST(HttpRequest.BodyPublishers.ofString(requestParam))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpStatus.SC_OK) {

                String json = response.body();
                JsonNode rootNode = mapper.readTree(json);
                JsonNode responseNode = rootNode.path("responses");
                for (JsonNode node : responseNode) {
                    Response res = mapper.readValue(node.toString(), Response.class);
                    responses.add(res);
                }

            } else {
                throw new Exception("錯誤狀態碼: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responses;
    }
}
