import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private final HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .build();

    public static void main(String[] args) throws Exception {
        Main obj = new Main();
        for (int rollNo = 1; rollNo <= 99; rollNo++) {
            obj.sendPost("18D21A05" + String.format("%02d", rollNo));
        }
        for (char c = 'A'; c <= 'N'; c++) {
            for (int i = 0; i <= ((c == 'N')? 7: 9); i++) {
                obj.sendPost("18D21A05" + c + String.format("%d", i));
            }
        }
    }

    private void sendPost(String rollNo) throws Exception {
        while(true) {
            boolean tryagain = false;
            try {
                Map<Object, Object> data = new HashMap<>();
                data.put("degree", "btech");
                data.put("htno", rollNo);
                data.put("examCode", "1391");
                data.put("etype", "r17");
                data.put("type", "grade17");
                data.put("grad", "null");
                data.put("result", "null");

                // http://epayments.jntuh.ac.in/results/resultAction
                // http://202.63.105.184/results/resultAction
                HttpRequest request = HttpRequest.newBuilder()
                    .POST(buildFormDataFromMap(data))
                    .uri(URI.create("http://202.63.105.184/results/resultAction"))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    tryagain = true;
                    continue;
                }
                Document doc = Jsoup.parse(response.body());
                Elements tables = doc.getElementsByAttributeValue("width", "80%");
                File file = new File("results.html");
                FileWriter fr = new FileWriter(file, true);
                fr.write(tables.get(0).toString() + "<br/>" + tables.get(1).toString() + "<br/><br/>");
                fr.close();
                System.out.println(rollNo + " Done.");
            }
            catch (IndexOutOfBoundsException e) {
                System.out.println(rollNo + " Dropped.");
                tryagain = false;
                break;
            }
            catch(Exception e) {
                tryagain = true;
                System.out.println("Retrying " + rollNo + " becoz " + e.toString());
            }
            if (!tryagain)
                break;
        }
    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
//        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}