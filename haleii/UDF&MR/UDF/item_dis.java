package ItemVecModUDF.itemvecmod;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class item_dis extends UDF {
	DecimalFormat df = new DecimalFormat("#0.000000");// 保留6位小数点

	/**
	 * UDF Evaluate接口
	 * 
	 * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
	 * 
	 * @param model =0 : 0 1,2,3,4
	 * @param model =1 : 1 1:1.2,2:5.4
	 * @param type= "o",欧式距离
	 * @param type= "o",欧式距离
	 * @param type= "c",余弦距离
	 * @param type= "p" 皮尔逊相关系数
	 * @param type= "m" 曼哈顿距离Manhattan
	 * @param type= "q" 切比雪夫距离
	 * @param type= "a" Adjusted Cosine调整余弦相似度
	 * @param type= "i"向量内积
	 */
	public  String evaluate(String a, String b, String model, String type) {
		// TODO:
		// model : 0 1,2,3,4
		// model : 1 1:1.2,2:5.4

		// type = "o",欧式距离
		// type = "c",余弦距离
		// type = "p" 皮尔逊相关系数
		// type = "m" 曼哈顿距离Manhattan
		// type = "q"切比雪夫距离
		// type = "a" Adjusted Cosine调整余弦相似度
		// type = "i"向量内积

		if (a == null || b == null || a.length() <= 0 || b.length() <= 0) {
			return "1000000";
		}
		double dis = 1000.0;
		if (model.equals("0")) {
			dis = getDistance0(a, b, type);
		} else if (model.equals("1")) {
			dis = getDistance1(a, b, type);
		} else {
			dis = 1000.0;
		}

		return df.format(dis);
	}

	private  double getDistance0(String a, String b, String type) {
		// type = "o",欧式距离
		// type = "c",余弦距离

		String[] v1 = a.trim().split(",");
		String[] v2 = b.trim().split(",");
		int leng = 0;
		if (v1.length != v2.length) {
			return 10000.0;
		} else {
			leng = v1.length;
		}
		double[] vector1 = new double[leng];
		double[] vector2 = new double[leng];
		for (int i = 0; i < leng; i++) {
			vector1[i] = Double.parseDouble(v1[i]);
		}

		for (int i = 0; i < leng; i++) {
			vector2[i] = Double.parseDouble(v2[i]);
		}

		double dis0 = 1000.0;
		if (type.equals("o")) {
			dis0 = euclideanDis(vector1, vector2, leng);
		} else if (type.equals("c")) {
			dis0 = cosineDis(vector1, vector2, leng);
		} else if (type.equals("p")) {
			dis0 = getPearsonSim(vector1, vector2, leng);
		} else if (type.equals("m")) {
			dis0 = getManhattanDis(vector1, vector2, leng);
		} else if (type.equals("q")) {
			dis0 = getChebyshevDis(vector1, vector2, leng);
		} else if (type.equals("a")) {
			dis0 = getAdjustedDis(vector1, vector2, leng);
		}else if (type.equals("i")) {
			dis0 = getInnerDis(vector1, vector2, leng);
		}

		return dis0;
	}


	private  double getDistance1(String a, String b, String type) {
		// type = "o",欧式距离
		// type = "c",余弦距离
		double dis0 = 1000.0;
		if (type.equals("c")) {
			dis0 = cosineDisTF(a, b);
		} else if (type.equals("o")) {
			dis0 = oulaDisTF(a, b);
		} else if (type.equals("p")) {
			dis0 = getPearSimTF(a, b);
		} else if (type.equals("m")) {
			dis0 = getManhattanDisTF(a, b);
		} else if (type.equals("q")) {
			dis0 = getChebyshevDisTF(a, b);
		} else if (type.equals("a")) {
			dis0 = getAdjustedDisTF(a, b);
		}else if (type.equals("i")) {
			dis0 = getInnerDisTF(a, b);
		}

		return dis0;

	}



	private  double cosineDisTF(String a, String b) {
		double dis = 1000.0;
		Map<String, String> map = new HashMap<String, String>();

		double mod1 = 0.0;
		double mod2 = 0.0;

		for (String string : a.split(",")) {
			String wt[] = string.split(":");
			map.put(wt[0], wt[1]);
			mod1 += Double.parseDouble(wt[1]) * Double.parseDouble(wt[1]);
		}
		double pointMulti_result = 0;
		for (String string : b.split(",")) {
			String wt[] = string.split(":");
			mod2 += Double.parseDouble(wt[1]) * Double.parseDouble(wt[1]);

			if (map.get(wt[0]) == null)
				continue;
			else

				pointMulti_result += Double.parseDouble(map.get(wt[0])
						.toString()) * Double.parseDouble(wt[1]);
		}

		dis = pointMulti_result / (Math.sqrt(mod1 * mod2));

		return dis;
	}

	private  double oulaDisTF(String a, String b) {
		double dis = 1000.0;

		Map<String, Double> map1 = new HashMap<String, Double>();
		Map<String, Double> map2 = new HashMap<String, Double>();

		String[] as = a.trim().split(",");
		String[] key1 = new String[as.length];

		for (int i = 0; i < as.length; i++) {
			String wt[] = as[i].split(":");
			map1.put(wt[0], Double.parseDouble(wt[1]));
			key1[i] = wt[0];

		}

		String[] bs = b.trim().split(",");
		String[] key2 = new String[bs.length];

		for (int i = 0; i < bs.length; i++) {
			String wt[] = bs[i].split(":");
			map2.put(wt[0], Double.parseDouble(wt[1]));
			key2[i] = wt[0];
		}

		double diff = 0.0;

		for (int i = 0; i < as.length; i++) {
			if (map2.get(key1[i]) == null) {
				diff += (map1.get(key1[i])) * (map1.get(key1[i]));
			} else {
				diff += (map1.get(key1[i]) - map2.get(key1[i]))
						* (map1.get(key1[i]) - map2.get(key1[i]));
			}
		}
		for (int i = 0; i < bs.length; i++) {
			if (map1.get(key2[i]) == null) {
				diff += (map2.get(key2[i])) * (map2.get(key2[i]));
			}
		}

		dis = (Math.sqrt(diff));

		return dis;
	}

	private  double getPearSimTF(String v1, String v2) {
		Pearson sim1 = new Pearson();
		Pearson sim2 = new Pearson();

		String[] as = v1.trim().split(",");
		String[] bs = v2.trim().split(",");
		int lengA = as.length;
		int lengB = bs.length;
		int leng = 0;
		int a = Integer.parseInt(as[lengA - 1].split(":")[0]);
		int b = Integer.parseInt(bs[lengB - 1].split(":")[0]);

		if (a > b) {
			leng = a;
		} else {
			leng = b;
		}

		Map<String, Double> map1 = new HashMap<String, Double>();
		Map<String, Double> map2 = new HashMap<String, Double>();

		for (String s : as) {
			String wt[] = s.split(":");
			map1.put(wt[0], Double.parseDouble(wt[1]));
		}

		for (String s : bs) {
			String wt[] = s.split(":");
			map2.put(wt[0], Double.parseDouble(wt[1]));
		}

		for (int i = 0; i <= leng; i++) {
			if (map1.get(String.valueOf(i)) == null) {
				sim1.rating_map.put(String.valueOf(i), 0.0);
			} else {
				sim1.rating_map.put(String.valueOf(i),
						map1.get(String.valueOf(i)));
			}
			if (map2.get(String.valueOf(i)) == null) {
				sim2.rating_map.put(String.valueOf(i), 0.0);
			} else {
				sim2.rating_map.put(String.valueOf(i),
						map2.get(String.valueOf(i)));
			}

		}

		double dis = sim1.getsimilarity_bydim(sim2);
		return dis;
	}

	private  double getManhattanDisTF(String a, String b) {

		Map<String, Double> map1 = new HashMap<String, Double>();
		Map<String, Double> map2 = new HashMap<String, Double>();

		String[] as = a.trim().split(",");
		String[] key1 = new String[as.length];

		for (int i = 0; i < as.length; i++) {
			String wt[] = as[i].split(":");
			map1.put(wt[0], Double.parseDouble(wt[1]));
			key1[i] = wt[0];

		}

		String[] bs = b.trim().split(",");
		String[] key2 = new String[bs.length];

		for (int i = 0; i < bs.length; i++) {
			String wt[] = bs[i].split(":");
			map2.put(wt[0], Double.parseDouble(wt[1]));
			key2[i] = wt[0];
		}

		double diff = 0.0;

		for (int i = 0; i < as.length; i++) {
			if (map2.get(key1[i]) == null) {
				diff += Math.abs(map1.get(key1[i]));
			} else {
				diff += Math.abs(map1.get(key1[i]) - map2.get(key1[i]));

			}
		}
		for (int i = 0; i < bs.length; i++) {
			if (map1.get(key2[i]) == null) {
				diff += Math.abs(map2.get(key2[i]));
			}
		}
		return diff;
	}

	private  double getChebyshevDisTF(String a, String b) {

		Map<String, Double> map1 = new HashMap<String, Double>();
		Map<String, Double> map2 = new HashMap<String, Double>();

		String[] as = a.trim().split(",");
		String[] key1 = new String[as.length];

		for (int i = 0; i < as.length; i++) {
			String wt[] = as[i].split(":");
			map1.put(wt[0], Double.parseDouble(wt[1]));
			key1[i] = wt[0];

		}

		String[] bs = b.trim().split(",");
		String[] key2 = new String[bs.length];

		for (int i = 0; i < bs.length; i++) {
			String wt[] = bs[i].split(":");
			map2.put(wt[0], Double.parseDouble(wt[1]));
			key2[i] = wt[0];
		}

		double diff = 0.0;

		for (int i = 0; i < as.length; i++) {
			if (map2.get(key1[i]) == null) {
				if (diff <= Math.abs(map1.get(key1[i]))) {
					diff = Math.abs(map1.get(key1[i]));
				}
			} else {
				if (diff <= Math.abs(map1.get(key1[i]) - map2.get(key1[i]))) {
					diff = Math.abs(map1.get(key1[i]) - map2.get(key1[i]));
				}

			}
		}
		for (int i = 0; i < bs.length; i++) {
			if (map1.get(key2[i]) == null) {
				if (diff <= Math.abs(map2.get(key2[i]))) {
					diff = Math.abs(map2.get(key2[i]));
				}
			}
		}

		return diff;
	}

	private  double getAdjustedDisTF(String a, String b) {

		String[] as = a.trim().split(",");
		String[] bs = b.trim().split(",");
		int lengA = as.length;
		int lengB = bs.length;
		int leng = 0;
		int A = Integer.parseInt(as[lengA - 1].split(":")[0]);
		int B = Integer.parseInt(bs[lengB - 1].split(":")[0]);

		if (A > B) {
			leng = A;
		} else {
			leng = B;
		}

		Map<String, Double> map1 = new HashMap<String, Double>();
		Map<String, Double> map2 = new HashMap<String, Double>();

		double mean1 = 0.0;
		double mean2 = 0.0;

		for (String s : as) {
			String wt[] = s.split(":");
			mean1 += Double.parseDouble(wt[1]);
			map1.put(wt[0], Double.parseDouble(wt[1]));
		}

		for (String s : bs) {
			String wt[] = s.split(":");
			mean2 += Double.parseDouble(wt[1]);
			map2.put(wt[0], Double.parseDouble(wt[1]));
		}
		mean1 = mean1 / (leng + 1);
		mean2 = mean2 / (leng + 1);

		double dis = 0.0;
		double mod1 = 0.0;
		double mod2 = 0.0;
		double temp1 = 0.0;
		double temp2 = 0.0;

		for (int i = 0; i <= leng; i++) {
			if (map1.get(String.valueOf(i)) == null) {
				temp1 = -mean1;
			} else {
				temp1 = map1.get(String.valueOf(i)) - mean1;
			}
			if (map2.get(String.valueOf(i)) == null) {
				temp2 = -mean2;
			} else {
				temp2 = map2.get(String.valueOf(i)) - mean2;
			}
			dis += temp1 * temp2;
			mod1 += temp1 * temp1;
			mod2 += temp2 * temp2;

		}

		return dis / Math.sqrt(mod1 * mod2);
	}
	
	private  double getInnerDisTF(String a, String b) {

		Map<String, String> map = new HashMap<String, String>();

		for (String string : a.split(",")) {
			String wt[] = string.split(":");
			map.put(wt[0], wt[1]);
		}
		double pointMulti_result = 0;
		for (String string : b.split(",")) {
			String wt[] = string.split(":");

			if (map.get(wt[0]) == null)
				continue;
			else

				pointMulti_result += Double.parseDouble(map.get(wt[0])
						.toString()) * Double.parseDouble(wt[1]);
		}


		return pointMulti_result;
	}
	
	

	private  double cosineDis(double[] vector1, double[] vector2, int leng) {
		// TODO Auto-generated method stub
		double dis = 0.0;
		double mod1 = 0.0;
		double mod2 = 0.0;
		for (int i = 0; i < leng; i++) {
			dis += vector1[i] * vector2[i];
			mod1 += vector1[i] * vector1[i];
			mod2 += vector2[i] * vector2[i];
		}

		dis = dis / Math.sqrt(mod1 * mod2);

		return dis;
	}

	private  double euclideanDis(double[] vector1, double[] vector2,
			int leng) {
		// TODO Auto-generated method stub
		double dis = 0.0;
		double diff = 0.0;

		for (int i = 0; i < leng; i++) {
			diff += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
		}
		dis = Math.sqrt(diff);

		return dis;
	}

	private  double getPearsonSim(double[] vector1, double[] vector2,
			int leng) {
		Pearson sim1 = new Pearson();
		Pearson sim2 = new Pearson();

		for (int i = 0; i < leng; i++) {
			sim1.rating_map.put(String.valueOf(i), vector1[i]);
		}

		for (int i = 0; i < leng; i++) {
			sim2.rating_map.put(String.valueOf(i), vector2[i]);
		}

		double dis = sim1.getsimilarity_bydim(sim2);
		return dis;
	}

	private  double getManhattanDis(double[] vector1, double[] vector2,
			int leng) {
		double dis = 0.0;

		for (int i = 0; i < leng; i++) {
			dis += Math.abs(vector1[i] - vector2[i]);
		}
		return dis;
	}

	private  double getChebyshevDis(double[] vector1, double[] vector2,
			int leng) {
		double dis = 0.0;

		for (int i = 0; i < leng; i++) {
			if (dis <= Math.abs(vector1[i] - vector2[i])) {
				dis = Math.abs(vector1[i] - vector2[i]);
			}
		}
		return dis;
	}

	private  double getAdjustedDis(double[] vector1, double[] vector2,
			int leng) {
		double dis = 0.0;
		double mod1 = 0.0;
		double mod2 = 0.0;
		double mean1 = 0.0;
		double mean2 = 0.0;

		for (int i = 0; i < leng; i++) {
			mean1 += vector1[i];
			mean2 += vector2[i];
		}

		mean1 = mean1 / leng;
		mean2 = mean2 / leng;

		double temp1 = 0.0;
		double temp2 = 0.0;

		for (int i = 0; i < leng; i++) {
			temp1 = vector1[i] - mean1;
			temp2 = vector2[i] - mean2;
			dis += temp1 * temp2;
			mod1 += temp1 * temp1;
			mod2 += temp2 * temp2;
		}

		dis = dis / Math.sqrt(mod1 * mod2);

		return dis;

	}
	private  double getInnerDis(double[] vector1, double[] vector2,
			int leng) {
		double dis = 0.0;

		for (int i = 0; i < leng; i++) {
			dis += vector1[i]*vector2[i];
		}
		return dis;
	}
}
