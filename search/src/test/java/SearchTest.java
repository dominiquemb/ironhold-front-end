import com.reqo.ironhold.search.IndexService;
import org.junit.*;

public class SearchTest {

    private static IndexService indexService;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //indexService = new IndexService("reqo");
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
    public void test() {
    }
    /*
  @Test
  public void test() throws JsonParseException, JsonMappingException,
          IOException {

      SearchHit[] results = indexService.search(indexService.getNewBuilder()
              .withCriteria("Virus")).getHits().getHits();
      Assert.assertTrue(results.length > 0);
      for (SearchHit result : results) {

          // System.out.println(result.getFields().get("pstMessage.subject").getValue().toString());
          // System.out.println(result.getFields().get("pstMessage.body").getValue().toString());
          System.out.println(StringUtils.join(result.getHighlightFields()
                  .get("pstMessage.subject").getFragments(), ","));
          // System.out.println(StringUtils.join(result.getHighlightFields().get("pstMessage.body").getFragments(),",
          "));

          System.out.println("*******************");

      }
  }
    */
}
