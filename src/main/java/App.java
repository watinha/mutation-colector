import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class App {

    private void init (String url) throws Exception {
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        driver.manage().window().maximize();
        List <WebElement> activators = driver.findElements(By.cssSelector("body *"));
        for (int i = 0; i < activators.size(); i++) {
            WebElement activator = activators.get(i);
            Actions actions = new Actions(driver);
            actions.moveToElement(activator).build().perform();
            Thread.sleep(500);
        }
        driver.quit();
    }

    public static void main (String[] args) throws Exception {
        String url = args[0];
        App app = new App();
        app.init(url);
    }
}
