package com.recommender.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * This class handles interaction with the lucene framework.
 * 
 * @author ashwinvinod
 *
 */
public class LuceneHelper
{

	/**
	 * 1. fix correctness of query
	 * 2. add dropdown to columns and make ajax call to populate columns - should query index 
	 * 3. add priorities to runQuery - call boost(). verify correctness of recommendations.
	 */
	
	private static Map<String, Float> propertyName2priority = new HashMap<String, Float>();
	
	// 0. Specify the analyzer for tokenizing text.
	// The same analyzer should be used for indexing and searching
	private static StandardAnalyzer analyzer = new StandardAnalyzer();

	// 1. create the index
	private static Directory index = new RAMDirectory();
	private static IndexReader reader = null;

	static
	{
		//TODO priorities could be configured in properties file
		propertyName2priority.put("currentTeam", 4f);
		propertyName2priority.put("graduateInstitute", 3f);
		propertyName2priority.put("postGraduateInstitute", 3f);
		propertyName2priority.put("graduateYear", 1f);
		propertyName2priority.put("postGraduateYear", 2f);
	}
	
	public static void buildIndex(List<Employee> employees) throws Exception
	{

		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		IndexWriter w = new IndexWriter(index, config);

		for (Employee employee : employees)
		{
			Map<String, String> propertyName2PropertyValue = new HashMap<String, String>();
			java.lang.reflect.Field[] fields = Employee.class.getDeclaredFields();
			for (java.lang.reflect.Field field : fields)
			{
				field.setAccessible(true);
				String name = field.getName();
				Object propertyValue = field.get(employee);
				String value = propertyValue == null ? "" : (String) propertyValue;
				propertyName2PropertyValue.put(name, value);
			}
			addEmployee(w, propertyName2PropertyValue);
		}
		w.close();
		reader = DirectoryReader.open(index);
	}

	// TODO use string field(not text field) for properties that should not be
	// tokenized.
	private static void addEmployee(IndexWriter w, Map<String, String> propertyName2PropertyValue) throws IOException
	{
		Document doc = new Document();
		for (Entry<String, String> property : propertyName2PropertyValue.entrySet())
		{
			try
			{
				TextField field = new TextField(property.getKey(), property.getValue(), Field.Store.YES);
				field.setBoost(getBoost(property.getKey()));
				doc.add(field);
			}
			catch (Exception ex)
			{
				System.out.println(ex);
			}
		}
		w.addDocument(doc);
	}
	
	private static Float getBoost(String propertyName)
	{
		Float boost = propertyName2priority.get(propertyName);
		if(boost == null)
		{
			boost = 0.5f;
		}
		return boost;
	}

	// TODO indexReader should be kept open for fast queries
	public static List<Employee> runQuery(Map<String, String> filterName2filterValue, int hitsPerPage) throws Exception
	{
		// 2. query
		// the "title" arg specifies the default field to use
		// when no field is explicitly specified in the query.
		// Query q = new QueryParser("currentTeam", analyzer).parse(querystr);

		BooleanQuery employeeQuery = new BooleanQuery();
		for (Entry<String, String> filter : filterName2filterValue.entrySet())
		{
			Query q = new QueryParser(filter.getKey(), analyzer).parse(filter.getValue() + "~");
			employeeQuery.add(q, BooleanClause.Occur.SHOULD);
		}

		// 3. search
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		searcher.search(employeeQuery, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		System.out.println("Found " + hits.length + " hits.");
		List<Employee> recommendedEmployees = new ArrayList<Employee>();
		for (int i = 0; i < hits.length; ++i)
		{
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			recommendedEmployees.add(convertDocumentToEmployee(d));
			System.out.println((i + 1) + ". " + d.get("name") + "\t" + d.get("graduateInstitute"));
		}
		return recommendedEmployees;
	}

	private static Employee convertDocumentToEmployee(Document document) throws Exception
	{
		java.lang.reflect.Field[] fields = Employee.class.getDeclaredFields();
		Employee employee = new Employee();
		for (java.lang.reflect.Field field : fields)
		{
			field.setAccessible(true);
			String fieldName = field.getName();
			String fieldValue = document.get(fieldName);
			field.set(employee, fieldValue);
		}
		return employee;
	}

	public static void close()
	{
		try
		{
			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception
	{
		buildIndex(PersistenceManager.getAllEmployees());
		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("name", "ashwin");
		filterMap.put("currentTeam", "step solution");
		System.out.println(runQuery(filterMap, 100));
	}
}