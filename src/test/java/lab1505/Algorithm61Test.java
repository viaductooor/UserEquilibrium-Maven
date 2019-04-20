package lab1505;

import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.algorithm6.Algorithm6;
import org.lab1505.ue.alg.algorithm6.Algorithm61;

public class Algorithm61Test {
    @Test
    public void total(){
        Algorithm61.run(Algorithm6.NET_URL,Algorithm6.TRIP_URL,Algorithm61.VOLUME_2);
    }
}