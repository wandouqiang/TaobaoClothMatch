package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDTF;
import com.aliyun.odps.udf.UDTFCollector;
import com.aliyun.odps.udf.annotation.Resolve;
import com.aliyun.odps.udf.UDFException;

/**
 * BASE UDTF
 */
@Resolve({"bigint,bigint,bigint,string,string->bigint,bigint,bigint,bigint,bigint"})
// TODO define input and output types, e.g., "string,string->string,bigint".
public class terms2pair extends UDTF {

    /**
     * UDTF Process接口
     *
     * 每条记录都会调用此接口。
     */
    public void process(Object[] args) throws UDFException {
        // TODO: 实现对每条记录的处理逻辑
    	long cat = (Long)args[0];
    	long item1 = (Long)args[1];
    	long item2 = (Long)args[2];
    	
    	String terms1[] = quchong((String) args[3]).split(",");
    	String terms2[] = quchong((String) args[4]).split(",");
    	
    	int lengA = terms1.length;
    	int lengB = terms2.length;
    	
		for (int i = 0; i < lengA; i++) {
			for (int j = 0; j < lengB; j++) {
				forward(cat,item1,item2,Long.parseLong(terms1[i]), Long.parseLong(terms2[j]));
			}
		}

    }

    /**
     * UDTF Close接口
     *
     * 任务最后调用此接口，规格化所有数据并输出。forward方法用于输出结果
     */
    public void close() throws UDFException {
        // TODO: 实现终结逻辑
    }
    
    private String quchong(String test) {
    	String t = "";
    	if (test.trim() == null || test.trim().length() <=0){
    		t = "-1";
    	}else {
    		t = test;
    	}
    		
		String[] itemList = t.trim().split(",");
		int leng = itemList.length;
		for (int i=0;i<leng;i++){
			for (int j=i+1;j<leng;j++){
				if (itemList[i].equals(itemList[j]) && !itemList[i].equals("chuge")){
					itemList[j] = "chuge";
				}
			}
		}
	
		String result = "";
		
		for (int i = 0; i < leng; i++) {
			if (itemList[i].equals("chuge")){
				continue;
			}
			result += itemList[i] + ",";
		}
		return result.substring(0,result.length()-1);
	}
}
