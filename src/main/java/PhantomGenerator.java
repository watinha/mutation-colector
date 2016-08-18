import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class PhantomGenerator {
    private Runtime runtime = null;

    private static String PHANTOM_COMMAND = "phantomjs RIA_menu_event_controller.js ";

    public PhantomGenerator (Runtime runtime) {
        this.runtime = runtime;
    }

    public String generate (String url) throws Exception {
        String selector = "",
               error_message = "",
               aux;
        BufferedReader br = null;
        Process p = this.runtime.exec(PhantomGenerator.PHANTOM_COMMAND + url);
        p.waitFor();

        if (p.exitValue() > 0) {
            br = this.getBufferedReader(p.getErrorStream());
            while ((aux = br.readLine()) != null) {
                error_message += "\n" + aux;
            }
            br.close();
            throw new Exception(error_message);
        }

        br = this.getBufferedReader(p.getInputStream());
        selector = br.readLine();
        br.close();
        return selector;
    }

    public BufferedReader getBufferedReader (InputStream is) {
        return new BufferedReader(
                new InputStreamReader(is));
    }
}
