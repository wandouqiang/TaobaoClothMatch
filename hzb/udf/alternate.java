package DaZhongUDF.DaZhongUDF;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class alternate extends UDF {
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
		
		// K = 穿插的比例，LA是A数组的长度，LB是B数组的长度
		int K = Integer.parseInt(LA) / Integer.parseInt(LB);

		if (a != null) {
			int m = 0,n= (K+1)*lengB;;
			int i = 0;
			int j = 0;
			int k = 0;
			while (i < lengA) {
				if (K == 1) {
					if (m >= lengB) {
						itemList[n++] = as[i++];
					} else {
						k = 2 * m;
						itemList[k] = as[i];
						i++;
						m++;
					}
				} else if (K == 3) {
					if (m >= lengB) {
						itemList[n++] = as[i++];
					} else {
						k = 4 * m ;
						itemList[k] = as[i++];
						if (i < lengA) {
							k = 4 * m + 1;
							itemList[k] = as[i++];
							if (i < lengA) {
								k = 4 * m + 2;
								itemList[k] = as[i++];
							}
						}
						m++;
					}
				}else{
					break;
				}
			}
			k++;
			while (j < lengB) {
				if (K == 1){
					if (2*j+1>=k){
						itemList[k++] = bs[j++];
					}else{
						itemList[2*j+1] = bs[j++];
					}
				}else if (K == 3) {
					if (4 * j + 3 >= k) {
						itemList[k++] = bs[j++];
					} else {
						itemList[4 * j + 3] = bs[j++];
					}
				}else{
					break;
				}
			}
		}
		
		for (int i=0;i<leng;i++){
			for (int j=i+1;j<leng;j++){
				if (itemList[i].equals(itemList[j])){
					itemList[j] = "chuge";
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
		return result;
	}

}
