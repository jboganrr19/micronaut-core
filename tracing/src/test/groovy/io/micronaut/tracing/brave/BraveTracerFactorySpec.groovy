/*
 * Copyright 2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.tracing.brave

import brave.Tracing
import brave.http.HttpClientHandler
import brave.http.HttpServerHandler
import brave.http.HttpTracing
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.tracing.brave.sender.HttpClientSender
import io.opentracing.Tracer
import spock.lang.Specification
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.Reporter

/**
 * @author graemerocher
 * @since 1.0
 */
class BraveTracerFactorySpec extends Specification {
    void "test brave tracer configuration no endpoint present"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        when:"The tracer is obtained"
        context.getBean(Tracer)


        then:"It is present"
        thrown(NoSuchBeanException)

    }

    void "test brave tracer configuration"() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'tracing.zipkin.enabled':true,
                'tracing.zipkin.http.endpoint':HttpClientSender.Builder.DEFAULT_SERVER_URL
        )

        expect:"The tracer is obtained"
        context.getBean(AsyncReporter)
        context.getBean(Tracer)
        context.getBean(Tracing)
        context.getBean(HttpTracing)
        context.getBean(HttpClientHandler)
        context.getBean(HttpServerHandler)
    }

    void "test brave tracer configuration no endpoint"() {
        given:
        ApplicationContext context = ApplicationContext.run(
                'tracing.zipkin.enabled':true
        )

        expect:"The tracer is obtained"
        !context.containsBean(Reporter)
        context.getBean(Tracer)
        context.getBean(Tracing)
        context.getBean(HttpTracing)
        context.getBean(HttpClientHandler)
        context.getBean(HttpServerHandler)
    }

    void "test brace tracer report spans"() {
        given:

        ApplicationContext context = ApplicationContext.build()
        context.environment.addPropertySource(PropertySource.of('tracing.zipkin.enabled':true))
        def reporter = new TestReporter()
        context.registerSingleton(reporter)
        context.start()

        when:
        Tracer tracer = context.getBean(Tracer)
        def scope = tracer.buildSpan("test").startActive(true)
        scope.close()

        then:
        reporter.spans.size() == 1
        reporter.spans[0].name() == "test"
    }



}