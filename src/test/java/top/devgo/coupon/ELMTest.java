package top.devgo.coupon;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import top.devgo.coupon.utils.IOUtil;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by dd on 16/10/9.
 */
public class ELMTest {

    public static void main(String[] args) throws Exception {
        String folder = "/Users/dd/Downloads/E_data/";

        Map<String, Map<String, String>> eco_envs = buildRecord(folder+"his_eco_env.txt", "list_id");
        Map<String, Map<String, String>> eco_infos = buildRecord(folder+"his_eco_info.txt", "log_id");
//        Map<String, Map<String, String>> order_infos = buildRecord("/Users/dd/Downloads/E_data/his_order_info.txt", "order_id");
        Map<String, Map<String, String>> rst_infos = buildRecord(folder+"rst_info.txt", "restaurant_id");

        List<Map<String, String>> combineRecords = new ArrayList<>();

        for (Entry<String, Map<String, String>> eco_info : eco_infos.entrySet()) {
            String list_id = eco_info.getValue().get("list_id");
            String restaurant_id = eco_info.getValue().get("restaurant_id");
            String order_id = eco_info.getValue().get("order_id");
            Map<String, String> eco_env = eco_envs.get(list_id);
//            Map<String, String> order_info = order_infos.get(order_id);
            Map<String, String> rst_info = rst_infos.get(restaurant_id);

            Map<String, String> combineRecord = new HashMap<>();
            combineRecord.putAll(eco_info.getValue());
            if (eco_env!=null)
            combineRecord.putAll(eco_env);
//            combineRecord.putAll(order_info);
            if (rst_info!=null)
            combineRecord.putAll(rst_info);
            combineRecords.add(combineRecord);
        }


//        FileOutputStream fos = new FileOutputStream("/Users/dd/Downloads/E_data/train_data.csv");
//        String[] headers = combineRecords.get(0).keySet().toArray(new String[]{});
//        fos.write(combine(headers, ",").getBytes());
//        fos.write("\n".getBytes());
//
//        for (int i = 0; i < combineRecords.size(); i++) {
//            fos.write(map2String(headers, combineRecords.get(i), ",").getBytes());
//            fos.write("\n".getBytes());
//        }
//        IOUtil.close(fos);

        Writer outputWriter = new FileWriter(folder+"train_data.csv") ;
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setNullValue("?");
        CsvWriter writer = new CsvWriter(outputWriter, settings);

        writer.writeHeaders(combineRecords.get(0).keySet());
        for (int i = 0; i < combineRecords.size(); i++) {
            writer.writeRow(combineRecords.get(i));
        }
		writer.close();

    }

    private static String map2String(String[] headers, Map<String, String> map, String comb){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            sb.append(map.get(headers[i]));
            if (i < headers.length)
                sb.append(comb);
        }
        return sb.toString();
    }

    /**
     * 将array拼接成完整的str, 用comb连接
     * @param array
     * @param comb
     * @return
     */
    private static String combine(String[] array, String comb){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length)
                sb.append(comb);
        }
        return sb.toString();
    }
    /**
     * build数据集, &lt; id, recordMap &gt;
     * @param file_path
     * @param id 记录中作为主键的attr名
     * @return
     * @throws Exception
     */
    private static Map<String, Map<String, String>> buildRecord(String file_path, String id) throws Exception {
        Map<String, Map<String, String>> records = new HashMap<>();
        List<String> lines = IOUtil.readFile2List(file_path, "UTF-8");
        String[] headers = lines.get(0).split("\t");
        for (int i = 1; i < lines.size(); i++) {//skip header
            Map<String, String> record = new HashMap<>();
            String line = lines.get(i);
            String[] attrs = line.split("\t");
            for (int j = 0; j < headers.length; j++) {
                //如果header或attr本身包含"", 则去掉
                record.put(trim(headers[j]), trim(attrs[j]));
            }
            String key = record.get(id);
            String tmp = record.get("list_id");
            records.put(key, record);
        }
        return records;
    }

    private static String trim(String src){
        if (src.startsWith("\"") && src.endsWith("\""))
            src = src.substring(1, src.length()-1);
        return src;
    }
}
