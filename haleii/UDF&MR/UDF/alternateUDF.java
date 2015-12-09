package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class alternateUDF extends UDF {
	/**
	 * UDF Evaluate接口
	 * 
	 * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
	 */

	public String evaluate(String a, String b, String LA, String LB) {
		// TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑
		// a,selected ,b,history

		String[] as = a.split(",");
		String[] bs = b.split(",");
		int lengB = bs.length;
		int lengA = as.length;
		int leng = lengA + lengB;
		String[] itemList = new String[leng];
		
		int K1 = Integer.parseInt(LA);
		int K2 = Integer.parseInt(LB);
		int K = K1+K2;
		int th ;
//		if (lengB%K2 == 0){
//			th = (K1 + K2) * (lengB / K2);
//		}else{
//			th = (K1 + K2) * (lengB / K2) + K1 + lengB % K2;
//		}
		th = (K1 + K2) * (lengB / K2) + K1 + lengB % K2;
	 
		if (a != null) {
			int m = 0;
			int n = th;
			int i = 0;
			int j = 0;
			int k = 0;
			while (i < lengA) {
				if (k + 1 >= th) {
					itemList[n++] = as[i++];
				} else {
					for (int t = 0; t < K1; t++) {
						k = K * m + t;
						if (i < lengA && k+1<th) {
							itemList[k] = as[i++];
						}
					}
					m++;
				}
			}

			for (int t = 0; t < leng; t++) {
				if (itemList[t] == null && j < lengB) {
					itemList[t] = bs[j++];
				}
			}
		}
	
		String temp = "";
		for (int i=0;i<leng;i++){
			for (int j=i+1;j<leng;j++){
				if (itemList[i].equals(itemList[j]) && !itemList[i].equals("chuge")){
					itemList[j] = "chuge";
					if (i > 0) {
						temp = itemList[i - 1];
						itemList[i - 1] = itemList[i];
						itemList[i] = temp;
					}

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

		
		result = result.substring(0, result.length() - 1);
		System.out.println(result);
		return result;
	}

}
