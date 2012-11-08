import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.search.SearchHit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.reqo.ironhold.search.IndexService;


public class SearchTest {

	private static IndexService indexService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		indexService = new IndexService("reqo");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		SearchHit[] results = indexService.search("Virus");
		Assert.assertTrue(results.length > 0);
		for (SearchHit result : results) {
			
			
			//System.out.println(result.getFields().get("pstMessage.subject").getValue().toString());
			//System.out.println(result.getFields().get("pstMessage.body").getValue().toString());
			System.out.println(StringUtils.join(result.getHighlightFields().get("pstMessage.subject").getFragments(),","));
			//System.out.println(StringUtils.join(result.getHighlightFields().get("pstMessage.body").getFragments(),","));
			
			System.out.println("*******************");
			
		}
	}

}
