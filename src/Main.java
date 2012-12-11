import java.util.concurrent.atomic.AtomicIntegerArray;

public class Main {

    public static void main(String[] args) {
        boolean status = true;
        char last_name;
        last_name = 'a';
        byte register =  64;
        short foo = 32000;
        int bar =  1000000;
        float baz;
        baz = 0x5f5e100;
        int[] nums = new int[3];
        nums[0] = 0;
        nums[1] = 1;
        nums[2] = 2;

        Dog[] pets;
        pets = new Dog[2];
        pets[0] = new Dog();
        pets[1] = new Dog();
        pets[0].setName("fido") ;
        pets[1].setName("spot");

        int x = 0;
        while( x < pets.length ) {
            String name = pets[x].getName();
            System.out.println(name + " says Ruff");
            x++;
        }

        Dog dog = new Dog();
        for(x = 0; x < 10; x++) {
            System.out.println("One small step for man");
            dog.bark(3);
        }
    }
}
