package ItemVecModUDF.itemvecmod;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class minhash_sim extends UDF {
	/**
	 * UDF Evaluate接口
	 * 
	 * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
	 */
	public String evaluate(String a, String b, String type) {
		// type = "0"杰卡德相似距离，去重Jaccard
		// type= "1"交集个数，不去重
		// type ="2"编辑距离，不去重
		// type ="3"编辑距离比(简单共有词)，不去重

		if (a == null || b == null || a.length() <= 0 || b.length() <= 0) {
			return "1000000";
		}

		double result = 1000.0;

		if (type.equals("0")) {
			result = jaccardDis(a, b);
		} else if (type.equals("1")) {
			result = innerDis(a, b);
		} else if (type.equals("2") || type.equals("3")) {
			result = editDis(a, b,type);
		}
		return String.valueOf(result);
	}

	private double editDis(String a, String b, String type) {
		String[] s1 = a.split(",");
		String[] s2 = b.split(",");
		double re = 0.0;
		double edis = ld(s1, s2);
		double s = sim(s1, s2);
		if (type.equals("2")) {
			re = edis;
		}else if (type.equals("3")){
			re = s;
		}
		
		return re;
	}

	private static int min(int one, int two, int three) {
		int min = one;
		if (two < min) {
			min = two;
		}
		if (three < min) {
			min = three;
		}
		return min;
	}

	public static int ld(String[] s1, String[] s2) {
		 
		int n = s1.length;
		int m = s2.length;
		String ch1; // str1的
		String ch2; // str2的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		int d[][] = new int[n + 1][m + 1];
		for (int i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}
		for (int j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}
		for (int i = 1; i <= n; i++) { // 遍历str1
			ch1 = s1[i - 1];
			// 去匹配str2
			for (int j = 1; j <= m; j++) {
				ch2 = s2[j - 1];
				if (ch1.equals(ch2)) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
						+ temp);
			}
		}
		return d[n][m];
	}

	public static double sim(String[] s1, String[] s2) {
		int ld = ld(s1, s2);
		return 1 - (double) ld / Math.max(s1.length, s2.length);
	}
	
	private double innerDis(String a, String b) {

		String[] v1 = a.trim().split(",");
		String[] v2 = b.trim().split(",");
		int lengA = v1.length;
		int lengB = v2.length;

		int Comm = 0;
		for (int i = 0; i < lengA; i++) {
			for (int j = 0; j < lengB; j++) {
				if (v1[i].trim().equals(v2[j].trim())) {
					Comm++;
				}
			}
		}
		return Comm;

	}

	private double jaccardDis(String a, String b) {
		String a1 = quchong(a);
		String b1 = quchong(b);

		String[] v1 = a1.trim().split(",");
		String[] v2 = b1.trim().split(",");
		int lengA = v1.length;
		int lengB = v2.length;

		int Comm = 0;
		int Diff = 0;
		for (int i = 0; i < lengA; i++) {
			for (int j = 0; j < lengB; j++) {
				if (v1[i].trim().equals(v2[j].trim())) {
					Comm++;
				}
			}
		}
		Diff = lengA + lengB - Comm;
		double sim = (double) Comm / Diff;

		return sim;
	}

	private String quchong(String test) {
		String[] itemList = test.split(",");
		int leng = itemList.length;
		for (int i = 0; i < leng; i++) {
			for (int j = i + 1; j < leng; j++) {
				if (itemList[i].equals(itemList[j])
						&& !itemList[i].equals("chuge")) {
					itemList[j] = "chuge";
				}
			}
		}
		int count = 0;

		String result = "";

		for (int i = 0; i < leng; i++) {
			if (itemList[i].equals("chuge")) {
				continue;
			}
			result += itemList[i] + ",";
			count++;
			// if (count >= 200){
			// break;
			// }
		}
		return result.substring(0, result.length() - 1);
	}
}
