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

    private int getWordsTextNodes (WebElement target) {
        String js = this.readResource("get_average_words_textnodes.js");
        return (int) this.executeJavaScript(js, target);
    }

    private int getNumberOfTextNodes (WebElement target) {
        String js = this.readResource("get_number_of_textnodes.js");
        return (int) this.executeJavaScript(js, target);
    }

    private int presenceOfWidgetInClass (WebElement target) {
        String js = this.readResource("get_presence_of_widget_in_class.js");
        return (int) this.executeJavaScript(js, target);
    }

    private int presenceOfDateInType (WebElement target) {
        String js = this.readResource("get_presence_of_date_in_type.js");
        return (int) this.executeJavaScript(js, target);
    }

    private int tableUl80Percent (WebElement target) {
        String js = this.readResource("table_ul_80_percent_present.js");
        return (int) this.executeJavaScript(js, target);
    }

    private int proportionOfTextNodesNumber (WebElement target) {
        String js = this.readResource("get_proportion_text_nodes_numbers.js");
        return (int) this.executeJavaScript(js, target);
    }

    @SuppressWarnings("unchecked")
    private int executeJavaScript (String js, WebElement target) {
        JavascriptExecutor executor = (JavascriptExecutor) this.driver;
        return ((Long) executor.executeScript(js, target)).intValue();
    }

    private void search (String url, String cssSelector) throws Exception {
        int i;
        this.driver = new ChromeDriver();
        //this.driver = new FirefoxDriver();
        this.resultsWriter.println(url);
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
                this.saveMutations(activator, mutations, this.websiteIndex + i);
            }
        }
        this.websiteIndex += i;
        driver.quit();
    }

    private void saveMutations (WebElement activator, List <WebElement> mutations, int index) throws Exception {
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
                        activatorHeight = activator.getSize().getHeight(),
                        activatorWidth = activator.getSize().getWidth(),
                        distanceTop = (activatorTop + activatorHeight) - top,
                        distanceLeft = (activatorLeft + activatorWidth) - left,
                        numberElements = mutation.findElements(By.cssSelector("*")).size(),
                        numberOfWordsTextNodes = this.getWordsTextNodes(mutation),
                        numberOfTextNodes = this.getNumberOfTextNodes(mutation),
                        tablePresent = mutation.findElements(By.cssSelector("table")).size() > 0 ? 1: 0,
                        listPresent = mutation.findElements(By.cssSelector("ul")).size() > 0 ? 1: 0,
                        inputPresent = mutation.findElements(By.cssSelector("input")).size() > 0 ? 1: 0,
                        widgetNamePresent = this.presenceOfWidgetInClass(mutation),
                        datePresent = this.presenceOfDateInType(mutation),
                        imgPresent = mutation.findElements(By.cssSelector("img")).size() > 0 ? 1: 0,
                        links80percent = this.tableUl80Percent(mutation),
                        proportionNumbersTextNodes = this.proportionOfTextNodesNumber(mutation);
                    float proportionNumbers  = (numberOfTextNodes == 0 ? 0 : proportionNumbersTextNodes/numberOfTextNodes);
                    this.resultsWriter.println(index + "," + i + "," + displayed + "," + height + "," + width + "," +
                                               top + "," + left + "," + activatorTop + "," + activatorLeft + "," +
                                               distanceTop + "," + distanceLeft + "," + numberElements + "," + numberOfWordsTextNodes + "," +
                                               numberOfTextNodes + "," +
                                               tablePresent + "," + listPresent + "," + inputPresent + "," +
                                               widgetNamePresent + "," + datePresent + "," + imgPresent + "," +
                                               proportionNumbers + "," + links80percent);
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
        this.resultsWriter.println("activator-id,mutation-id,displayed,height,width,top,left,activatorTop,activatorLeft,distanceTop,distanceLeft,numberElements,numberWords," +
                                   "textNodes,table,list,input,widgetName,date,img,proportionNumbers,links80percent");
    }


    private BufferedImage takeScreenshot () {
        Screenshot ashot_screenshot = new AShot()
            //.shootingStrategy(ShootingStrategies.viewportPasting(500))
            .takeScreenshot(this.driver);
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
            Thread.sleep(3000);
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
