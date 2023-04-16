package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static ArrayList<String> readCSVFile(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true; // flag to skip the first line
            while ((line = br.readLine()) != null) {
                if (!firstLine) {
                    lines.add(line);
                }
                firstLine = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void writeLineToCSV(String fileName, String line) {
        try {
            // Create a FileWriter object with append mode set to true
            FileWriter fileWriter = new FileWriter(fileName, true);

            // Write the line to the CSV file
            fileWriter.write(line + "\n");

            // Close the FileWriter
            fileWriter.close();

            System.out.println("Line written to CSV file: " + line);
        } catch (IOException e) {
            System.err.println("Failed to write line to CSV file: " + e.getMessage());
        }
    }


    public static String getSubstringBeforeSecondToLastUnderscore(String input) {
        int secondToLastUnderscoreIndex = input.lastIndexOf('_', input.lastIndexOf('_') - 1);
        if (secondToLastUnderscoreIndex != -1) {
            return input.substring(0, secondToLastUnderscoreIndex);
        } else {
            return input;
        }
    }



    public static void download_fdroid_from_url(WebDriver d, String url) {

    }


    public static void main(String[] args) throws IOException, InterruptedException {


        ArrayList<String> project_lines = readCSVFile("App.csv");


        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--remote-allow-origins=*");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        writeLineToCSV("Results.csv", "app_name,fdroid_link,package_name,fdroid_tar,fdroid_lastupdate,github_url,github_lastupdate,github_tags,github_forks,github_stars,github_watching,playstore_url,playstore_lastupdate,playstore_stars,playstore_downloads");

        for (String line : project_lines) {




            System.setProperty("webdriver.chrome.driver", "C:\\Users\\ricca\\Documents\\chromedriver.exe");

            System.out.println("Hello world!");
            //String app_name = "ParanoidWallpapers"; //debug
            String app_name = line.split(",")[0];
            //String base_url = "https://f-droid.org/it/packages/com.paranoid.ParanoidWallpapers/"; //debug
            String base_url = line.split(",")[1];


            driver.get(base_url);
            Thread.sleep(1000);


            WebElement download_link = driver.findElements(By.linkText("questa tarball del codice sorgente")).get(0);

            // Ottieni l'URL del file dal link
            String fileUrl = download_link.getAttribute("href");

            // Scarica il file dal server
            URL tar_gz_url = new URL(fileUrl);

            String file_name = tar_gz_url.toString().split("https://f-droid.org/repo/")[1];
            System.out.println(file_name);
            String package_name = getSubstringBeforeSecondToLastUnderscore(file_name);
            System.out.println(package_name);


            //download the app

            /*URLConnection conn = tar_gz_url.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            File newFile = new File("savedtars\\" + file_name);
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newFile, false);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            bis.close();*/



            WebElement versionheader = driver.findElements(By.className("package-version-header")).get(0);

            String lastupdate_fdroid =  versionheader.getText().split("Aggiunto il ")[1];
            System.out.println("Last update fdroid: " + lastupdate_fdroid);



            //search the package name on github. return all the occurrencies in a list


            driver.get("https://github.com/");
            WebElement searchBox = driver.findElement(By.name("q"));
            searchBox.sendKeys(app_name);
            searchBox.submit();
            Thread.sleep(1000);

            List<WebElement> items_found_github = driver.findElements(By.className("repo-list-item"));

            ArrayList<String> github_hrefs = new ArrayList<String>();

            String right_url = null;
            String github_stars ="", github_watching="", github_forks="", github_latest_version_date="", github_tags ="";

            for (WebElement item : items_found_github) {
                github_hrefs.add(item.findElement(By.className("v-align-middle")).getAttribute("href"));
            }

            for (String href : github_hrefs) {

                //TODO: for now this only searches the first page. shall we change t to cover also other pages?
                driver.get(href);
                Thread.sleep(1000);

                searchBox = driver.findElement(By.name("q"));
                searchBox.sendKeys(package_name);
                searchBox.submit();

                Thread.sleep(1000);


                if (driver.getPageSource().contains("We couldn’t find any code matching")) {
                    System.out.println("package name not found here");
                }
                else {
                    right_url = href;
                    break;
                }
            }

            if (right_url == null) {
                System.out.println("not found on github");
            }
            else {
                System.out.println("found on github at: " + right_url);
                driver.get(right_url);
                Thread.sleep(1000);

                // Trova l'elemento che contiene la data dell'ultima versione
                WebElement latestVersionDate = driver.findElement(By.tagName("relative-time"));


                // Trova l'elemento che contiene il numero di stelle
                //verificare come funziona poi quando ci sono + stelle o quando il numero di watching è 0
                WebElement stars = driver.findElement(By.xpath("//*[contains(@href, 'stargazers')]")).findElement(By.className("text-bold"));

                // Trova l'elemento che contiene il numero di "watching"
                WebElement watching = driver.findElement(By.xpath("//a[contains(@href,'/watchers')]"));

                // Trova l'elemento che contiene il numero di fork
                WebElement forks = driver.findElement(By.xpath("//*[contains(@href, 'forks')]")).findElement(By.className("text-bold"));

                WebElement tags = driver.findElements(By.xpath("//*[contains(@href, 'tags')]")).get(1);

                // Stampa le informazioni trovate

                if (latestVersionDate == null) {System.out.println("latest version date null");}
                else {
                  //  System.out.println("Last github version: " + latestVersionDate.getAttribute("datetime"));
                    github_latest_version_date = latestVersionDate.getAttribute("datetime");
                }

                if (stars == null) { System.out.println("stars null"); }
                else {
                    github_stars = stars.getAttribute("outerHTML").split("<span class=\"text-bold\">")[1].split("</span>")[0];
                 //   System.out.println("Github stars: " +  stars.getAttribute("outerHTML").split("<span class=\"text-bold\">")[1].split("</span>")[0]);
                }

                if (watching == null) { System.out.println("watching null"); }
                else {
                    github_watching = watching.getText().split(" watching")[0];
                  //  System.out.println("Github watching: " + watching.getText().split(" watching")[0]);

                }


                if (tags == null) { System.out.println("tags null"); }
                else {
                    github_tags = tags.getText();
                 //   System.out.println("Github tags: " + tags.getAttribute("outerHTML"));

                }

                if (forks == null) { System.out.println("forks null"); }
                else {
                    github_forks = forks.getAttribute("outerHTML").split("<span class=\"text-bold\">")[1].split("</span>")[0];
                //    System.out.println("Github forks: " + forks.getAttribute("outerHTML").split("<span class=\"text-bold\">")[1].split("</span>")[0]);
                }


            }

            //search the package name on playstore. return all the occurrencies in a list


            String play_store_base_url = "https://play.google.com/store/apps/details?id=";

            //package_name = "com.zorinos.zorin_connect"; //debug
            driver.get(play_store_base_url + package_name);

            Thread.sleep(1000);

            String playstore_rating="", playstore_downloads="", playstore_latest_release="";
            if (driver.getPageSource().contains("Spiacenti")) {
                System.out.println("not found on playstore");
                play_store_base_url="";
            }
            else {
                WebElement rating = driver.findElements(By.className("wVqUob")).get(0).findElement(By.className("ClM7O"));
                WebElement downloads = driver.findElements(By.className("wVqUob")).get(1).findElement(By.className("ClM7O"));
                WebElement lastupdate = driver.findElements(By.className("TKjAsc")).get(0);

                System.out.println("PlayStore rating: " + rating.getText());
                playstore_rating = rating.getText();
                System.out.println("PlayStore downloads: " + downloads.getText());
                playstore_downloads = downloads.getText();
                System.out.println("PlayStore last update: " + lastupdate.getText());
                playstore_latest_release = lastupdate.getText();

                playstore_latest_release = playstore_latest_release.split("\\r?\\n")[1];
                playstore_rating = playstore_rating.split("\\r?\\n")[0].replace(",",".");
                playstore_downloads = playstore_downloads.replace(".", "");
            }




            System.out.println("RECAP");
            System.out.println("App name: " + app_name);
            System.out.println("Package name: " + package_name);
            System.out.println("Url of F-droid tar: " + tar_gz_url);
            //System.out.println("F-droid category: ");
            System.out.println("Last update on F-droid: " + lastupdate_fdroid);
            System.out.println("Github URL: "  + right_url);
            System.out.println("Github last update: " + github_latest_version_date);
            System.out.println("Github tags: " + github_tags.split(" ")[0]);
            System.out.println("Github forks: " + github_forks);
            System.out.println("Github stars: " + github_stars);
            System.out.println("Github watching: " + github_watching);
            System.out.println("Playstore URL: " + play_store_base_url + package_name);

            System.out.println("Playstore last update: " + playstore_latest_release);
            System.out.println("Playstore stars: " +  playstore_rating);
            System.out.println("Playstore downloads: " + playstore_downloads);


            writeLineToCSV("Results.csv", app_name + "," + base_url + "," + package_name + "," + tar_gz_url + "," +
                    lastupdate_fdroid + "," + right_url + "," + github_latest_version_date + "," + github_tags.split(" ")[0] + "," +
                    github_forks + "," + github_stars + "," + github_watching + "," + play_store_base_url + package_name + "," +
                    playstore_latest_release + "," + ((!playstore_downloads.equals("")) ? playstore_rating : "") + "," + ((!playstore_downloads.equals("")) ? playstore_downloads : playstore_rating));

        }

        driver.close();

    }
}