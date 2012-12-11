import java.util.ArrayList;
import java.util.List;

enum Day {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
    THURSDAY, FRIDAY, SATURDAY
}

@ClassPreamble (
        author = "Matthew Burns",
        date = "3/17/2002",
        currentRevision = 6,
        lastModified = "4/12/2004",
        lastModifiedBy = "Jane Doe",
        // Note array notation
        reviewers = {"Alice", "Bob", "Cindy"}
                )
public class Dog {
    private int size;
    private String breed;
    private String name;

    public int getSize() {
        return this.size;
    }

    public void setSize(int s) {
        this.size = s;
    }

    public String getBreed() {
        return this.breed;
    }

    public void setBreed(String b) {
        breed = b;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        name = n;
    }

    public void speak(int number_of_barks) {
        while (number_of_barks > 0) {
            System.out.println("Ruff, Ruff");
            number_of_barks--;
            Day today = Day.MONDAY;
            System.out.println("Today is " + today);
        }
    }

}


