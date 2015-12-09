package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class sparse_feat extends UDF {
    /**
     * UDF Evaluate接口
     * 
     * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
     */
    public String evaluate(String delta_day, String count,String w2v1,String w2v2) {
        // TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑
		String[] vec = (w2v1 + "," + w2v2).split(",");
		int leng = vec.length;
    	String[] sparse = new String[leng+2];
    	sparse[0] = "0:"+delta_day;
    	sparse[1] = "1:"+count;
		
		
    	for (int i = 0;i<leng;i++){
    		sparse[i+2] = (i+2)+":"+vec[i];
    	}

    	String result = "";
    	for (int j=0;j<leng+2;j++){
    		result += sparse[j]+",";
    	}
    	result = result.substring(0,result.length()-1);
		return result;
    		
    }
}
