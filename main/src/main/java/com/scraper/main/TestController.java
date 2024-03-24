package com.scraper.main;

import jakarta.websocket.server.PathParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    @GetMapping("/ah-search")
    public AHResults searchProduct(@PathParam("query") String query) {
        String url = "https://www.ah.nl/zoeken?query=" + query;
        List<AHResult> results = new ArrayList<>();

        try {
            // Fetch the HTML content of the webpage
            Document doc = Jsoup.connect(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                    .header("authority", "www.google.com")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "en-US,en;q=0.9")
                    .header("cache-control", "max-age=0")
                    .get();


            // Extract product details
            Element scriptElement = doc.select("script").stream()
                    .filter(element -> element.html().contains("window.__INITIAL_STATE__"))
                    .findFirst()
                    .orElse(null);

            if (scriptElement != null) {

                var scriptContent = scriptElement.toString();
                // Find the index of the '=' sign
                int equalsIndex = scriptContent.indexOf('=');

                if (equalsIndex != -1) {
                    // Extract the substring after '=' sign
                    String jsonData = scriptContent.substring(equalsIndex + 1).trim();

                    // Parse the JSON string
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONObject searchObject = jsonObject.getJSONObject("search");
                    JSONArray resultsArrayObject = searchObject.getJSONArray("results");
                    for (int i = 0; i < resultsArrayObject.length(); i++) {

                        JSONObject itemObject = resultsArrayObject.getJSONObject(i);
                        JSONArray productsArray = itemObject.getJSONArray("products");
                        String title = productsArray.getJSONObject(0).getString("title");
                        JSONObject priceObject = productsArray.getJSONObject(0).getJSONObject("price");
                        Double price = priceObject.getDouble("now");
                        String size = priceObject.getString("unitSize");
                        String link = productsArray.getJSONObject(0).getString("link");

                        results.add(new AHResult(title, link, price.toString(), size));
                    }

                } else {
                    System.out.println("No '=' sign found in the script content.");
                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new AHResults(results, results.size());
    }


}
