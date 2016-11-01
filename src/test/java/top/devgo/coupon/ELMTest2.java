package top.devgo.coupon;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import top.devgo.coupon.utils.IOUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToBinary;
import weka.filters.unsupervised.attribute.NumericToNominal;

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
        String folder = "D:\\test\\E_data\\";
//        String folder = "/Users/dd/Downloads/E_data/";
        String trainingFile = "train_data.csv";
        String predictFile = "predict_data.csv";
//        buildTrainingFile(folder, trainingFile, 0);
//        buildPredictFile(folder, predictFile);

//*
		// click  rows 47
		CSVLoader loader = new CSVLoader();
		Instances clickInstances = preProcess(folder+trainingFile, loader, true);
		
		Classifier cf = naiveBayes(clickInstances, true);
		SerializationHelper.write(folder+"click.model", cf);
		
//		// load unlabeled data and set class attribute
//	    loader.setSource(new File(folder, predictFile));
//	    Instances predictData = loader.getDataSet();
//	    
	    FastVector classAttr = new FastVector();
	    classAttr.addElement("0");
	    classAttr.addElement("1");
//	    predictData.insertAttributeAt(new Attribute("is_click", classAttr), predictData.numAttributes());
//	    predictData.setClassIndex(predictData.numAttributes() - 1);
//	    // create copy
//	    Instances result = new Instances(predictData);
//	    result.setClassIndex(result.numAttributes() - 1);
//			    
//		// label instances
//	    labelInstances(cf, predictData, result, false);
//    	// save newly labeled data
//    	DataSink.write(folder+"predict_click.csv", result);	 
    	
    	// buy  rows 47
    	Instances buyInstances = preProcess(folder+trainingFile, loader, false);
    	Classifier cfBuy = naiveBayes(buyInstances, true);
    	SerializationHelper.write(folder+"buy.model", cfBuy);
    			
//	    loader.setSource(new File(folder, predictFile));
//	    Instances predictBuy = loader.getDataSet();
//	    predictBuy.insertAttributeAt(new Attribute("is_buy", classAttr), predictBuy.numAttributes());
//	    predictBuy.setClassIndex(predictBuy.numAttributes() - 1);
//	    Instances resultBuy = new Instances(predictBuy);
//	    resultBuy.setClassIndex(resultBuy.numAttributes() - 1);
//			    
//	    labelInstances(cfBuy, predictBuy, resultBuy, false);
//    	DataSink.write(folder+"predict_buy.csv", resultBuy);	
//*/  	
    	
//    	buildOutputFile(folder, predictFile, "predict_click.csv", "predict_buy.csv", "output.csv", true);
    }

    /**
     * 
     * @param folder
     * @param predictFile
     * @param click
     * @param buy
     * @param resultFileName
     * @param tabSplit  输出的csv文件是否用tab来分隔。false表示用'，'分割
     * @throws Exception
     */
	private static void buildOutputFile(String folder, String predictFile,
			String click, String buy, String resultFileName, boolean tabSplit) throws Exception {
		Map<String, String> predict_infos = buildRecord(folder+predictFile, ",", eco_info_id_pos);
        Map<String, String> click_infos = buildRecord(folder+click, ",", eco_info_id_pos);
        Map<String, String> buy_infos = buildRecord(folder+buy, ",", eco_info_id_pos);

        List<String> records = new ArrayList<>();

        for (Entry<String, String> entry : predict_infos.entrySet()) {
            if ("HEADER".equals(entry.getKey())) continue;

            String predict_info = entry.getValue();
            String log_id = predict_info.split(",")[0];

            String[] clicks = click_infos.get(log_id).split(",");
            String is_click = clicks[clicks.length-1];
            String[] buys = buy_infos.get(log_id).split(",");
            String is_buy = buys[buys.length-1];
            
            
            //后置处理：1.所有买了的一定click过  
            if ("1".equals(is_buy)) {
				is_click = "1";
			}
            //TODO 2.每个用户至少有一个click和一个buy
            
            StringBuilder sb = new StringBuilder();
            sb.append(log_id);
            sb.append("\t");
            sb.append(is_click);
            sb.append("\t");
            sb.append(is_buy);
            
            records.add(sb.toString());
        }
        String header = "log_id"+"\t"+"is_click"+"\t"+"is_buy";
        writeCSV(folder+resultFileName, header, records, 0, tabSplit);
    }
    
	private static void labelInstances(Classifier cf, Instances predictData, Instances result, boolean log) {
		for (int i = 0; i < predictData.numInstances(); i++) {
			try {
				double clsLabel = cf.classifyInstance(predictData.instance(i));
				result.instance(i).setClassValue(clsLabel);
	    		
				if (log) {
					double[] dist = cf.distributionForInstance(predictData.instance(i));
					System.out.print((i+1) + " - ");
					System.out.print(predictData.instance(i).toString(predictData.classIndex()) + " - ");
					System.out.print(predictData.classAttribute().value((int) clsLabel) + " - ");
					System.out.println(Utils.arrayToString(dist));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
	}

    /**
     * 
     * @param path
     * @param loader
     * @param isClick true是预测click用的；false是预测buy用的
     * @return
     * @throws IOException
     * @throws Exception
     */
	private static Instances preProcess(String path, CSVLoader loader, boolean isClick) throws IOException, Exception {
		loader.setSource(new File(path));
		Instances clickInstances = loader.getDataSet();
		clickInstances.deleteAttributeAt(7);//order_id
		clickInstances.deleteAttributeAt(6);//is_raw_buy
		if (isClick) {
			clickInstances.deleteAttributeAt(5);//is_buy
		}else{
			clickInstances.deleteAttributeAt(4);//is_click
		}
		NumericToNominal filter = new NumericToNominal();
		filter.setOptions(new String[]{"-R","5"});
		filter.setInputFormat(clickInstances);
		clickInstances = Filter.useFilter(clickInstances, filter);
		clickInstances.setClassIndex(4);
		return clickInstances;
	}
    
    /**
     * 
     * @param data
     * @param showEval 是否进行Evaluation
     * @return
     * @throws Exception
     */
    private static Classifier naiveBayes(Instances data, boolean showEval) throws Exception {
	    Classifier cf  = (Classifier) Class.forName("weka.classifiers.bayes.NaiveBayes").newInstance(); 
	    cf.buildClassifier(data);
	    
	    if (showEval) {
	    	Evaluation eval = new Evaluation(data);
	    	eval.crossValidateModel(cf, data, 10, new Random(1));
	    	
	    	System.out.println(eval.toSummaryString("Results\n", false));
	    	System.out.println(eval.toClassDetailsString());
	    	System.out.println(eval.toMatrixString());
		}
        return cf;
    }
    
    private static void buildPredictFile(String folder, String resultFileName) throws Exception {
        Map<String, String> next_infos = buildRecord(folder+"next_eco_info.txt", "\t", eco_info_id_pos);
        Map<String, String> next_envs = buildRecord(folder+"next_eco_env.txt", "\t", eco_env_id_pos);
        Map<String, String> rst_infos = buildRecord(folder+"rst_info.txt", "\t", rst_info_id_pos);

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
            sb.append(eco_env==null?buildNull(16):eco_env);
            sb.append(rst_info==null?buildNull(30):rst_info);

            combineRecords.add(sb.toString());
        }

        String header = next_infos.get("HEADER") + next_envs.get("HEADER") + rst_infos.get("HEADER");
        writeCSV(folder+resultFileName, header, combineRecords, 0, false);

    }

    /**
     * 
     * @param folder
     * @param resultFileName
     * @param limit 限制输出的记录数。limit<=0,表示无限制
     * @throws Exception
     */
    private static void buildTrainingFile(String folder, String resultFileName, long limit) throws Exception {
        Map<String, String> eco_envs = buildRecord(folder+"his_eco_env.txt", "\t", eco_env_id_pos);
        Map<String, String> eco_infos = buildRecord(folder+"his_eco_info.txt", "\t", eco_info_id_pos);
//        Map<String, String> order_infos = buildRecord(folder+"his_order_info.txt", "\t", order_info_id_pos);
        Map<String, String> rst_infos = buildRecord(folder+"rst_info.txt", "\t", rst_info_id_pos);

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
            sb.append(eco_env==null?buildNull(16):eco_env);
//            sb.append(order_info==null?buildNull(27):order_info);
            sb.append(rst_info==null?buildNull(30):rst_info);

            combineRecords.add(sb.toString());
        }

		String header = eco_infos.get("HEADER") + eco_envs.get("HEADER")
		// 		+ order_infos.get("HEADER")
				+ rst_infos.get("HEADER");
        writeCSV(folder+resultFileName, header, combineRecords, limit, false);
    }

    /**
     *
     * @param path
     * @param header
     * @param records
     * @param limit 限制输出的记录数。limit<=0,表示无限制
     * @param tabSplit 输出的csv文件是否用tab来分隔。false表示用'，'分割
     * @throws IOException
     */
    private static void writeCSV(String path, String header, List<String> records, long limit, boolean tabSplit) throws IOException {
        Writer outputWriter = new FileWriter(path) ;
        CsvWriterSettings settings = new CsvWriterSettings();
        if (tabSplit) {
        	CsvFormat format = new CsvFormat();
        	format.setDelimiter('\t');
        	settings.setFormat(format);
		}
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
        outputWriter.close();
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
    private static Map<String, String> buildRecord(String file_path, String splitChar, int id_index) throws Exception {
        Map<String, String> records = new HashMap<>();
        List<String> lines = IOUtil.readFile2List(file_path, "UTF-8");
        records.put("HEADER", combine(trim(lines.get(0).split(splitChar)), splitChar));
        for (int i = 1; i < lines.size(); i++) {//skip header
            String line = lines.get(i);
            String[] attrs = line.split(splitChar);
            records.put(trim(attrs[id_index]), combine(trim(attrs), splitChar));
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
