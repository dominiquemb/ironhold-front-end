import com.reqo.ironhold.search.IndexService;
import org.junit.*;


public class CreateIndexTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public void test() throws Exception {
        IndexService indexService = new IndexService("unittest");
        indexService.dropIndex();
    }

}
