package com.reqo.ironhold.web.api;

import com.reqo.ironhold.web.api.mocks.MockMessageIndexService;
import com.reqo.ironhold.web.domain.CountSearchResponse;
import com.reqo.ironhold.web.support.ApiResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * User: ilya
 * Date: 11/23/13
 * Time: 11:38 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration("/META-INF/spring/test-api-bootstrap-context.xml")
})
public class MessageControllerFunctionalTests {
    private static final Logger LOG = LoggerFactory.getLogger(MessageControllerFunctionalTests.class);

    @Inject
    private WebApplicationContext wac;

    //@Inject
    //private PatientService patientService;

    @Inject
    private ObjectMapper jacksonObjectMapper;

    @Inject
    private MockMessageIndexService messageIndexService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void testCount() throws Exception {
        String criteria = "randomString";
        String clientKey = "testClient";
        ResultActions resultActions = this.mockMvc.perform(get(String.format("/messages/%s/count?criteria=%s", clientKey, criteria))
                .accept(MediaType.APPLICATION_JSON));
        LOG.debug("RestultActons: " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions.andExpect(status().isOk())
                .andExpect(content().string(containsString(criteria)));

        ApiResponse<CountSearchResponse> result = this.jsonToObject(resultActions.andReturn().getResponse().getContentAsString());

        Assert.assertTrue(result.getPayload().getMatches() >  0);


    }

    private ApiResponse<CountSearchResponse> jsonToObject(String jsonString) throws IOException {
        jacksonObjectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        return jacksonObjectMapper.readValue(jsonString, new TypeReference<ApiResponse<CountSearchResponse>>() {});
    }
}
