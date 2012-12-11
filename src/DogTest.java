import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: matthewburns
 * Date: 12/10/12
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class DogTest extends TestCase {
    Dog test_dog;
    @Before
    public void setUp() throws Exception {
        test_dog = new Dog();
        test_dog.setName("Fido");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearing down");
        test_dog = null;
    }

    @Test
    public void testGetSize() throws Exception {
        assertEquals(test_dog.getName(), "Fido");
    }

    @Test
    public void testSetSize() throws Exception {
        test_dog.setSize(10);
        assertEquals(test_dog.getSize(), 10);
    }

    @Test
    public void testGetBreed() throws Exception {

    }

    @Test
    public void testSetBreed() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testBark() throws Exception {

    }
}
