import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class App {

    private WebDriver driver;
    private String folder = "data/";

    private String readResource (String resourceFilename) {
        String resource = "", buffer = "";
        InputStream is = this.getClass()
                             .getClassLoader()
                             .getResourceAsStream(resourceFilename);
        BufferedReader br = new BufferedReader(
                              new InputStreamReader(is));
        try {
            while ((buffer = br.readLine()) != null) {
                resource += buffer + "\n";
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return resource;
    }

    private List <WebElement> setMutationObserved () {
        String js = this.readResource("set_mutation_observer.js");
        return this.executeJavaScript(js);
    }

    private List <WebElement> getMutations () {
        String js = this.readResource("get_mutations_observed.js");
        return this.executeJavaScript(js);
    }

    @SuppressWarnings("unchecked")
    private List <WebElement> executeJavaScript (String js) {
        JavascriptExecutor executor = (JavascriptExecutor) this.driver;
        return (List <WebElement>) executor.executeScript(js);
    }

    private void init (String url) throws Exception {
        this.driver = new ChromeDriver();
        driver.get(url);
        driver.manage().window().maximize();
        this.setMutationObserved();
        List <WebElement> activators = driver.findElements(By.cssSelector("body *"));
        for (int i = 0; i < activators.size(); i++) {
            WebElement activator = activators.get(i);
            List <WebElement> mutations = null;
            if (activator.getSize().getWidth() < 300 && activator.getSize().getHeight() < 100) {
                this.mouseMove(activator);
                mutations = this.getMutations();
                if (mutations != null && mutations.size() != 0) {
                    this.saveScreenshot(activator, mutations, i);
                }
            }
        }
        driver.quit();
    }

    private void saveScreenshot (WebElement activator, List <WebElement> mutations, int index) throws Exception {
        BufferedImage image = this.takeScreenshot();
        this.save_target_screenshot(image, activator, 0, "widget" + index);
        for (int i = 0; i < mutations.size(); i++) {
            WebElement mutation = mutations.get(i);
            this.save_target_screenshot(image, mutation, i + 1, "widget" + index);
        }
    }

    private BufferedImage takeScreenshot () {
        Screenshot ashot_screenshot = new AShot().shootingStrategy(
                ShootingStrategies.viewportPasting(500)).takeScreenshot(this.driver);
        return ashot_screenshot.getImage();
    }

    public void save_target_screenshot (BufferedImage full_image, WebElement target,
                                        int element_index, String filename) throws Exception {
        BufferedImage sub_image = null;
        int left = target.getLocation().getX(),
            top = target.getLocation().getY(),
            height = target.getSize().getHeight(),
            width = target.getSize().getWidth();
        if (top < 0) {
            height = height + top;
            top = 0;
        }
        if (left < 0) {
            width = width + left;
            left = 0;
        }
        if (top >= full_image.getHeight())
            top = full_image.getHeight() - 2;
        if (left >= full_image.getWidth())
            left = full_image.getWidth() - 2;
        if (top + height >= full_image.getHeight())
            height = full_image.getHeight() - top - 1;
        if (left + width >= full_image.getWidth())
            width = full_image.getWidth() - left - 1;
        sub_image = full_image.getSubimage(
                left, top,
                (width <= 0 ? 1 : width),
                (height <= 0 ? 1 : height));
        File file = new File(this.folder + filename + "." + element_index + ".element.png");
        ImageIO.write(sub_image, "png", file);
    }


    private void mouseMove (WebElement target) throws Exception {
        if (target.isDisplayed()) {
            Actions actions = new Actions(this.driver);
            actions.moveToElement(target, 1, 1)
                   .build()
                   .perform();
            Thread.sleep(500);
        }
    }

    public static void main (String[] args) throws Exception {
        String url = args[0];
        App app = new App();
        app.init(url);
    }
}
