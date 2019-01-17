import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class DownloadPdf {
    private static String domain = "http://digital.dtxww.com";
    private static String urlPrefix = "http://digital.dtxww.com/Media/dtrb/";
    private List<String> urls;

    public List<String> getUrls(){
        return urls;
    }

    public DownloadPdf(){
        this.urls = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2018);
        for(int m=5;m<12;m++) {
            calendar.set(Calendar.MONTH, m);
            for (int i = 1; i <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                calendar.set(Calendar.DAY_OF_MONTH, i);
                String day = dateFormat.format(calendar.getTime());
                this.urls.add(urlPrefix+day);
            }
        }
    }


    public static void main(String[] args) {
        DownloadPdf downloadPdf = new DownloadPdf();
        for(String url : downloadPdf.getUrls()) {
            System.out.println(url);
            downloadPdf.download(url);
        }
    }


    public void download(String url){
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(5000)).followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(5009)).build();
        String[] infos = url.split("/");
        String name = infos[infos.length-1];
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Document document = Jsoup.parse(response.body());
            Elements indexLis = document.select("li.item2");
            if(indexLis.size()<=0){
                // 没有相关版面
                return;
            }
            Iterator iterator = indexLis.iterator();
            int i = 1;
            while (iterator.hasNext()){
                Element current = (Element)iterator.next();
                String title = current.text();
                if (title.contains("广告")) {
                    // download target pdf begins
                    String suburl = current.select("a").attr("href");
                    HttpResponse<String> targetPage = client.send((HttpRequest) HttpRequest.newBuilder().uri(URI.create(domain + suburl)).build(), HttpResponse.BodyHandlers.ofString());
                    Document subDoc = Jsoup.parse(targetPage.body());
                    String href = subDoc.select("#pdf_toolbar>a").attr("href");
                    client.send(HttpRequest.newBuilder().uri(URI.create(href)).build(), HttpResponse.BodyHandlers.ofFile(Paths.get(name + "-" + i +".pdf")));
                    // download ends
                }
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
