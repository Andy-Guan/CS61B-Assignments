package flik;

import static flik.Flik.isSameNumber;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;


public class TestFlik {

    @Test
    public void WhetherTheSame(){
        assertTrue(isSameNumber(5,5));

        assertFalse(isSameNumber(5,6));
    }
}
