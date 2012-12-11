/**
 * Created with IntelliJ IDEA.
 * User: matthewburns
 * Date: 12/10/12
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pet<T> {
    private T creature = new T();

    public void set(T t) { this.creature = t; }
    public T get() { return creature; }

    public void talk(int n) {
        this.creature.speak(n);
    }
}
