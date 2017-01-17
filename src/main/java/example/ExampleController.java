package example;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class ExampleController {

	private static final String DEFAULT_CHARSET = "UTF-8";
	private List<Restaurant> restaurants;
	private String restaurantsAsJson;

	ExampleController() throws IOException, URISyntaxException {

		restaurants = new ArrayList<Restaurant>();

		CellProcessor[] processors = new CellProcessor[] { new ParseDouble(), new ParseDouble(), new NotNull(),
				new NotNull() };
			
		URL url = this.getClass().getClassLoader().getResource("data/locations (2).csv");				
		File file = new File(url.toURI());
		
		try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(file),
				CsvPreference.STANDARD_PREFERENCE)) {

			final String[] header = beanReader.getHeader(true);
			Restaurant bean;

			while ((bean = beanReader.read(Restaurant.class, header, processors)) != null) {
				System.out.println(String.format("lineNo=%s, rowNo=%s, restaurant=%s", beanReader.getLineNumber(),
						beanReader.getRowNumber(), bean));
				restaurants.add(bean);
			}
		}

		restaurants = MySort(restaurants, RestaurantTitleComparator.DEFAULT);
		
		ObjectMapper mapper = new ObjectMapper();
		restaurantsAsJson = mapper.writeValueAsString(restaurants);
	}

	@GetMapping("/example")
	public String exampleGetRequest(Model model) throws MalformedURLException, IOException {

		RequestData requestData = new RequestData();
		requestData.setUrl("http://httpbin.org/post");
		model.addAttribute("restaurants", restaurants);
		model.addAttribute("requestData", requestData);
		model.addAttribute("code", "");
		model.addAttribute("response", "");

		return "example";
	}

	@PostMapping(path = "/example", params = "get")
	public String exampleUsingGet(@ModelAttribute RequestData requestData, Model model)
			throws MalformedURLException, IOException {

		String query = String.format("restaurants=%s", URLEncoder.encode(restaurantsAsJson, DEFAULT_CHARSET));
		HttpURLConnection connection = (HttpURLConnection) new URL(requestData.getUrl() + "?" + query).openConnection();
		connection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);

		StringBuffer buffer = new StringBuffer();

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream is = connection.getInputStream();

			try (Scanner scanner = new Scanner(is)) {
				buffer.append(scanner.useDelimiter("\\A").next());
			}
		}

		model.addAttribute("restaurants", restaurants);
		model.addAttribute("requestData", requestData);
		model.addAttribute("code", connection.getResponseCode());
		model.addAttribute("response", buffer.toString());

		return "example";
	}

	@PostMapping(path = "/example", params = "post")
	public String exampleUsingPost(@ModelAttribute RequestData requestData, Model model)
			throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(requestData.getUrl()).openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);
		connection.setRequestProperty("Content-Type", "application/json;charset=" + DEFAULT_CHARSET);

		try (OutputStream output = connection.getOutputStream()) {
			output.write(restaurantsAsJson.getBytes(DEFAULT_CHARSET));
		}

		StringBuffer buffer = new StringBuffer();

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream is = connection.getInputStream();

			try (Scanner scanner = new Scanner(is)) {
				buffer.append(scanner.useDelimiter("\\A").next());
			}
		}

		model.addAttribute("restaurants", restaurants);
		model.addAttribute("requestData", requestData);
		model.addAttribute("code", connection.getResponseCode());
		model.addAttribute("response", buffer.toString());

		return "example";
	}

	private static <T> List<T> MySort(List<T> l, Comparator<T> cmp) {

		if (l.size() <= 1) {
			return l;
		}

		final T first = l.get(0);
		final List<T> rest = l.subList(1, l.size());

		Stream<T> lessThanList = rest.stream().filter(it -> cmp.compare(it, first) < 0);
		Stream<T> notLessThanList = rest.stream().filter(it -> cmp.compare(it, first) >= 0);

		List<T> concat = new ArrayList<T>(l.size());
		concat.addAll(MySort(lessThanList.collect(Collectors.toList()), cmp));
		concat.add(first);
		concat.addAll(MySort(notLessThanList.collect(Collectors.toList()), cmp));
		return concat;
	}

}
