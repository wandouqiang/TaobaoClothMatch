package taobao.udf_test;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.odps.udf.UDF;

/***
 * BASE UDF
 */
public final class cal_sim extends UDF {
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
    	String aa[]=a.split(",");
    	String bb[]=b.split(",");
    	double pointMulti_result=0;
    	for (int i=0;i<aa.length;i++){
    		
    		pointMulti_result+=Double.parseDouble(aa[i])*Double.parseDouble(bb[i]);
    	}
        
		
		double result=pointMulti_result / (Double.parseDouble(mod_a)*Double.parseDouble(mod_b));

        return result+"";
        
    }
    
}
