import edu.miun.firespark.domain.Weather;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Dwe on 2016-03-03.
 */
public class SMHItest {

    @Test
    public void testSMHITemp(){
        RestTemplate rt = new RestTemplate();
        Weather w = rt.getForObject("http://opendata-download-metfcst.smhi.se/api/category/pmp1.5g/version/1/geopoint/lat/58.59/lon/16.18/data.json",Weather.class);
        //ApplicationContext ac = new ClassPathXmlApplicationContext("/src/beans.xml");

        System.out.println(w.getData().get(0).getT());
    }


}
