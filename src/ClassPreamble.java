/**
 * Created with IntelliJ IDEA.
 * User: matthewburns
 * Date: 12/10/12
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public @interface ClassPreamble {
        String author();
        String date();
        int currentRevision() default 1;
        String lastModified() default "N/A";
        String lastModifiedBy() default "N/A";
        // Note use of array
        String[] reviewers();
    }

