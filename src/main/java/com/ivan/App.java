package com.ivan;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static final String GROSS_TARGET_REGEX = ".*\\|.*\\|.*\\|.*\\d+\\.\\d+[ ]*(\\d+\\.\\d+)[ ]*(\\d+\\.\\d+)";
    public static final String CARRY_FORWARD_REGEX = ".*Carry[ ]*Forward.*Period[ ]*([+-]\\d+\\.\\d+)";

    public static final Map<RegexType, Pattern> regexMap = new HashMap<>();

    enum RegexType {
        GROSS_TARGET_REGEX, CARRY_FORWARD_REGEX
    }

    static {
        regexMap.put(RegexType.GROSS_TARGET_REGEX, Pattern.compile(GROSS_TARGET_REGEX));
        regexMap.put(RegexType.CARRY_FORWARD_REGEX, Pattern.compile(CARRY_FORWARD_REGEX));
    }

    public static void main(String[] args) {

        App app = new App();

        System.out.println(
                "NOTE:\n" +
                        "  1. Your username / password isn't stored anywhere.\n" +
                        "  2. If your password isn't correct the program will crash.\n" +
                        "     Relaunch the program and try again.\n" +
                        "  3. The time statistic is shown at the bottom of the report page.\n" +
                        "  4. If you note any errors, please, send it at: 'ivan.gandacov@cmg.com'.\n" +
                        "  5. The program computes the result by adding all the differences between\n" +
                        "     GROSS and TARGET. CARRY FORWARD records are also taken into account.\n\n");

        String username = System.getProperty("user.name") + "@cgm.com";

        System.out.println("Username: " + username);
        Console console = System.console();
        String password = new String(console.readPassword("Password: "));

        System.setProperty("webdriver.chrome.driver", "./chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

        driver.get("https://int.zeus.cgm.ag/");
        driver.findElement(By.id("uiFldUsername")).sendKeys(username);

        driver.findElement(By.id("uiFldPassword")).sendKeys(password);

        driver.findElement(By.id("uiBtnLogin")).click();

        WebElement fmeNavigation = driver.findElement(By.id("FmeNavigation"));
        driver.switchTo().frame(fmeNavigation);

        WebElement radPanelbar1Row7 = driver.findElement(By.id("RadPanelbar1Row7"));
        radPanelbar1Row7.click();

        WebElement radPanelbar1Row8 = driver.findElement(By.id("RadPanelbar1Row8"));
        radPanelbar1Row8.findElement(By.xpath(".//a[1]")).click();

        driver.switchTo().parentFrame();
        WebElement fmeContent = driver.findElement(By.id("FmeContent"));
        driver.switchTo().frame(fmeContent);

        WebElement frmReportContent = driver.findElement(By.id("FrmReportContent"));
        driver.switchTo().frame(frmReportContent);

        WebElement uiReport = driver.findElement(By.id("uiReport"));

        InputStream stream = new ByteArrayInputStream(uiReport.getText().getBytes());
        Time time = app.getTimeStatistic(stream);

        String color = (time.getHours() < 0 || time.getMinutes() < 0) ? "red" : "green";

        ((ChromeDriver) driver).executeScript(
                "var para = document.createElement('P');" +
                        "var timeTextNode = document.createTextNode(" +
                        "'Time offset (from normal): " + ((time.getHours() > 0) ? "+" : "") + time.getHours() +
                        "h " + time.getMinutes() + "m');" +
                        "para.style.color = '" + color + "';" +
                        "para.appendChild(timeTextNode);" +
                        "arguments[0].appendChild(para);",
                uiReport);

        console.readLine("Press any key to continue...");
    }

    public void test() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream zeusStream = classloader.getResourceAsStream("test.txt");

        Time statistic = getTimeStatistic(zeusStream);
        System.out.println("Hours: " + statistic.getHours());
        System.out.println("Mins: " + statistic.getMinutes());
    }


    public Time getTimeStatistic(InputStream zeusStream) {

        int hours = 0;
        int minutes = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(zeusStream))) {

            String line;
            while ((line = br.readLine()) != null) {

                TimeInfo timeInfo;

                timeInfo = getTimeInfo(RegexType.CARRY_FORWARD_REGEX, line);

                if (timeInfo != null) {
                    hours += timeInfo.getCarryForward().getHours();
                    minutes += timeInfo.getCarryForward().getMinutes();
                    System.out.println("NOTE: You've got a CARRY FORWARD PERIOD of " +
                            timeInfo.getCarryForward().getHours() + "h " +
                            timeInfo.getCarryForward().getMinutes() + "m.");
                }

                timeInfo = getTimeInfo(RegexType.GROSS_TARGET_REGEX, line);

                if (timeInfo != null) {

                    int grossHours = timeInfo.getGross().getHours();
                    int grossMinutes = timeInfo.getGross().getMinutes();
                    int targetHours = timeInfo.getTarget().getHours();
                    int targetMinutes = timeInfo.getTarget().getMinutes();
                    int diffMinutes = 0, diffHours = 0;

                    if (grossHours < targetHours) {
                        if (targetMinutes < grossMinutes) {
                            targetMinutes += 60;
                            targetHours -= 1;
                        }
                    } else if (grossHours > targetHours) {
                        if (grossMinutes < targetMinutes) {
                            grossMinutes += 60;
                            grossHours -= 1;
                        }
                    }

                    diffMinutes = grossMinutes - targetMinutes;
                    diffHours = grossHours - targetHours;

                    minutes += diffMinutes;
                    hours += diffHours;
                }

                hours += minutes / 60;
                minutes = minutes % 60;
            }


            zeusStream.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return new Time(hours, minutes);
    }

    private TimeInfo getTimeInfo(RegexType regexType, String line) {

        TimeInfo result = null;
        Pattern pattern = regexMap.get(regexType);
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) return null;

        if (regexType == RegexType.CARRY_FORWARD_REGEX) {
            return new TimeInfo(matcher.group(1));
        } else if (regexType == RegexType.GROSS_TARGET_REGEX) {
            return new TimeInfo(matcher.group(1), matcher.group(2));
        }

        return result;
    }

}
