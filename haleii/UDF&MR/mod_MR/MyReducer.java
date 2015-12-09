package taobao.TaoBao;

import com.aliyun.odps.data.Record;
import com.aliyun.odps.mapred.Reducer;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Reducer模板。请用真实逻辑替换模板内容
 */
public class MyReducer implements Reducer {
	private Record result;
	DecimalFormat df=new DecimalFormat("#0.000000");//保留6位小数点
	Map<Long, Long> map;
	List<Map.Entry<Long, Long>> entryList;

	public void setup(TaskContext context) throws IOException {
		result = context.createOutputRecord();
	}

	public void reduce(Record key, Iterator<Record> values, TaskContext context)
			throws IOException {
		String word = "";
		String wordTdidf = "";
		double tdidf = 0.0;
		double mod = 0.0;
		while (values.hasNext()) {
			Record val = values.next();
			word = val.getString("word");
			tdidf = val.getDouble("tfidf");
			mod += tdidf*tdidf;
			wordTdidf += word + ":"+df.format(tdidf)+",";
		}
		mod = Math.sqrt(mod);
		wordTdidf = wordTdidf.substring(0, wordTdidf.length() - 1);
		result.set(0, Long.parseLong(key.getString("item")));
		result.set(1, wordTdidf);
		result.set(2, df.format(mod));
		
		context.write(result);
	}

	public void cleanup(TaskContext arg0) throws IOException {

	}

}
