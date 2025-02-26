/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.method.annotation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.SynthesizingMethodParameter
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.bind.support.WebRequestDataBinder
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.testfixture.servlet.MockHttpServletRequest
import kotlin.annotation.AnnotationTarget.*

/**
 * Kotlin test fixture for [ModelAttributeMethodProcessor].
 *
 * @author Sebastien Deleuze
 */
class ModelAttributeMethodProcessorKotlinTests {

	private lateinit var container: ModelAndViewContainer

	private lateinit var processor: ModelAttributeMethodProcessor

	private lateinit var param: MethodParameter


	@BeforeEach
	fun setup() {
		container = ModelAndViewContainer()
		processor = ModelAttributeMethodProcessor(false)
		var method = ModelAttributeHandler::class.java.getDeclaredMethod("test",Param::class.java)
		param = SynthesizingMethodParameter(method, 0)
	}

	@Test
	fun resolveArgumentWithValue() {
		val mockRequest = MockHttpServletRequest().apply { addParameter("a", "b") }
		val requestWithParam = ServletWebRequest(mockRequest)
		val factory = Mockito.mock<WebDataBinderFactory>()
		BDDMockito.given(factory.createBinder(any(), any(), eq("param")))
			.willAnswer { WebRequestDataBinder(it.getArgument(1)) }
		assertThat(processor.resolveArgument(this.param, container, requestWithParam, factory)).isEqualTo(Param("b"))
	}

	@Test
	fun throwMethodArgumentNotValidExceptionWithNull() {
		val mockRequest = MockHttpServletRequest().apply { addParameter("a", null) }
		val requestWithParam = ServletWebRequest(mockRequest)
		val factory = Mockito.mock<WebDataBinderFactory>()
		BDDMockito.given(factory.createBinder(any(), any(), eq("param")))
			.willAnswer { WebRequestDataBinder(it.getArgument(1)) }
		assertThatThrownBy {
			processor.resolveArgument(this.param, container, requestWithParam, factory)
		}.isInstanceOf(MethodArgumentNotValidException::class.java)
			.hasMessageContaining("parameter a")
	}

	private data class Param(val a: String)

	@Suppress("UNUSED_PARAMETER")
	private class ModelAttributeHandler {
		fun test(param: Param) { }
	}

}
