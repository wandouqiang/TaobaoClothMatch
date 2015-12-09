package taobao.udf_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class cal_tfidf_sim extends UDF {
    /**
     * UDF Evaluate接口
     * 
     * UDF在记录层面上是一对一，字段上是一对一或多对一。 Evaluate方法在每条记录上被调用一次，输入为一个或多个字段，输出为一个字段
     */
    public String evaluate(String a, String b,String mod_a,String mod_b) {
        // TODO: 请按需要修改参数和返回值，并在这里实现你自己的逻辑

        if (a == null || b == null) {
            return "1000000";
        }       
        Map<String, String> map =
                new HashMap<String, String>();
         for (String string : a.split(",")) {
    		  String wt[]=string.split(":");	
        	 map.put(wt[0], wt[1]);
    		}
        double pointMulti_result=0;
		for (String string : b.split(",")) {
			String wt[]=string.split(":");	
			if (map.get(wt[0])==null)
				continue;
			else
				System.out.println(map.get(wt[0]));
			pointMulti_result+=Double.parseDouble(map.get(wt[0]).toString())*Double.parseDouble(wt[1]);
			//vector2.add(Double.parseDouble(string));
		}
        
		
		double result=pointMulti_result / (Double.parseDouble(mod_a)*Double.parseDouble(mod_b));

        return result+"";
    }
    
}
