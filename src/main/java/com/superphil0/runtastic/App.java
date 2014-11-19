package com.superphil0.runtastic;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.superphil0.runtasticapi.Activity;
import com.superphil0.runtasticapi.RequestHandler;

/**
 * Hello world!
 *
 */
public class App 
{
	private static OkHttpClient client;
	private static String email = "EMAIl";
	private static String pwRuntastic = "PASSWORD";
	private static String pwLaufliga = "PASSWORD";
	
    public static void main( String[] args )
    {
    	RequestHandler handler = new RequestHandler();
    	List<Activity> activities = handler.getActivitiesOfUser(email,pwRuntastic);
    	client = new OkHttpClient();
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		client.setCookieHandler(cookieManager);
		RequestBody body = new FormEncodingBuilder().add("j_username", email)
				.add("j_password", pwLaufliga).build();
		Request request = new Request.Builder().url("http://www.laufliga.net/Laufen/j_spring_security_check").post(body)
				.build();
		
		Response response = run(request);
		request = new Request.Builder()
		.url("http://www.laufliga.net/Trainingsbereich")
		.get()
		.build();
		
		String bodyStr = null;
		try {
			bodyStr = response.body().string();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Document document = Jsoup.parse(bodyStr);
		String token = document.select("input[id=j_id1:javax.faces.ViewState:0]").first().val();
		for(Activity a : activities)
		{
			if(Integer.parseInt(a.getDistance()) > 4000)
				saveActivityToLaufliga(a, token);
		}
		// text/html,application/xhtml+xml,application/xml;
    	
    }
    private static void saveActivityToLaufliga(Activity activity, String token)
    {
    	Date d = activity.getDate().getDate();
    	d.setYear(d.getYear()-1900);
    	d.setMonth(d.getMonth()-1);
    	DateTime date = new DateTime(d);
    	DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.YYYY");
    	long duration = Long.parseLong(activity.getDuration());
    	String dateStr = dateFormat.print(date);
    	Period period = new Duration(duration).toPeriod();
    	PeriodFormatter dur = new PeriodFormatterBuilder()
    	     .printZeroAlways()
    	     .minimumPrintedDigits(2)
    	     .appendHours()
    	     .appendSeparator(":")
    	     .appendMinutes()
    	     .appendSeparator(":")
    	     .appendSeconds()
    	     .toFormatter();
    	String durationStr = dur.print(period);
		RequestBody body = new FormEncodingBuilder()
			.add("Trainingseinheit:Eintrag", "Trainingseinheit:Eintrag")
			.add("Trainingseinheit:Eintrag:Datum",dateStr)
			.add("Trainingseinheit:Eintrag:Distanz", activity.getDistance())
			.add("Trainingseinheit:Eintrag:Zeit", durationStr)
			.add("Trainingseinheit:Eintrag:j_idt63", "Strasse")
			.add("Trainingseinheit:Eintrag:Ort", "test")
			.add("Trainingseinheit:Eintrag:j_idt67", "gut")
			.add("Trainingseinheit:Eintrag:button1", "Speichern")
			.add("javax.faces.ViewState", token)
			.build();
		System.out.println(dateStr + " : "
			+ activity.getDistance() + " : "+ durationStr);
		Request request = new Request.Builder().url("http://www.laufliga.net/Trainingsbereich").post(body)
				.build();
		 Response response = run(request);
    }
	public static Response run(Request request) {
		Response response = null;
		try {
			response = client.newCall(request).execute();
			return response;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
