package lab1505;

import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.algorithm6.Algorithm6;
import org.lab1505.ue.alg.algorithm7.Algorithm7;
import org.lab1505.ue.alg.algorithm7.Algorithm72;

public class Algorithm7Test{

    @Test
    public void alg1(){
        Algorithm7.run(Algorithm6.NET_URL,Algorithm6.TRIP_URL,Algorithm7.VOLUME_2);
    }

    @Test
    public void alg2(){
        Algorithm72.run(Algorithm6.NET_URL,Algorithm6.TRIP_URL,Algorithm7.VOLUME_2);
    }
}