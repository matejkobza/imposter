/*
 * Copyright (c) 2016-2021.
 *
 * This file is part of Imposter.
 *
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as
 * defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software: Imposter
 *
 * License: GNU Lesser General Public License version 3
 *
 * Licensor: Peter Cornish
 *
 * Imposter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Imposter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Imposter.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.gatehill.imposter.plugin.openapi;

import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import io.gatehill.imposter.plugin.Plugin;
import io.gatehill.imposter.server.BaseVerticleTest;
import io.gatehill.imposter.util.HttpUtil;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for scripted responses.
 *
 * @author Pete Cornish
 */
public class ScriptedResponseTest extends BaseVerticleTest {
    @Override
    protected Class<? extends Plugin> getPluginClass() {
        return OpenApiPluginImpl.class;
    }

    @Before
    public void setUp(TestContext testContext) throws Exception {
        super.setUp(testContext);
        RestAssured.baseURI = "http://" + getHost() + ":" + getListenPort();
    }

    @Override
    protected List<String> getTestConfigDirs() {
        return Lists.newArrayList(
                "/openapi2/scripted"
        );
    }

    /**
     * Should return the example from the specification when the script triggers an HTTP 201 Created status code.
     */
    @Test
    public void testServeScriptedExample() {
        given()
                .log().ifValidationFails()
                // JSON content type in 'Accept' header matches specification example
                .accept(ContentType.JSON)
                .when()
                .put("/simple/apis")
                .then()
                .log().ifValidationFails()
                .statusCode(HttpUtil.HTTP_CREATED)
                .body("result", equalTo("success"))
                .header("MyHeader", "MyHeaderValue");
    }

    @Test
    public void testRequestWithHeaders() {
        given()
                .log().ifValidationFails()
                .accept(ContentType.TEXT)
                .when()
                .header("Authorization", "AUTH_HEADER")
                .get("/simple/apis")
                .then()
                .statusCode(equalTo(HttpUtil.HTTP_NO_CONTENT));
    }

    @Test
    public void testRequestWithParams() {
        given()
                .log().ifValidationFails()
                .accept(ContentType.TEXT)
                .when()
                .get("/simple/apis?param1=foo")
                .then()
                .statusCode(equalTo(HttpUtil.HTTP_ACCEPTED));
    }
}