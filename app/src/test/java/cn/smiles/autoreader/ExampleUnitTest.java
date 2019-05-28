package cn.smiles.autoreader;

import org.junit.Test;

import cn.smiles.autoreader.ktool.KTools;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test1() {
        for (int i = 0; i < 10; i++) {
            int r = KTools.getRandomNumberInRange(3, 6);
            System.out.println(r);
        }

    }
}