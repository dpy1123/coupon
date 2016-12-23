package top.devgo.coupon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import top.devgo.coupon.utils.IOUtil;
import top.devgo.coupon.utils.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by dd on 16/12/20.
 */
public class PhoneTest {

    static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36";
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        List<String> records = IOUtil.readFile2List("/Users/dd/Downloads/tb_employee.csv","utf-8");
        List<String> results = new LinkedList<>();
        records.parallelStream().forEach(record ->  {
            String phone = record.split(",")[2].replace("\"", "").trim();
            try {
                String loc = getLocalHtml(phone);
                System.out.println(loc);
                if (loc!=null && !loc.equals("null")) {
                    results.add(record+= "," + loc);
                }
                else
                    System.err.println("null: phone-"+phone);
            }catch (Exception e){
                e.printStackTrace();
                System.err.println("err: phone-"+phone);
            }
        });
        IOUtil.writeList(results, "/Users/dd/Downloads/phones3.txt","utf-8");
    }

    public static String getLocalJson(String phone) throws IOException {
        Response resp = Jsoup.connect("http://opendata.baidu.com/api.php").ignoreContentType(true)
                .method(Method.GET)
                .header("User-Agent", USER_AGENT)
                .data("query", phone)
                .data("resource_name", "guishudi")
                .execute();

        Map<String, Object> result = mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() { } );
        if ("0".equals(result.get("status"))) {
            Map<String, Object> data = (Map<String, Object>) ((List) result.get("data")).get(0);
            String prov = (String) data.get("prov");
            String city = (String) data.get("city");
            String type = (String) data.get("type");
            return prov==null?city:prov +","+type;
        }
        return null;
    }
    public static String getLocalHtml(String phone) throws IOException {
        Response resp = Jsoup.connect("https://www.baidu.com/s").ignoreContentType(true)
                .method(Method.GET)
                .header("User-Agent", USER_AGENT)
                .data("wd", phone)
                .execute();

        Document doc = Jsoup.parse(resp.body());
        Element div = doc.select("div.c-border > div > div.c-span21.c-span-last > div.op_mobilephone_r.c-gap-bottom-small").first();
        if (div!=null) {
            String text = div.child(1).html();
            String prov = text.split("&nbsp;")[0];
            String city = text.split("&nbsp;")[1];
            String type = text.split("&nbsp;")[3];
            return (StringUtil.isBlank(prov)?city:prov) +","+type;
        }
        return null;
    }
}
