package lab1505;

import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.oct5.Algorithm8;;

public class OctTest{
    @Test
    public void run(){
        Algorithm8.run(Algorithm8.NET_5, Algorithm8.TRIPS_5,"oct5_5pm_");
        Algorithm8.run(Algorithm8.NET_8, Algorithm8.TRIPS_8,"oct5_8am_");
    }
}