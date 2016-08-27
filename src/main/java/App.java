import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class App {

    private WebDriver driver;
    private String folder = "data/";
    private String menuFolder = "data/menus/";
    private int websiteIndex = 0;
    private PrintWriter resultsWriter;

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

    private void search (String url, String cssSelector) throws Exception {
        int i;
        this.driver = new ChromeDriver();
        //this.driver = new FirefoxDriver();
        driver.get(url);
        driver.manage().window().maximize();
        this.setMutationObserved();
        List <WebElement> activators = new ArrayList <WebElement> ();
        String [] cssSelectors = cssSelector.split(",");
        for (int j = 0; j < cssSelectors.length; j++) {
            activators.addAll(driver.findElements(By.cssSelector(cssSelectors[j])));
        }
        for (i = 0; i < activators.size(); i++) {
            WebElement activator = activators.get(i);
            List <WebElement> mutations = null;
            this.mouseMove(activator);
            mutations = this.getMutations();
            if (mutations != null && mutations.size() != 0) {
                this.saveScreenshot(activator, mutations, this.websiteIndex + i);
            }
        }
        this.websiteIndex += i;
        driver.quit();
    }

    private void saveScreenshot (WebElement activator, List <WebElement> mutations, int index) throws Exception {
        List <WebElement> mutationCache = new ArrayList <WebElement> ();
        BufferedImage image = this.takeScreenshot();
        //this.save_target_screenshot(image, activator, 0, "widget" + index);
        for (int i = 0; i < mutations.size(); i++) {
            WebElement mutation = mutations.get(i);
            if (!mutationCache.contains(mutation)) {
                try {
                    int displayed = mutation.isDisplayed() ? 1 : 0,
                        height = mutation.getSize().getHeight(),
                        width = mutation.getSize().getWidth(),
                        top = mutation.getLocation().getY(),
                        left = mutation.getLocation().getX(),
                        activatorTop = activator.getLocation().getY(),
                        activatorLeft = activator.getLocation().getX(),
                        distanceTop = activatorTop - top,
                        distanceLeft = activatorLeft - left,
                        numberElements = mutation.findElements(By.cssSelector("*")).size();
                    this.resultsWriter.println(index + "," + i + "," + displayed + "," + height + "," + width + "," +
                                               top + "," + left + "," + activatorTop + "," + activatorLeft + "," +
                                               distanceTop + "," + distanceLeft + "," + numberElements);
                    this.save_target_screenshot(image, mutation, i, "widget" + index);
                    mutationCache.add(mutation);
                } catch (StaleElementReferenceException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public App (String resultsFileName) throws Exception {
        File resultsFile = new File(resultsFileName);
        this.resultsWriter = new PrintWriter(resultsFile);
        this.resultsWriter.println("activator-id,mutation-id,displayed,height,width,top,left,activatorTop,activatorLeft,distanceTop,distanceLeft,numberElements");
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
        if (target.isDisplayed() && target.getSize().getWidth() < 300 && target.getSize().getHeight() < 100) {
            Actions actions = new Actions(this.driver);
            actions.moveToElement(target)
                   .build()
                   .perform();
            Thread.sleep(500);
        }
    }

    public void close () throws Exception {
        this.resultsWriter.close();
    }

    public static void main (String[] args) throws Exception {
        App app = new App("results");
        BufferedReader br = new BufferedReader(new FileReader("url_list.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String url = line.substring(0, line.indexOf(' ')),
                   cssSelector = line.substring(line.indexOf(' ') + 1);
            app.search(url, cssSelector);
        }
        app.close();
        br.close();
    }
}
