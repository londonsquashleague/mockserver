package org.mockserver.client.proxy;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockserver.client.http.HttpRequestClient;
import org.mockserver.client.serialization.ExpectationSerializer;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class ProxyClientTest {

    @Mock
    private ContentResponse contentResponse;
    @Mock
    private HttpRequestClient mockHttpClient;
    @Mock
    private ExpectationSerializer expectationSerializer;
    @InjectMocks
    private ProxyClient proxyClient;

    @Before
    public void setupTestFixture() throws Exception {
        proxyClient = new ProxyClient("localhost", 8080);

        initMocks(this);
    }

    @Test
    public void shouldSendDumpToLogAsJSONRequest() throws Exception {
        // when
        proxyClient.dumpToLogAsJSON();

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/dumpToLog", "");
    }

    @Test
    public void shouldSendDumpToLogAsJavaRequest() throws Exception {
        // when
        proxyClient.dumpToLogAsJava();

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/dumpToLog?type=java", "");
    }

    @Test
    public void shouldSendResetRequest() throws Exception {
        // when
        proxyClient.reset();

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/reset", "");
    }

    @Test
    public void shouldSendClearRequest() throws Exception {
        // when
        proxyClient
                .clear(
                        new HttpRequest()
                                .withPath("/some_path")
                                .withBody("some_request_body")
                );

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/clear", "" +
                "{\n" +
                "  \"path\" : \"/some_path\",\n" +
                "  \"body\" : \"some_request_body\"\n" +
                "}");
    }

    @Test
    public void shouldReceiveExpectationsAsObjects() {
        // given
        Expectation[] expectations = {};
        when(contentResponse.getContent()).thenReturn("body".getBytes());
        when(mockHttpClient.sendPUTRequest(anyString(), anyString(), anyString())).thenReturn(contentResponse);
        when(expectationSerializer.deserializeArray(aryEq("body".getBytes()))).thenReturn(expectations);

        // when
        assertSame(expectations, proxyClient
                .retrieveAsExpectations(
                        new HttpRequest()
                                .withPath("/some_path")
                                .withBody("some_request_body")
                ));

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/retrieve", "" +
                "{\n" +
                "  \"path\" : \"/some_path\",\n" +
                "  \"body\" : \"some_request_body\"\n" +
                "}");
        verify(expectationSerializer).deserializeArray("body".getBytes());
    }

    @Test
    public void shouldReceiveExpectationsAsObjectsWithNullRequest() {
        // given
        Expectation[] expectations = {};
        when(contentResponse.getContent()).thenReturn("body".getBytes());
        when(mockHttpClient.sendPUTRequest(anyString(), anyString(), anyString())).thenReturn(contentResponse);
        when(expectationSerializer.deserializeArray(aryEq("body".getBytes()))).thenReturn(expectations);

        // when
        assertSame(expectations, proxyClient.retrieveAsExpectations(null));

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/retrieve", "");
        verify(expectationSerializer).deserializeArray("body".getBytes());
    }

    @Test
    public void shouldReceiveExpectationsAsJSON() {
        // given
        String expectations = "body";
        when(contentResponse.getContent()).thenReturn("body".getBytes());
        when(mockHttpClient.sendPUTRequest(anyString(), anyString(), anyString())).thenReturn(contentResponse);

        // when
        assertEquals(expectations, proxyClient
                .retrieveAsJSON(
                        new HttpRequest()
                                .withPath("/some_path")
                                .withBody("some_request_body")
                ));

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/retrieve", "" +
                "{\n" +
                "  \"path\" : \"/some_path\",\n" +
                "  \"body\" : \"some_request_body\"\n" +
                "}");
    }

    @Test
    public void shouldReceiveExpectationsAsJSONWithNullRequest() {
        // given
        String expectations = "body";
        when(contentResponse.getContent()).thenReturn("body".getBytes());
        when(mockHttpClient.sendPUTRequest(anyString(), anyString(), anyString())).thenReturn(contentResponse);

        // when
        assertEquals(expectations, proxyClient.retrieveAsJSON(null));

        // then
        verify(mockHttpClient).sendPUTRequest("http://localhost:8080", "/retrieve", "");
    }
}
