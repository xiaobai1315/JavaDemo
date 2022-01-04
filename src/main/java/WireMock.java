import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

@WireMockTest
public class WireMock {

    @Test
    void record() throws InterruptedException {
        int port =8089;
        //实例化wirmockServer对象
        WireMockServer wireMockServer = new WireMockServer(
                wireMockConfig()
                        //设置mock服务的端口号
                        .port(port)
                        .extensions(new ResponseTemplateTransformer(true))
        );
        //启动mock服务
        wireMockServer.start();
        configureFor(port);
        //设置mock服务中的一个stub，类似于Charles中的一个maplocal规则
        stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(
                        aResponse()
                                //设置返回的状态、header、body体
                                .withStatus(404)
                                .withHeader("Content-Type","text/plain11111111s")
                                .withBody("this is wirmock base cases!")
                )
        );
        System.out.println("http://localhost:"+port);
        Thread.sleep(999999);
        //wiremock复位
        com.github.tomakehurst.wiremock.client.WireMock.reset();
        //wiremock停止
        wireMockServer.stop();
    }

    @Test
    public void test1() {
        String access_token = given().get("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ww5ef451bf006ec894&corpsecret=EcEIog2OJ8AtO7PDaqt_yuVZS3NeYF3kcko9Vd6i9EE")
                .then().extract().response().path("access_token");
        System.out.println(access_token);

        given().contentType("application/json;charset=utf-8")
                .body("{\n" +
                        "   \"touser\" : \"@all\",\n" +
                        "   \"msgtype\" : \"text\",\n" +
                        "   \"agentid\" : 1000002,\n" +
                        "   \"text\" : {\n" +
                        "       \"content\" : \"你的快递已到，请携带工卡前往邮件中心领取。\\n出发前可查看<a href=\\\"http://work.weixin.qq.com\\\">邮件中心视频实况</a>，聪明避开排队。\"\n" +
                        "   },\n" +
                        " \"content\" : \"你的快递已到，请携带工卡前往邮件中心领取。\\n出发前可查看<a href=\\\"http://work.weixin.qq.com\\\">邮件中心视频实况</a>，聪明避开排队。\"\n" +
                        "}")
                .post("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + access_token)
                .then().log().all();
    }

    // A.B.C的形式取值
    @Test
    public void test2() {
        given().get("http://localhost:8080/lotto")
                .then()
                .log().all()
                .body("lotto.lottoId", is(5.123f))
                .body("lotto.winners.winnerId", hasItems(23, 54));
    }

    // 统计所有的字段
     @Test
    public void test3() {
        given().get("http://localhost:8080/lotto")
                .then()
                .body("store.book.findAll { it.price < 10 }.title", hasItems("Moby Dick"));
    }

    // JSON返回值转数据结构
    @Test
    public void test4() {
        List<Map<String, Object>> maps = given().get("http://localhost:8080/lotto")
                .as(new TypeRef<List<Map<String, Object>>>() {
                });
        System.out.println(maps);
    }

    // 从返回结果中获取数据
    @Test
    public void test5() {
        Response response = given()
//                .param("tset")
                .when()
                .get("http://localhost:8080/lotto")
                .then()
                .contentType(ContentType.JSON)    // 判断返回值类型是否是JSON类型
                .body("title", is("My Title"))  // 判断title属性
                .extract()
//                .path("_links.next.href");    // 获取_links.next.href属性
                .response();// 获取全部的返回值

        // 通过response获取返回的数据
        String path = response.path("_links.next.href");
        System.out.println(path);

        // 获取全部的响应头信息
        Headers headers = response.headers();
        System.out.println(headers.toString());
    }


}
