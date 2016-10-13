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
public class ELMTest2 {
    private static int eco_env_id_pos = 0;
    private static int eco_info_id_pos = 0;
    private static int order_info_id_pos = 2;
    private static int rst_info_id_pos = 0;

    public static void main(String[] args) throws Exception {
//        String folder = "D:\\test\\E_data\\";
        String folder = "/Users/dd/Downloads/E_data/";
        buildTrainingFile(folder);
//        buildPredictFile(folder);
    }

    private static void buildPredictFile(String folder) throws Exception {
        Map<String, String> next_infos = buildRecord(folder+"next_eco_info.txt", eco_info_id_pos);
        Map<String, String> next_envs = buildRecord(folder+"next_eco_env.txt", eco_env_id_pos);
        Map<String, String> rst_infos = buildRecord(folder+"rst_info.txt", rst_info_id_pos);

        List<String> combineRecords = new ArrayList<>();

        for (Entry<String, String> entry : next_infos.entrySet()) {
            if ("HEADER".equals(entry.getKey())) continue;

            String eco_info = entry.getValue();
            String list_id = eco_info.split("\t")[1];
            String restaurant_id = eco_info.split("\t")[2];
            String eco_env = next_envs.get(list_id);
            String rst_info = rst_infos.get(restaurant_id);

            StringBuilder sb = new StringBuilder();
            sb.append(eco_info);
            sb.append("\t");
            sb.append(eco_env==null?buildNull(16):eco_env);
            sb.append("\t");
            sb.append(rst_info==null?buildNull(30):rst_info);

            combineRecords.add(sb.toString());
        }

        String header = next_infos.get("HEADER") + "\t" + next_envs.get("HEADER")
                + "\t" + rst_infos.get("HEADER");
        writeCSV(folder+"predict_data.csv", header, combineRecords, 0);

    }

    private static void buildTrainingFile(String folder) throws Exception {
        Map<String, String> eco_envs = buildRecord(folder+"his_eco_env.txt", eco_env_id_pos);
        Map<String, String> eco_infos = buildRecord(folder+"his_eco_info.txt", eco_info_id_pos);
//        Map<String, String> order_infos = buildRecord(folder+"his_order_info.txt", order_info_id_pos);
        Map<String, String> rst_infos = buildRecord(folder+"rst_info.txt", rst_info_id_pos);

        List<String> combineRecords = new ArrayList<>();

        for (Entry<String, String> entry : eco_infos.entrySet()) {
            if ("HEADER".equals(entry.getKey())) continue;

        	String eco_info = entry.getValue();
            String list_id = eco_info.split("\t")[1];
            String restaurant_id = eco_info.split("\t")[2];
            String order_id = eco_info.split("\t")[7];
            String eco_env = eco_envs.get(list_id);
//            String order_info = order_infos.get(order_id);
            String rst_info = rst_infos.get(restaurant_id);

            StringBuilder sb = new StringBuilder();
            sb.append(eco_info);
            sb.append("\t");
            sb.append(eco_env==null?buildNull(16):eco_env);
            sb.append("\t");
//            sb.append(order_info==null?buildNull(27):order_info);
//            sb.append("\t");
            sb.append(rst_info==null?buildNull(30):rst_info);

            combineRecords.add(sb.toString());
        }

        String header = eco_infos.get("HEADER") + "\t" + eco_envs.get("HEADER")
//                + "\t" + order_infos.get("HEADER")
                + "\t" + rst_infos.get("HEADER");
        writeCSV(folder+"train_data_10w.csv", header, combineRecords, 100000);
    }

    /**
     *
     * @param path
     * @param header
     * @param records
     * @param limit 限制输出的记录数。limit<=0,表示无限制
     * @throws IOException
     */
    private static void writeCSV(String path, String header, List<String> records, long limit) throws IOException {
        Writer outputWriter = new FileWriter(path) ;
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setNullValue("?");
        CsvWriter writer = new CsvWriter(outputWriter, settings);


        List<Integer> deleteIndex = getRepeatedIndex(header.split("\t"));
        List<String> headers = new ArrayList<String>(Arrays.asList(header.split("\t")));
        for (int i = deleteIndex.size()-1; i>=0; i--) {
            headers.remove((int)deleteIndex.get(i));
        }
        writer.writeHeaders(headers);
        for (int i = 0; i < records.size(); i++) {
        	List<String> record = new ArrayList<String>(Arrays.asList(records.get(i).split("\t")));
        	for (int j = deleteIndex.size()-1; j>=0; j--) {
    			record.remove((int)deleteIndex.get(j));
    		}
            writer.writeRow(record.toArray(new String[]{}));

            if (limit>0 && i>=limit-1) {
				break;
			}
        }
        writer.close();
    }

    private static String buildNull(int i) {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; j++) {
			sb.append("null");
			if (j<i-1) 
				sb.append("\t");
		}
		return sb.toString();
	}

    /**
     * 获取数组中重复元素的下标
     * @param temp
     * @return
     */
	private static List<Integer> getRepeatedIndex(String[] temp) {
    	List<Integer> index = new ArrayList<Integer>();
        for (int i = 0; i < temp.length; i++) {
            String h = temp[i];
            boolean repeated = false;
            for (int j = 0; j < i; j++) {
                if (temp[j].equals(h)) {
                    repeated = true;
                    break;
                }
            }
            if (repeated) {
            	index.add(i);
            }
        }
        return index;
    }

    /**
     * build数据集
     * @param file_path
     * @param id_index 记录中作为主键的attr下标
     * @return map中保存&lt;id, recordStr&gt;, 同时key=HEADER的记录保存了header
     * @throws Exception
     */
    private static Map<String, String> buildRecord(String file_path, int id_index) throws Exception {
        Map<String, String> records = new HashMap<>();
        List<String> lines = IOUtil.readFile2List(file_path, "UTF-8");
        records.put("HEADER", combine(trim(lines.get(0).split("\t")), "\t"));
        for (int i = 1; i < lines.size(); i++) {//skip header
            String line = lines.get(i);
            String[] attrs = line.split("\t");
            records.put(trim(attrs[id_index]), combine(trim(attrs), "\t"));
        }
        return records;
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

    private static String[] trim(String[] array){
    	for (int i = 0; i < array.length; i++) {
            array[i] = trim(array[i]);
		}
        return array;
    }

    private static String trim(String s){
        if (s.startsWith("\"") && s.endsWith("\""))
            s = s.substring(1, s.length()-1);
        return s;
    }
}
