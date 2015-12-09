package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class same_terms_rate extends UDF {
	/**
	 * UDF Evaluate接口
	 * 
	 * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
	 */

	public String evaluate(String a,String b) {
		// TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑
		// a,selected ,b,history
		
		
		String[] as = a.split(",");
		String[] bs = b.split(",");
		int lengB = bs.length;
		int lengA = as.length;
	
		int count = 0;
		for (int i=0;i<lengA;i++){
			for (int j=0;j<lengB;j++){
				if (as[i].equals(bs[j])){
					count++;
				}
			}
		}
		
		return String.valueOf((double)count/lengA);
	}
	
	

}
