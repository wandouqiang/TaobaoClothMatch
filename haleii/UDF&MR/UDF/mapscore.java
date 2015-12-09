package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class mapscore extends UDF {
	/**
	 * UDF Evaluate接口
	 * 
	 * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
	 */

	public String evaluate(String test,String ans) {
		// TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑
		// a,selected ,b,history
		
		String ts1 = quchong(test);
		
		String[] as = ans.split(",");
		String[] ts = ts1.split(",");
		int lengT = ts.length;
		int lengA = as.length;
		
		double[] p = new double[lengT];
		int[] delta = new int[lengT];
		for (int j = 0; j < lengT; j++) {
			for (int i = 0; i < lengA; i++) {
				if (as[i].equals(ts[j])) {
					delta[j] = 1;
				}
			}
		}
		
		int count =0;

		for (int j = 0; j < lengT; j++) {
			
			count += delta[j];
			p[j] = (double)count / (j+1);
			
		}
		
		double fenzi = 0.0;
		for (int i=0;i<lengT;i++){
			fenzi += delta[i]/(1-Math.log(p[i]));
		}
		double api = fenzi /lengA;
		
		return String.valueOf(api);
		
	}

	private String  quchong(String test) {
		String[] itemList = test.split(",");
		int leng = itemList.length;
		for (int i=0;i<leng;i++){
			for (int j=i+1;j<leng;j++){
				if (itemList[i].equals(itemList[j]) && !itemList[i].equals("chuge")){
					itemList[j] = "chuge";
//					if (i > 0) {
//						temp = itemList[i - 1];
//						itemList[i - 1] = itemList[i];
//						itemList[i] = temp;
//					}

				}
			}
		}
		int count= 0;
	
		String result = "";
		
		for (int i = 0; i < leng; i++) {
			if (itemList[i].equals("chuge")){
				continue;
			}
			result += itemList[i] + ",";
			count++;
			if (count >= 200){
				break;
			}
		}
		return result.substring(0,result.length()-1);
	}
	
	

}
