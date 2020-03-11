package edu.utep.cs.cs4330.firebaseproject;

import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceFinder {
    private double randomDouble;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    public double getNewPrice(String urlOfItem) throws IOException{
        //randomDouble = Math.random() * 1000 + 1;
        //return randomDouble;

        //String a = getURLSource("https://www.amazon.com/dp/B07D4F2P26/ref_=fs_ods_fs_smp_re");
        //String a = getURLSource("https://www.walmart.com/ip/Sceptre-32-Class-HD-720P-LED-TV-X322BV-SR/55427159?athcpid=55427159&athpgid=athenaItemPage&athcgid=null&athznid=PWVUB&athieid=v0&athstid=CS020&athguid=466001f5-8a60d8f5-d33bc09a320c883&athena=true");
        //String a = getURLSource("http://www.cs.utep.edu/cheon/cs4330/homework/hw3");
        String a = getURLSource(urlOfItem);

        if (!a.equals("")) {
            System.out.println("Item price on the given URL is: "+a);
            return Double.parseDouble(a.substring(1));
        }else {
            System.out.println("Seller configuration or pattern not matched!");
            return 0;
        }
    }
    public String calculateChange(double np, double op){
        String outPut;
        double p;
        if (np < op){
            p = (op - np)/op*100;
            outPut = "Price dropped "+df2.format(p)+"%";
        }else{
            p = (np - op)/op*100;
            outPut = "Price increased "+df2.format(p)+"%";
        }
        return outPut;
    }

    public static String getURLSource(String url) throws IOException{
        String seller ="";
        if(url.contains("amazon.com")) {seller="Amazon";}
        else if(url.contains("walmart.com")) {seller="Walmart";}
        else {seller="Demo";}
        URL urlObject = new URL(url);
        URLConnection urlConnection = urlObject.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        return toString(seller, urlConnection.getInputStream());
    }

    private static String toString(String seller, InputStream itemURL) throws IOException{
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(itemURL, "UTF-8"))){
            String inputLine, finalPrice="", startWith="", endWith="", price="";
            StringBuilder stringBuilder = new StringBuilder();

            //Amazon pattern
            if(seller.equals("Amazon")) {
                //<span id="priceblock_ourprice" class="a-size-medium a-color-price">$119.99</span>
                startWith= "<span id=\"priceblock_ourprice\" class=\"a-size-medium a-color-price\">";
                endWith = "</span>";
                price = "[\\$]\\d*[\\.]\\d{2}?";
                System.out.println("looking for Amazon item price...");
            }
            else if(seller.equals("Walmart")) {
                //aria-label=\"Your price for this item is $349.99\">$
                startWith="<span class=\"price-group\" role=\"text\" aria-label=\"";
                endWith="\">";
                price = "[\\$]\\d*[\\.]\\d{2}?";
                System.out.println("looking for Walmart item price...");
            }
            else if(seller.equals("Demo")){
                System.out.println("looking for Demo item price...");
                while ((inputLine = bufferedReader.readLine()) != null){stringBuilder.append(inputLine);}
                String finalPage = stringBuilder.toString();
                String pattern = "[\\$]\\d*[\\.]\\d{2}?";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(finalPage);
                if (m.find()){
                    System.out.println("Pattern Matched:\nExtracting price..." );
                    return m.group(0);//removing $ sign as well.
                }
                else {
                    return "0.00";
                }
            }else {return "0";}

            Pattern finalPattern = Pattern.compile(startWith+"(.*?)"+endWith);

            while ((inputLine = bufferedReader.readLine()) != null){
                stringBuilder.append(inputLine);
                Matcher match = finalPattern.matcher(inputLine);

                if (match.find()){
                    System.out.println("Pattern Matched: " + match.group(0)+ "\nExtracting price..." );
                    //extract price
                    Pattern pricePattern = Pattern.compile(price);
                    Matcher matchPrice = pricePattern.matcher(match.group(0));
                    if (matchPrice.find()) {
                        finalPrice = matchPrice.group(0);
                        break;//as soon as price is found, break.
                    }
                }else{
                    continue;
                }
            }
            return finalPrice;//removing $ sign as well.
        }
    }
}
